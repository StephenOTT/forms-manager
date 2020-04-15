package formsmanager.hazelcast

import com.hazelcast.config.*
import com.hazelcast.jet.Jet
import com.hazelcast.jet.JetInstance
import com.hazelcast.jet.config.JetConfig
import com.hazelcast.map.MapStore
import formsmanager.hazelcast.context.MicronautManagedContext
import formsmanager.hazelcast.map.HazelcastCrudRepository
import formsmanager.hazelcast.serialization.SmileByteArraySerializer
import formsmanager.ifDebugEnabled
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@Context
class HazelcastJetManager(private val hzConfig: HzConfig) {

    companion object {
        private val log = LoggerFactory.getLogger(HazelcastJetManager::class.java)
    }

    lateinit var defaultInstance: JetInstance

    @PostConstruct
    fun initialize() {

        val jetCfg = JetConfig()
        jetCfg.hazelcastConfig = hzConfig.generateHzConfig()

        defaultInstance = Jet.newJetInstance(jetCfg)
    }
}


@Singleton
class HzConfig(
        private val hazelcastMicronautManagedContext: MicronautManagedContext,
        private val applicationContext: ApplicationContext,
        private val smileSerializer: SmileByteArraySerializer
) {

    companion object {
        private val log = LoggerFactory.getLogger(HzConfig::class.java)
    }

    fun generateHzConfig(): Config {
        //@TODO review for optimization and loading with config and annotations

        val hConfig: Config = ClasspathYamlConfig("hazelcast.yml")

        val serializationConfig = SerializationConfig()

        val globalSerializer = GlobalSerializerConfig()
//                .setOverrideJavaSerialization(true) // Make this a config
                .setImplementation(smileSerializer)
        serializationConfig.globalSerializerConfig = globalSerializer

        hConfig.serializationConfig = serializationConfig

        createMapStoreImplementations(hConfig)

        hConfig.managedContext = hazelcastMicronautManagedContext
//        hConfig.classLoader = applicationContext.classLoader //@TODO review impacts
        return hConfig
    }

    private fun createMapStoreConfig(implementation: MapStore<*, *>,
                                     initialLoadMode: MapStoreConfig.InitialLoadMode = MapStoreConfig.InitialLoadMode.EAGER): MapStoreConfig {
        val config = MapStoreConfig()
        config.initialLoadMode = initialLoadMode
        config.implementation = implementation
        return config
    }

    /**
     * Sets MapStore implementations based on the MapStore annotation used on HazelcastCrudRepository classes.
     */
    private fun createMapStoreImplementations(hConfig: Config){
        applicationContext.getBeanDefinitions(HazelcastCrudRepository::class.java).forEach { repo ->
            repo.getDeclaredAnnotation(formsmanager.hazelcast.annotation.MapStore::class.java)?.let { ann ->
                val mapName = ann.getRequiredValue(formsmanager.hazelcast.annotation.MapStore::mapName.name, String::class.java)

                val mapConfig = hConfig.getMapConfig(mapName)

                // Find the bean associated with the MapStore annotation, and create a MapStoreConfig
                ann.classValue().orElseThrow { IllegalStateException("Expected a class value") }.let {
                    val mapStore = applicationContext.getBean(it)

                    mapConfig.mapStoreConfig = createMapStoreConfig(mapStore as MapStore<*, *>)

                    log.ifDebugEnabled { "Setup MapStore implementation for map $mapName with ${mapStore::class.qualifiedName}" }
                }
            }
        }
    }
}
package formsmanager.hazelcast

import com.hazelcast.config.*
import com.hazelcast.jet.Jet
import com.hazelcast.jet.JetInstance
import com.hazelcast.jet.config.JetConfig
import com.hazelcast.map.MapStore
import formsmanager.respository.FormSchemasMapStore
import formsmanager.respository.FormsMapStore
import formsmanager.validator.queue.HazelcastTransportable
import formsmanager.validator.queue.HazelcastTransportableSmileSerializer
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.inject.Singleton

@Singleton
@Context
class HazelcastJet(private val hzConfig: HzConfig) {

    companion object{
        private val log = LoggerFactory.getLogger(HzConfig::class.java)
    }

    lateinit var jet: JetInstance

    @PostConstruct
    fun initialize(){

        val jconfig = JetConfig()
        jconfig.hazelcastConfig = hzConfig.generateHzConfig()

        jet = Jet.newJetInstance(jconfig)
    }
}

@Singleton
class HzConfig(
        private val hazelcastMicronautManagedContext: ManagedContext,
        private val applicationContext: ApplicationContext,
        private val formsMapStore: FormsMapStore,
        private val formSchemasMapStore: FormSchemasMapStore,
        private val smileSerializer: HazelcastTransportableSmileSerializer
){

    companion object{
        private val log = LoggerFactory.getLogger(HzConfig::class.java)
    }

    fun generateHzConfig():Config{
        val hConfig:Config = ClasspathYamlConfig("hazelcast.yml")

        val smileConfig: SerializerConfig = SerializerConfig()
        smileConfig.implementation = smileSerializer
        smileConfig.typeClass = HazelcastTransportable::class.java

        val serializationConfig = SerializationConfig()
        serializationConfig.addSerializerConfig(smileConfig)

        hConfig.serializationConfig = serializationConfig

        hConfig.getMapConfig("forms").mapStoreConfig = createMapStoreConfig(formsMapStore)
        hConfig.getMapConfig("form-schemas").mapStoreConfig = createMapStoreConfig(formSchemasMapStore)

        hConfig.managedContext = hazelcastMicronautManagedContext
        hConfig.classLoader = applicationContext.classLoader
        return hConfig
    }

    private fun createMapStoreConfig(implementation: MapStore<*,*>,
                                     initialLoadMode: MapStoreConfig.InitialLoadMode = MapStoreConfig.InitialLoadMode.EAGER): MapStoreConfig{
        val config = MapStoreConfig()
        config.initialLoadMode = initialLoadMode
        config.implementation = implementation
        return config
    }
}
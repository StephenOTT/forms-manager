package formsmanager.core.hazelcast

import com.hazelcast.config.ClasspathYamlConfig
import com.hazelcast.config.Config
import com.hazelcast.config.GlobalSerializerConfig
import com.hazelcast.config.SerializationConfig
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.jet.Jet
import com.hazelcast.jet.JetInstance
import com.hazelcast.jet.config.JetConfig
import formsmanager.core.hazelcast.context.MicronautManagedContext
import formsmanager.core.hazelcast.map.persistence.DatabaseMapStoreFactory
import formsmanager.core.hazelcast.serialization.KryoStreamSerializer
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import org.slf4j.LoggerFactory
import javax.inject.Named
import javax.inject.Singleton

@Factory
class HazelcastJetFactory {

    @Singleton
    @Context
    @Primary
    @Named("default")
    fun jet(
            jetConfiguration: JetConfiguration
    ): JetInstance {
        val config: JetConfig = jetConfiguration.createJetConfig()
        return Jet.newJetInstance(config)
    }

    @Singleton
    @Primary
    @Named("default")
    fun hazelcast(
            jet: JetInstance
    ): HazelcastInstance {
        return jet.hazelcastInstance
    }

}

@Singleton
class JetConfiguration(
        private val hazelcastMicronautManagedContext: MicronautManagedContext,
        private val kryoSerializer: KryoStreamSerializer,
        private val databaseMapStoreFactory: DatabaseMapStoreFactory
) {

    companion object {
        //@TODO move to a startup shared logger
        private val log = LoggerFactory.getLogger(JetConfiguration::class.java)
    }

    fun createJetConfig(): JetConfig {
        val jetCfg = JetConfig()
        jetCfg.hazelcastConfig = createHazelcastConfig()

        return jetCfg
    }

    private fun createHazelcastConfig(): Config {
//@TODO review for optimization and loading with config and annotations

        val hConfig: Config = ClasspathYamlConfig("hazelcast.yml")

        val serializationConfig = SerializationConfig()

        val globalSerializer = GlobalSerializerConfig()
                .setOverrideJavaSerialization(true) // Make this a config
                .setImplementation(kryoSerializer)
        serializationConfig.globalSerializerConfig = globalSerializer

        hConfig.serializationConfig = serializationConfig

        hConfig.mapConfigs.filter {
            it.value.mapStoreConfig.factoryClassName == "DatabaseMapStoreFactory"
        }.forEach {
            it.value.mapStoreConfig.factoryImplementation = databaseMapStoreFactory
        }

        hConfig.managedContext = hazelcastMicronautManagedContext
//        hConfig.classLoader = applicationContext.classLoader //@TODO review impacts
        return hConfig
    }

}
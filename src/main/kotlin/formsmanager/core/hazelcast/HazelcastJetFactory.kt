package formsmanager.core.hazelcast

import com.hazelcast.config.*
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.jet.Jet
import com.hazelcast.jet.JetInstance
import com.hazelcast.jet.config.JetConfig
import com.hazelcast.map.MapStore
import formsmanager.core.hazelcast.context.MicronautManagedContext
import formsmanager.core.hazelcast.map.HazelcastCrudRepository
import formsmanager.core.hazelcast.serialization.SmileByteArraySerializer
import formsmanager.core.ifDebugEnabled
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Context
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import org.camunda.bpm.engine.impl.history.event.*
import org.camunda.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity
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
        private val applicationContext: ApplicationContext,
        private val smileSerializer: SmileByteArraySerializer
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

        // Add overrides for Camunda serializations to ensure that the Jackson serializer is used.
        serializationConfig
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricActivityInstanceEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricCaseActivityInstanceEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricCaseInstanceEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricDecisionEvaluationEvent::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricDecisionInstanceEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricDetailEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricExternalTaskLogEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricExternalTaskLogEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricFormPropertyEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricIdentityLinkLogEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricIncidentEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricJobLogEvent::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricProcessInstanceEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricTaskInstanceEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(UserOperationLogEntryEventEntity::class.java))
                .addSerializerConfig(SerializerConfig().setImplementation(smileSerializer).setTypeClass(HistoricVariableInstanceEntity::class.java))


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
    private fun createMapStoreImplementations(hConfig: Config) {
        applicationContext.getBeanDefinitions(HazelcastCrudRepository::class.java).forEach { repo ->
            repo.getDeclaredAnnotation(formsmanager.core.hazelcast.annotation.MapStore::class.java)?.let { ann ->
                val mapName = ann.getRequiredValue(formsmanager.core.hazelcast.annotation.MapStore::mapName.name, String::class.java)

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
package formsmanager.camunda.engine

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import io.micronaut.core.util.StringUtils
import javax.validation.constraints.Min

/**
 * Core Camunda configurations.  Aligns with Camunda SpringBoot Configs as much as possible.
 */
@ConfigurationProperties("camunda")
interface CamundaConfiguration {

    @ConfigurationProperties("bpm")
    interface Bpm {

        companion object{
            const val autoDeploymentEnabled_default: String = StringUtils.TRUE
            const val historyLevel_default: String = "FULL"
        }

        val processEngineName: String?

        val defaultSerializationFormat: String?

        @get:Bindable(defaultValue = historyLevel_default )
        val historyLevel: String?

        @get:Bindable(defaultValue = historyLevel_default)
        val historyLevelDefault: String?

        @get:Bindable(defaultValue = autoDeploymentEnabled_default)
        val autoDeploymentEnabled: Boolean

        @get:Bindable
        val bpmnStacktraceVerbose: Boolean?

        @ConfigurationProperties("jobExecutor")
        interface JobExecutor {

            companion object {
                const val enabled_default: String = StringUtils.TRUE
                const val deploymentAware_default: String = StringUtils.TRUE
            }

            @get:Bindable(defaultValue = enabled_default)
            val enabled: Boolean

            @get:Bindable(defaultValue = deploymentAware_default)
            val deploymentAware: Boolean

            @get:Bindable
            @get:Min(1)
            val queueSize: Int?

            @get:Bindable
            @get:Min(1)
            val corePoolSize: Int?

            @get:Bindable
            @get:Min(1)
            val maxPoolSize: Int?
        }


        @ConfigurationProperties("database")
        interface Database{
            val schemaUpdate: Boolean
            val type: String?
            val tablePrefix: String?
            val schemaName: String?
        }

        @ConfigurationProperties("authorization")
        interface Authorization {
            val enabled: Boolean
            val enabledForCustomCode: Boolean
            val tenantCheckEnabled: Boolean
        }

        @ConfigurationProperties("metrics")
        interface Metrics {
            val enabled: Boolean
            val dbReporterActivate: Boolean
        }
    }

    @ConfigurationProperties("custom")
    interface Custom {
        // @TODO Add custom configs here
    }

}
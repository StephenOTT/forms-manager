package formsmanager.camunda.engine

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.bind.annotation.Bindable
import javax.validation.constraints.Min


@ConfigurationProperties("camunda")
interface CamundaConfiguration {

    @ConfigurationProperties("bpm")
    interface Bpm {
        val processEngineName: String?

        val defaultSerializationFormat: String?

        @get:Bindable(defaultValue = "FULL")
        val historyLevel: String?

        @get:Bindable(defaultValue = "FULL")
        val historyLevelDefault: String?

        val autoDeploymentEnabled: Boolean

        @ConfigurationProperties("jobExecutor")
        interface JobExecutor {

            @get:Bindable(defaultValue = "true")
            val enabled: Boolean

            @get:Bindable(defaultValue = "false")
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
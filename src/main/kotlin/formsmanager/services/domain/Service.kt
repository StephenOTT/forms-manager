package formsmanager.services.domain

import java.util.*

data class Service(
        val id: UUID,
        val actionType: ActionType,
        val tags: Set<String>,
        val data: ServiceData,
        val config: ServiceConfiguration,
        val owner: UUID,
        val defaultLocalization: String,
        val localizations: List<ServiceLocalized>
)

data class ServiceLocalized(
        val languageCode: LocalizationCode,
        val title: String,
        val shortDescription: String,
        val longDescription: String,
        val data: ServiceData,
        val activationUrl: String? = null

)

data class LocalizationCode(val code: String, val description: String)


data class ServiceConfiguration(
        val other: Map<String, Any>
)

data class ServiceData(
        val other: Map<String, Any>
)

enum class ActionType{
    INFORMATION, EXTERNAL_LINK, WORKFLOW, PDF,
}
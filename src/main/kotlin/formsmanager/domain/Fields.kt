package formsmanager.domain

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.Instant

interface TenantField{
    val tenant: String?
}

interface TimestampFields{
    val createdAt: Instant
    val updatedAt: Instant
}

interface DataField{
    val data: Map<String, Any?>
}

interface ConfigField{
    val config: Map<String, Any?>
}

interface OwnerField{
    val owner: String
}

interface EnabledField{
    val enabled: Boolean
}

enum class FormType{
    @JsonProperty("formio")
    FORMIO
}


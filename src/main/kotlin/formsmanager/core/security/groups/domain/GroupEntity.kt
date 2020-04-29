package formsmanager.core.security.groups.domain

import formsmanager.core.ConfigField
import formsmanager.core.DataField
import formsmanager.core.TenantField
import formsmanager.core.TimestampFields
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.security.groups.GroupMapKey
import formsmanager.core.security.groups.repository.GroupEntityWrapper
import formsmanager.core.security.shiro.domain.Role
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
data class GroupEntity(

        override val internalId: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

        val roles: Set<Role>,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt,

        override val data: Map<String, Any?> = mapOf(),

        override val config: Map<String, Any?> = mapOf(),

        override val tenant: UUID

        ): TimestampFields,
        DataField,
        ConfigField,
        TenantField,
        CrudableObject {

    override fun toEntityWrapper(): GroupEntityWrapper {
        return GroupEntityWrapper(getMapKey().toUUID(), this::class.qualifiedName!!, this)
    }

    override fun getMapKey(): GroupMapKey {
        return GroupMapKey(this.name, this.tenant)
    }
}

@Schema
data class GroupEntityCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val roles: Set<Role> = setOf()

){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toGroupEntity(internalId: UUID, tenant: UUID): GroupEntity{
        return GroupEntity(
                internalId = internalId,
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config,
                tenant = tenant,
                roles = roles
        )
    }
}

@Schema
data class GroupEntityModifier(
        var ol: Long = 0,

        var description: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val roles: Set<Role> = setOf()

){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toGroupEntity(internalId: UUID, groupName: String, tenant: UUID): GroupEntity{
        return GroupEntity(
                internalId = internalId,
                ol = ol,
                name = groupName,
                description = description,
                data = data,
                config = config,
                tenant = tenant,
                roles = roles
        )
    }
}

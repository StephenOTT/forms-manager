package formsmanager.core.security.roles.domain

import formsmanager.core.*
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.hazelcast.map.MapKey
import formsmanager.core.security.roles.RoleMapKey
import formsmanager.core.security.roles.repository.RoleEntityWrapper
import io.swagger.v3.oas.annotations.media.Schema
import org.apache.shiro.authz.Permission
import java.time.Instant
import java.util.*

@Schema
data class RoleEntity(

        override val internalId: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

        val permissions: Set<Permission>,

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
    override fun toEntityWrapper(): RoleEntityWrapper {
        return RoleEntityWrapper(getMapKey(), this::class.qualifiedName!!, this)
    }

    override fun getMapKey(): RoleMapKey {
        return RoleMapKey(name, tenant)
    }
}

@Schema
data class RoleEntityCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val permissions: Set<Permission> = setOf()

){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toRoleEntity(internalId: UUID, tenant: UUID): RoleEntity{
        return RoleEntity(
                internalId = internalId,
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config,
                tenant = tenant,
                permissions = permissions
        )
    }
}

@Schema
data class RoleEntityModifier(
        var ol: Long = 0,

        var description: String? = null,

        var defaultSchema: UUID? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val permissions: Set<Permission> = setOf()

){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toRoleEntity(internalId: UUID, name: String, tenant: UUID): RoleEntity{
        return RoleEntity(
                internalId = internalId,
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config,
                tenant = tenant,
                permissions = permissions
        )
    }
}
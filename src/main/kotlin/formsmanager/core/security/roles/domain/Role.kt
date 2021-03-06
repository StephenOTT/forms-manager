package formsmanager.core.security.roles.domain

import com.hazelcast.internal.util.UuidUtil
import formsmanager.core.ConfigField
import formsmanager.core.DataField
import formsmanager.core.TenantField
import formsmanager.core.TimestampFields
import formsmanager.core.hazelcast.map.CrudableObjectId
import formsmanager.tenants.domain.TenantId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

data class RoleId(override val value: UUID): CrudableObjectId<RoleId> {
    override fun toMapKey(): String {
        return value.toString()
    }

    override fun asString(): String {
        return value.toString()
    }

    override fun type(): String {
        return "role"
    }

    override fun compareTo(other: RoleId): Int {
        return value.compareTo(other.value)
    }
}

@Schema
data class Role(

        val id: RoleId,

        val ol: Long = 0,

        val name: String,

        val description: String? = null,

        val permissions: Set<String>,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt,

        override val data: Map<String, Any?> = mapOf(),

        override val config: Map<String, Any?> = mapOf(),

        override val tenant: TenantId

        ): TimestampFields,
        DataField,
        ConfigField,
        TenantField
//        CrudableObject,
//        OptimisticLocking
{
//    override fun toEntityWrapper(): RoleEntity {
//        return RoleEntity(id, this::class.qualifiedName!!, this)
//    }

    companion object {
        val roleNameRegex = Regex("^[A-Z0-9]+(?:_[A-Z0-9]+)*\$")
    }

    init {
        roleNameValidation(name)
    }

    /**
     * Validate role name against Role name validation rules.
     */
    @Throws(IllegalArgumentException::class)
    private fun roleNameValidation(name: String){
        require(name.matches(roleNameRegex))
    }

}

@Schema
data class RoleCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val permissions: Set<String> = setOf()

){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toRole(id: RoleId = RoleId(UuidUtil.newSecureUUID()), tenant: TenantId): Role{
        return Role(
                id = id,
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
data class RoleModifier(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        var defaultSchema: UUID? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val permissions: Set<String> = setOf()

){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toRole(id: RoleId, tenant: TenantId): Role{
        return Role(
                id = id,
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
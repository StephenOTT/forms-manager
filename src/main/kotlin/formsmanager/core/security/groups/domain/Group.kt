package formsmanager.core.security.groups.domain

import com.hazelcast.internal.util.UuidUtil
import formsmanager.core.ConfigField
import formsmanager.core.DataField
import formsmanager.core.TenantField
import formsmanager.core.TimestampFields
import formsmanager.core.hazelcast.map.CrudableObject
import formsmanager.core.hazelcast.map.CrudableObjectId
import formsmanager.core.hazelcast.map.OptimisticLocking
import formsmanager.core.hazelcast.map.persistence.MapStoreEntity
import formsmanager.core.security.SecurityAware
import formsmanager.core.security.groups.repository.GroupEntity
import formsmanager.core.security.roles.domain.RoleId
import formsmanager.core.typeconverter.RenderJoin
import formsmanager.tenants.domain.TenantId
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

data class GroupId(override val value: UUID): CrudableObjectId<GroupId> {
    override fun toMapKey(): String {
        return value.toString()
    }

    override fun asString(): String {
        return value.toString()
    }

    override fun type(): String {
        return "group"
    }

    override fun compareTo(other: GroupId): Int {
        return value.compareTo(other.value)
    }
}

@Schema
data class Group(

        override val id: GroupId,

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

        val roles: Set<RoleId>,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt,

        override val data: Map<String, Any?> = mapOf(),

        override val config: Map<String, Any?> = mapOf(),

        @get:RenderJoin
        override val tenant: TenantId,

        override val owner: UUID

): TimestampFields,
        DataField,
        ConfigField,
        TenantField,
        SecurityAware,
        Comparable<Group>,
        CrudableObject,
        OptimisticLocking {

    override fun toEntityWrapper(): MapStoreEntity<Group> {
        return GroupEntity(id, this::class.qualifiedName!!, this)
    }

    /**
     * Compares based on the createdAt property
     */
    override fun compareTo(other: Group): Int {
        return this.createdAt.compareTo(other.createdAt)
    }
}

@Schema
data class GroupCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val roles: Set<RoleId> = setOf(),

        val owner: UUID

){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toGroupEntity(id: GroupId = GroupId(UuidUtil.newSecureUUID()), tenant: TenantId): Group{
        return Group(
                id = id,
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config,
                tenant = tenant,
                roles = roles,
                owner = owner
        )
    }
}

@Schema
data class GroupModifier(
        var ol: Long = 0,

        val name: String,

        var description: String? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val roles: Set<RoleId> = setOf(),

        val owner: UUID

){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toGroupEntity(id: GroupId, tenant: TenantId): Group{
        return Group(
                id = id,
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config,
                tenant = tenant,
                roles = roles,
                owner = owner
        )
    }
}

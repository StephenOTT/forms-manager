package formsmanager.groups.domain

import formsmanager.core.*
import formsmanager.groups.repository.GroupEntityWrapper
import formsmanager.hazelcast.map.CrudableObject
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.*

@Schema
data class GroupEntity(

        override val id: UUID = UUID.randomUUID(),

        override val ol: Long = 0,

        val name: String,

        val description: String? = null,

        override val createdAt: Instant = Instant.now(),

        override val updatedAt: Instant = createdAt,

        override val data: Map<String, Any?> = mapOf(),

        override val config: Map<String, Any?> = mapOf(),

        override val owner: UUID,

        override val tenant: UUID

        ): TimestampFields,
        DataField,
        ConfigField,
        OwnerField,
        TenantField,
        CrudableObject<UUID> {
    override fun toEntityWrapper(): GroupEntityWrapper {
        return GroupEntityWrapper(id, this::class.qualifiedName!!, this)
    }
}

@Schema
data class GroupEntityCreator(
        var ol: Long = 0,

        var name: String,

        var description: String? = null,

        var defaultSchema: UUID? = null,

        val data: Map<String, Any?> = mapOf(),

        val config: Map<String, Any?> = mapOf(),

        val owner: UUID
){
    /**
     * Convert to GroupEntity.
     * Id is a parameter to allow Creators to be used for existing entities (such as when doing a Update)
     */
    fun toGroupEntity(id: UUID, tenant: UUID): GroupEntity{
        return GroupEntity(
                id = id,
                ol = ol,
                name = name,
                description = description,
                data = data,
                config = config,
                owner = owner,
                tenant = tenant
        )
    }
}
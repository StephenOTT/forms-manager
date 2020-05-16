package formsmanager.core.typeconverter

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.ContextualSerializer
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import formsmanager.core.security.groups.domain.GroupId
import formsmanager.core.security.roles.domain.RoleId
import formsmanager.tenants.domain.TenantId
import formsmanager.tenants.service.TenantService
import formsmanager.tenants.service.getTenant
import io.reactivex.schedulers.Schedulers
import javax.inject.Singleton

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY_GETTER)
annotation class RenderJoin()

@Singleton
class RoleIdSerializer(): StdSerializer<RoleId>(RoleId::class.java){
    override fun serialize(value: RoleId, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.asString())
    }
}

@Singleton
class GroupIdSerializer(): StdSerializer<GroupId>(GroupId::class.java){
    override fun serialize(value: GroupId, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.asString())
    }
}


@Singleton
class TenantIdToTenantNameSerializer(
        private val tenantService: TenantService
): StdSerializer<TenantId>(TenantId::class.java), ContextualSerializer {

    private var hasRenderJoin: Boolean = false

    override fun serialize(value: TenantId, gen: JsonGenerator, provider: SerializerProvider) {
        if (hasRenderJoin){
            val name = value.getTenant(tenantService).subscribeOn(Schedulers.io()).map {
                it.name
            }.blockingGet()
            gen.writeString(name)
        } else {
            gen.writeString(value.asString())
        }
    }

    override fun createContextual(prov: SerializerProvider, property: BeanProperty?): JsonSerializer<*> {
        return if (property != null){
            val ann: RenderJoin? = property.getAnnotation(RenderJoin::class.java)

            return if (ann != null){
                TenantIdToTenantNameSerializer(tenantService = tenantService).apply {
                    hasRenderJoin = true
                }
            } else {
                this
            }
        } else {
            this
        }
    }
}
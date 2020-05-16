package formsmanager.core.typeconverter

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import formsmanager.tenants.domain.TenantId
import formsmanager.tenants.service.TenantService
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TenantNameToTenantIdDeserializer : StdDeserializer<TenantId>(TenantId::class.java) {

    @Inject
    private lateinit var tenantService: TenantService

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TenantId {
        return tenantService.getTenantIdByTenantName(p.text).subscribeOn(Schedulers.io()).blockingGet()
    }
}
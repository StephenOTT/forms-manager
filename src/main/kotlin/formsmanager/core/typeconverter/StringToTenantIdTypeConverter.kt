package formsmanager.core.typeconverter

import formsmanager.tenants.domain.TenantId
import formsmanager.tenants.service.TenantService
import io.micronaut.core.convert.ConversionContext
import io.micronaut.core.convert.TypeConverter
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Singleton

@Singleton
class StringToTenantIdTypeConverter(
        private val tenantService: TenantService
): TypeConverter<String, TenantId> {

    override fun convert(`object`: String, targetType: Class<TenantId>, context: ConversionContext): Optional<TenantId> {
        return tenantService.getTenantIdByTenantName(`object`).map {
            Optional.of(it)
        }.onErrorResumeNext {
            Single.error(IllegalArgumentException("Unable to access requested tenant"))
        }.subscribeOn(Schedulers.io()).blockingGet()
    }
}
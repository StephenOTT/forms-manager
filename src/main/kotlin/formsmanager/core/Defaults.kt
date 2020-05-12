package formsmanager.core

import formsmanager.core.security.groups.domain.GroupCreator
import formsmanager.core.security.groups.domain.GroupId
import formsmanager.core.security.groups.service.GroupService
import formsmanager.core.security.roles.domain.RoleCreator
import formsmanager.core.security.roles.domain.RoleId
import formsmanager.core.security.roles.service.RoleService
import formsmanager.tenants.domain.TenantConfig
import formsmanager.tenants.domain.TenantCreator
import formsmanager.tenants.service.TenantService
import formsmanager.users.domain.User
import formsmanager.users.service.UserService
import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Context
import io.micronaut.context.event.StartupEvent
import io.micronaut.core.annotation.Introspected
import io.micronaut.runtime.event.annotation.EventListener
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull


@Context
class Defaults(
        private val tenantService: TenantService,
        private val roleService: RoleService,
        private val groupService: GroupService,
        private val userService: UserService,
        private val defaultsConfiguration: DefaultsConfiguration
) {

    @EventListener
    fun onStartup(event: StartupEvent) {
        // Create the Defaults.
        // Order is specific. Must be created in order: Tenants > Roles > Groups > Users.
        generateDefaultTenants(this.defaultsConfiguration.tenants, tenantService)
        generateDefaultRoles(this.defaultsConfiguration.roles, roleService, tenantService)
        generateDefaultGroups(this.defaultsConfiguration.groups, groupService, roleService, tenantService)
        generateDefaultUsers(this.defaultsConfiguration.users,userService, groupService, tenantService)
    }


    private fun generateDefaultTenants(tenants: List<DefaultTenant>, tenantService: TenantService) {
        tenants.forEach {
            val tenant = TenantCreator(
                    name = it.name,
                    description = it.description,
                    tenantConfigs = TenantConfig(setOf())
            ).toTenant()

            tenantService.create(tenant).subscribeOn(Schedulers.io())
                    .doOnSuccess { result ->
                        println("Created Tenant ${result.name} (${result.id})")
                    }.blockingGet()
        }
    }

    private fun generateDefaultRoles(roles: List<DefaultRole>, roleService: RoleService, tenantService: TenantService){
        roles.forEach { r ->
            val tenant = tenantService.getTenantIdByTenantName(r.tenant).subscribeOn(Schedulers.io()).blockingGet()

            val role = RoleCreator(
                    name = r.name,
                    permissions = r.permissions
            ).toRole(tenant = tenant)

            roleService.create(role).subscribeOn(Schedulers.io())
                    .doOnSuccess { created ->
                        println("Created Role ${created.name} (${created.id})")
                    }.blockingGet()
        }
    }

    private fun generateDefaultGroups(groups: List<DefaultGroup>, groupService: GroupService, roleService: RoleService,  tenantService: TenantService){
        groups.forEach { g ->
            val tenant = tenantService.getTenantIdByTenantName(g.tenant).subscribeOn(Schedulers.io()).blockingGet()

            val roles: Set<RoleId> = g.roles.map {
                roleService.getByName(it, tenant).subscribeOn(Schedulers.io()).blockingGet()
            }.map { it.id }.toSet()

            val group = GroupCreator(name = g.name, roles = roles, owner = UUID.randomUUID())
                    .toGroupEntity(tenant = tenant)

            groupService.create(group).subscribeOn(Schedulers.io())
                    .doOnSuccess { created ->
                        println("Created Group ${created.name} (${created.id})")
                    }.blockingGet()
        }
    }

    private fun generateDefaultUsers(users: List<DefaultUser>, userService: UserService, groupService: GroupService,  tenantService: TenantService){
        users.forEach { user ->
            val tenant = tenantService.getTenantIdByTenantName(user.tenant).subscribeOn(Schedulers.io()).blockingGet()

            val groupIds: Set<GroupId> = user.groups.map {
                groupService.getByName(it, tenant).subscribeOn(Schedulers.io()).blockingGet()
            }.map {
                it.id
            }.toSet()

            val hash = userService.createPasswordHash(user.password.toCharArray()).subscribeOn(Schedulers.io()).blockingGet()

            val user = User.newUser(user.email, hash.toBase64(), hash.salt.toBase64(), hash.algorithmName, tenant, groupIds)

            userService.create(user).subscribeOn(Schedulers.io())
                    .doOnSuccess { created ->
                        println("Default User ${created.emailInfo.email} (${created.id}) has been created.")
                    }
                    .blockingGet()
        }

    }
}


@ConfigurationProperties("myapp.default")
@Context
class DefaultsConfiguration{

    // NotNull is required because everything is a list with @Valid. Without it validation would not be activated:
    // https://github.com/micronaut-projects/micronaut-core/issues/3218
    @Valid @NotNull
    var tenants: List<DefaultTenant> = listOf()

    @Valid @NotNull
    var roles: List<DefaultRole> = listOf()

    @Valid @NotNull
    var groups: List<DefaultGroup> = listOf()

    @Valid @NotNull
    var users: List<DefaultUser> = listOf()

}

@Introspected
class DefaultTenant{
    @NotBlank
    var name: String = ""
    var description: String? = null
}

@Introspected
class DefaultRole{
    @NotBlank
    var name: String = ""
    @NotBlank
    var tenant: String = ""
    var permissions: Set<String> = setOf()
}

@Introspected
class DefaultGroup{
    @NotBlank
    var name: String = ""
    @NotBlank
    var tenant: String = ""
    var roles: Set<String> = setOf()
}

@Introspected
class DefaultUser{
    @NotBlank
    var email: String = ""

    @NotBlank
    var password: String = ""

    @NotBlank
    var tenant: String = ""
    var groups: Set<String> = setOf()
}

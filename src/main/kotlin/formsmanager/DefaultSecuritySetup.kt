package formsmanager

import formsmanager.tenants.domain.TenantConfig
import formsmanager.tenants.domain.TenantCreator
import formsmanager.tenants.service.TenantService
import io.micronaut.context.annotation.*
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


//data class DefaultSecuritySetup(
//        val dog: String
//)
//
//@EachProperty("myapp.default.tenants")
//@Context
//class DefaultTenant(
//        @param:Parameter var name: String,
//        @param:Parameter var description: String = "Default admin tenant."
//){
//    @Inject
//    private lateinit var tenantService: TenantService
//
//
//    init {
//        val tenant = TenantCreator(
//                name = name,
//                description = description,
//                tenantConfigs = TenantConfig(setOf())
//        ).toTenant()
//
//        tenantService.create(tenant).subscribeOn(Schedulers.io())
//                .doOnSuccess {
//                    println("Created Tenant ${name}")
//                }.blockingGet()
//    }
//}
//
//@EachProperty("myapp.default.roles")
//@Requires(classes = [DefaultTenant::class])
//@Context
//class DefaultRoles(
//        @param:Parameter val name: String,
//        @param:Parameter val tenant: TenantId,
//        @param:Parameter val permissions: Set<String>,
//        private val roleService: RoleService
//) {
//    @PostConstruct
//    fun initialize() {
//        val role = RoleCreator(
//                name = name,
//                permissions = permissions
//        ).toRole(tenant = tenant)
//
//        roleService.create(role).subscribeOn(Schedulers.io())
//                .doOnSuccess {
//                    println("Created Role $name")
//                }.blockingGet()
//    }
//}
//
//@EachProperty("myapp.default.groups")
//@Requires(classes = [DefaultRoles::class])
//@Context
//class DefaultGroups(
//        @param:Parameter val name: String,
//        @param:Parameter val tenant: TenantId,
//        @param:Parameter val roles: Set<String>,
//        private val roleService: RoleService,
//        private val groupService: GroupService
//) {
//    @PostConstruct
//    fun initialize() {
//        val roles: Set<RoleId> = roles.map {
//            roleService.getByName(it, tenant).subscribeOn(Schedulers.io()).blockingGet()
//        }.map { it.id }.toSet()
//
//        val groups = GroupCreator(name = name, roles = roles, owner = UUID.randomUUID())
//                .toGroupEntity(tenant = tenant)
//
//        groupService.create(groups).subscribeOn(Schedulers.io())
//                .doOnSuccess {
//                    println("Created Group $name")
//                }.blockingGet()
//    }
//}
//
//@EachProperty("myapp.default.users")
//@Requires(classes = [DefaultGroups::class])
//@Context
//class DefaultUser(
//        @param:Parameter val email: String,
//        @param:Parameter val password: String,
//        @param:Parameter val tenant: TenantId,
//        @param:Parameter val groups: Set<String>,
//        private val groupService: GroupService,
//        private val userService: UserService
//) {
//    @PostConstruct
//    fun initialize() {
//        val groupIds: Set<GroupId> = groups.map {
//            groupService.getByName(it, tenant).subscribeOn(Schedulers.io()).blockingGet()
//        }.map {
//            it.id
//        }.toSet()
//
//        val hash = userService.createPasswordHash(password.toCharArray()).subscribeOn(Schedulers.io()).blockingGet()
//
//        val user = User.newUser(email, hash.toBase64(), hash.salt.toBase64(), hash.algorithmName, tenant, groupIds)
//
//        userService.create(user).subscribeOn(Schedulers.io())
//                .doOnSuccess {
//                    println("Default User $email has been created.")
//                }
//                .blockingGet()
//    }
//}
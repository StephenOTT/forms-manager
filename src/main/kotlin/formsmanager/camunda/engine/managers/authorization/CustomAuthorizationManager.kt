package formsmanager.camunda.engine.managers.authorization

import formsmanager.core.security.shiro.jwt.JwtToken
import org.apache.shiro.mgt.SecurityManager
import org.apache.shiro.subject.support.DefaultSubjectContext
import org.camunda.bpm.engine.impl.db.CompositePermissionCheck
import org.camunda.bpm.engine.impl.db.PermissionCheck
import org.camunda.bpm.engine.impl.persistence.entity.AuthorizationManager
import javax.inject.Inject

class CustomAuthorizationManager : AuthorizationManager(){

    @Inject
    private lateinit var securityManager: SecurityManager

    override fun isAuthorized(userId: String, groupIds: List<String>?, permissionCheck: PermissionCheck): Boolean {
        if (!isAuthorizationEnabled) {
            return true
        }
        return true
        val subject = securityManager.createSubject(DefaultSubjectContext())
        // @TODO Create a new token that is camunda specific rather than use the JwtToken
        subject.login(JwtToken(userId))
        subject.isPermitted("${permissionCheck.resource.resourceName()}:${permissionCheck.permission.name}:${currentAuthentication.tenantIds.first()}")

        permissionCheck.permission.name //CREATE Permissions.class
        permissionCheck.resource.resourceName() // ProcessInstance Resources.class
        //Permission CREATE_INSTANCE, resource PROCESS_DEFINITION
        // resourceId = "happy"

        if (!isResourceValidForPermission(permissionCheck)) {
            throw LOG.invalidResourceForPermission(permissionCheck.resource.resourceName(), permissionCheck.permission.name)
        }

//        val filteredGroupIds = filterAuthenticatedGroupIds(groupIds)

//        val isRevokeAuthorizationCheckEnabled = isRevokeAuthCheckEnabled(userId, groupIds)
//        val compositePermissionCheck = createCompositePermissionCheck(permissionCheck)
//        permissionCheck
//        val authCheck = AuthorizationCheck(userId, filteredGroupIds, compositePermissionCheck, isRevokeAuthorizationCheckEnabled)
//        return dbEntityManager.selectBoolean("isUserAuthorizedForResource", authCheck)
        return true
    }

    override fun isAuthorized(userId: String, groupIds: List<String>, compositePermissionCheck: CompositePermissionCheck): Boolean {
        for (permissionCheck in compositePermissionCheck.allPermissionChecks) {
            if (!isResourceValidForPermission(permissionCheck)) {
                throw LOG.invalidResourceForPermission(permissionCheck.resource.resourceName(), permissionCheck.permission.name)
            }
        }
//        val filteredGroupIds = filterAuthenticatedGroupIds(groupIds)

//        val isRevokeAuthorizationCheckEnabled = isRevokeAuthCheckEnabled(userId, groupIds)
//        val authCheck = AuthorizationCheck(userId, filteredGroupIds, compositePermissionCheck, isRevokeAuthorizationCheckEnabled)
//        return dbEntityManager.selectBoolean("isUserAuthorizedForResource", authCheck)
        return true
    }

}
package formsmanager.core.security.roles.controler

import io.micronaut.http.annotation.Controller
import org.apache.shiro.authz.annotation.RequiresAuthentication


@RequiresAuthentication
@Controller("security/roles/{tenant}")
class RolesController {

    fun getRoles(){

    }

    fun createRole(){

    }

    fun deleteRole(){

    }

    fun modifyRole(){

    }


}
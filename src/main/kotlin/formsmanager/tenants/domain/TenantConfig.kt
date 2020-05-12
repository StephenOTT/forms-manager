package formsmanager.tenants.domain

import formsmanager.core.security.groups.domain.GroupId

data class TenantConfig(
        val defaultUserGroups: Set<GroupId> = setOf()
)
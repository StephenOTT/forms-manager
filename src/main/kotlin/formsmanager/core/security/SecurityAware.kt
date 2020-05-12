package formsmanager.core.security

import formsmanager.core.OwnerField
import formsmanager.core.TenantField

/**
 * Allows a object to be evaluated by a [formsmanager.core.hazelcast.query.predicate.SecurityPredicate]
 */
interface SecurityAware:
        TenantField,
        OwnerField
package formsmanager.core.hazelcast.query.sql.filterable

import com.hazelcast.query.impl.predicates.SqlPredicate
import formsmanager.core.hazelcast.query.sql.filterable.Filterable

/**
 * Default implementation of Filterable
 */
class DefaultFilterable : Filterable {

    override val sqlPredicate: SqlPredicate?
    override val unfiltered: Boolean

    constructor(sqlPredicate: SqlPredicate){
        this.sqlPredicate = sqlPredicate
        this.unfiltered = false
    }

    constructor(){
        this.sqlPredicate = null
        this.unfiltered = true
    }
}
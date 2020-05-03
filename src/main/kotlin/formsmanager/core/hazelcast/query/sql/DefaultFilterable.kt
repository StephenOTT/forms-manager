package formsmanager.core.hazelcast.query.sql

import com.hazelcast.query.impl.predicates.SqlPredicate

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
WIP prototype to use micronaut with hazelcast to create an in-memory data source 
that micronaut works from and uses Hazelcast's system for cross-app communication

# Endpoints

1. POST /form (Create a Form)
1. GET /form/:uuid (Get a form)
1. PATCH /form (Update a form)
1. POST /form/:uuid/schema?isDefault=true (Create a Form Schema)
1. PATH /form/:uuid/schema/:uuid?isDefault=true (Update a Form Schema)
1. GET /form/:uuid/schema (Get the default schema for the form)
1. GET /form/:uuid/schemas (Get all schemas)
1. GET /form/:uuid/schema/:uuid (Get a specific form schema for a specific form)
1. POST /form/validate (Generic validation)
1. POST /form/:uuid/validate (Validation against the Default Form Schema)
1. POST /form/:uuid/schemas/:schemaUuid/validate (Validation Against a specific Form Schema for a Specific Form)
1. POST /form/:uuid/schemas/:schemaUuid/submit (For use with the Submission Handler)


# Viewers

Swagger file: `http://localhost:8080/swagger/forms-manager-1.0.yml

1. Swagger-Ui: `http://localhost:8080/swagger-ui/index.html`
1. ReDoc: `http://localhost:8080/redoc/index.html`
1. RapiDoc: `http://localhost:8080/rapidoc/index.html`

# Docker

1. Go to project
1. Run `docker build -t forms-manager .`
3. Run `docker run -p 8080:8080 --name forms-manager forms-manager`




Notes:

1. Use Distributed Task for sync requests: Single Request <-> Single Response
1. Use ReliableTopic for broadcasts / Messages that do not have a single destination and can be received by many.
1. Added a InjectAware annotation to tell MN when to inject context 
1. If custom code needs to be shipped around to each member (the member does not actual have the code), then Jet tasks should be used.
1. If required to use confirmation authentication (to perform a sensitive action), then we do JWT + Password login on the endpoint.
1. Shiro Annotations supported through Micronaut SecurityRule beans
1. Issue with performance on Paging Predicate for Hazelcast: https://github.com/hazelcast/hazelcast/issues/10828.  A replacement of the Hazelcast query engine is supposed to be introduced for 4.1/4.2 that would "fix" this issue.  Timeline is ~Fall 2020.
1. Users are owned by a Tenant.  A user can have access to many tenants (through permissions), but lives in only one tenant.
1. A Administrative users or users who control multiple tenants will still have a "home tenant" to which they log in with (where they user exists), and then their permissions provide which tenants they can access.
1. Distributed Query for Hazelcast only works with items that are in Mem (obviously ;) ).  So If items are evicted from the mem based on TTL, then query will not find them.  Items that require search need to be findable based ID.  Use a object as ID that is a makeup of known values for that item.

1. MapKeys are based on a combination of multiple values in a Entity that make up the unique text.  That text is this turned into a UUID **v3**
1. When using the Near Cache on a IMap with Shrio Authz cache, make sure Principal Objects are Serializable (example PrimaryPrincipal.class had to be made serializable) so it can be cached by Shrio.  @TODO Review for ways to remove this need.

1. add expression handler to build custom dynmaic permissions for shiro wildcard https://stackoverflow.com/questions/2286648/named-placeholders-in-string-formatting

1. InjectAware needs to be added to all Tasks.  The inherit does not work apply to the inject.. it would only inject on the extended properties
1. Fields that are `@Inject` should try and always make them public because that way it does not require reflection.

1. ExecutorService in MN has overlap interfaces with Hazelcast: as a result when you create a bean for IExecutorService it will trigger Bean Event triggers for ExecutorService and cause the app to fail.

1. There are 4 Serializers:
   1. jackson used internally by Camunda
   1. jackson created by Micronuat (with Singleton imports and the Application.yml configuration)
   1. Jackson used for Smile: formsmanager.core.serialization.JacksonSmileSerialization.class
   1. Jackson used for Map Store Persistence (JDBC persistence with Hazelcast Mapstore (How the data is stored as JSON in the DB)): formsmanager.core.map.persistence.serialization.JacksonDBSerializationFactory.class


questions
1. Member selection for distributed tasks: To only have specific nodes work on specific tasks.
 

todo:

1. Add Avro support
1. Build an annotation processor for automating field logic updates such as Optimistic Locking increments, UpdatedDate, etc.  Basically any field that cannot be updated by the user.
1. Move controllers to module that is Hazelcast client based
1. Add Camunda node
1. Add TTL for maps with configuration options to clear out/evict memory for seldom used objects. (Evict does not delete from the hazelcast mapstore, it only evicts from in-memory)
1. Add local caching (with TTL) for WildcardPermissions that were generated from Strings in the User Entity
1. Add updated to UserDetails (Micronaut security) for working with Subject from Shiro, so we can use the Micronaut Annotation support `@secured` and accessing the Shiro permission validator. 
1. Eventually move to an ID Generator that is not built in UUID, as UUID is only 99.99 and could be collisions
1. Add user registration limits for username and password: length, password complexity, etc
1. Create a user Entity update page for Admin and for Regular users.
1. ** deal with scenarios of who owns tenants and groups: and how someone can assign the owner of an object: (Likely a permission)**    
1. ** Convert to modern binary serialization to replace Serialization for Shiro Cache (Using Hz) **  
1. Create a new PrincipalCollection class.  Dont use SimplePrincipalCollection as it does not have good enough typing...
1. Create a Mixin Bean support to auto add mixins based on configs

Python execution service:

https://groups.google.com/forum/#!topic/hazelcast/jGZcxpNDc5k
https://github.com/hazelcast/hazelcast-python-client
https://github.com/hazelcast/hazelcast-python-client/blob/master/hazelcast/proxy/executor.py


Formio Links:

1. https://formio.github.io/formio.js/app/examples/
2. Web builder: https://formio.github.io/formio.js/app/builder
3. CUSTOM SUBMISSION: https://formio.github.io/formio.js/app/examples/customendpoint.html
4. Thank you page: https://formio.github.io/formio.js/app/examples/thanyou.html
5. Multiple Languages: https://formio.github.io/formio.js/app/examples/language.html




# Building a Hazelcast based CRUD Repository

A Hazelcast based CRUD Repository is four components:

1. EntityWrapper
1. HazelcastRepository
1. MapStore
1. MapStoreRepository

See FormRepository.class for example usage.

## 1. Entity Wrapper

The entity wrapper is an extend of MapStoreItemWrapperEntity which is a generic for creating the DB entity that will be stored in the JdbcRepository.

Example:

```kotlin
@Entity
class FormEntityWrapper(key: UUID,
                        classId: String,
                        value: FormEntity) : MapStoreItemWrapperEntity<FormEntity>(key, classId, value)

```

## 2. Hazelcast Crud Repository

This is the repository specific to interacting with Hazelcast.
It is the repository that you would inject into your services that wish to perform CRUD operations.

The repository provides: 
1. create
1. update
1. exists
1. delete
1. find by key

Example:

```kotlin
@Singleton
class FormHazelcastRepository(private val jetService: HazelcastJetManager) :
        HazelcastCrudRepository<UUID, FormEntity>(
                jetService = jetService,
                mapName = "forms"
        ) {
}
```

## 3. MapStore Repository

A MapStore Repository extends the CruddableMapStoreRepository which extends the Micronaut CrudRepository.
This repository provides the JDBC connectivity that the MapStore will use.

Example:
```kotlin
@JdbcRepository(dialect = Dialect.H2)
interface FormsMapStoreRepository : CrudableMapStoreRepository<FormEntityWrapper>
```

## 4. MapStore

The MapStore should extend from the CrudableMapStore.
The CrudableMapStore provides ready to use setup with the CrudableMapStoreRepository.

Example:

```kotlin
@Singleton
class FormsMapStore(mapStoreRepository: FormsMapStoreRepository) :
        CurdableMapStore<FormEntity, FormEntityWrapper, FormsMapStoreRepository>(mapStoreRepository)
```




# Create/Update logic notes:

1. Create: Optimistic Locking performs automatically at the Entry Processor level before the insert/update logic is executed.
1. Update logic requires specific updates of fields such as: 
   ```kotlin
    formHazelcastRepository.update(formEntity) { originalItem, newItem ->
        newItem.copy(
                ol = originalItem.ol + 1,
                id = originalItem.id,
                type = originalItem.type,
                createdAt = originalItem.createdAt,
                updatedAt = Instant.now()
        )
    }
   ```
1. During an Update, the new item is injected into the .update() to apply the internal automatic checks and updates (such as the Optimistic locking check).



Words:

Default
Strategy
Simple
Basic
Factory
Service
System
Handler



# Permissions

## Roles

"roles:create:${entity.tenant}"
"roles:read:${g.tenant}"
"roles:update:${originalItem.tenant}"

"groups:create:${groupEntity.tenant}"
"groups:read:${g.tenant}"
"groups:update:${originalItem.tenant}"

"users:read:${ue.tenant}:${ue.internalId}"
"users:update:${userEntity.tenant}:${userEntity.internalId}"

"tenants:create"
"tenants:read:${te.internalId}"
"tenants:update:${originalItem.internalId}"

"forms:create:${formEntity.owner}:${formEntity.tenant}"
"forms:read:${fe.owner}:${fe.tenant}"
"forms:update:${originalItem.owner}:${originalItem.tenant}"
"forms:update:${fe.owner}:${fe.tenant}"
"form_schemas:update:${fe.owner}:${fe.tenant}"
"forms:update:${fe.owner}:${fe.tenant}"
"form_schemas:create:${fe.owner}:${fe.tenant}"
"form_schemas:read:${fe.owner}:${fe.tenant}"
"form_schemas:read:${fe.owner}:${fe.tenant}"
"form_schemas:validate:${fe.owner}:${fe.tenant}"



# Camunda Message Buffer

Prevent race conditions and support operation conditions.
Retry correlations that failed to correlate.

Sometimes you have correlation occurring to multiple instances.
But not all instances were ready at the same time, and durring the correlation, some were missed.
Retry support is provided to continue to correlate until all expected correlations occurred. 


**Actions:**

1. Retry if no matches
1. Exponential Retry
1. Expected Matches: Keep Retry until number of expected matches.  Throw error if too many matches?
1. Retry after expiry
1. Correlate with Response + timeout: if timeout is reached, the result is not waited for, but client will still get a 200. 
1. Correlate without Response
1. Callback URL on completion?
1. Correlate with Start Event
1. Correlate with Activity Execution
1. Correlate with StartEvent and/or Activity Execution
1. Max Events in Buffer
1. Max Correlated Messages In Buffer
1. Max Correlating Messages in Buffer
1. Pause Un-Correlated
1. Metadata about Message Submitter
1. Delegate code support to submit into buffer from scripts and delegates.
1. Complex permissions support: Send Messages per topic, process Def, with and without variables, etc.



# Permissions For

tasks:complete
tasks:view
tasks:assign
tasks:claim
tasks:unclaim

tasks:read_variable:*

Form Submissions are multiple variables.  They are json
If data should not be shared, then it should be split into a different variable for visibility by another user.
(can do this because the practice is that variables are more stable)
Example:
UT1 -> Submit form.  in UT2 user should only be able to see specific fields of a Form.  So in UT2, a new form should be created that loads that specific data.
If further constraints are needed, then Process Instance separation, or Variable permissions should be used.

Further permissions logic and special considerations should VERY likely be used within a Cases model.
WHere a Process Instance is where is being done, but the outcome is stored in a case.


Task Complete:
1. Is Assignee
1. Is member of Candidate Users
1. Is Member of Candidate Group
1. Has instance permission

View Completed Task Info:
1. Is Assignee
1. Is Member of Candidate Group
1. Has Instance Permission
1. Participated in process instance (optional toggle) (query of activity history for that process instance)

Process Instance:
1. Has Instance Permission
1. Participated in Task in instance (query of activity history for that process instance)
1. Has Instance Permission to Definition
1. Member of Process Instance Owner BU
1. Member of Process Definition Owner BU


Process Definition:
1. Process Definition Owner BU
1. Definition instance Permission
1. 


Dynamics:

1. Tenant
1. BU membership
1. Process Instance

WIP prototype to use micronaut with hazelcast to create a in-memory data source 
that micronaut works from and uses Hazelcast's system for cross-app communication

# Endpoints

1. POST /form
1. GET /form/:uuid
1. PATCH /form
1. POST /form/:uuid/schema?isDefault=true
1. GET /form/:uuid/schema
1. GET /form/:uuid/schemas
1. POST /form/validate
1. POST /form/:uuid/validate
1. POST /form/:uuid/schemas/:schemaUuid/validate



Notes:

1. Use Distributed Task for sync requests: Single Request <-> Single Response
1. Use ReliableTopic for broadcasts / Messages that do not have a single destination and can be received by many.
1. Added a InjectAware annotation to tell MN when to inject context 
1. If custom code needs to be shipped around to each member (the member does not actual have the code), then Jet tasks should be used.

questions
1. Member selection for distributed tasks: To only have specific nodes work on specific tasks.
 

todo:

1. Add Avro support


Python execution service:

https://groups.google.com/forum/#!topic/hazelcast/jGZcxpNDc5k
https://github.com/hazelcast/hazelcast-python-client
https://github.com/hazelcast/hazelcast-python-client/blob/master/hazelcast/proxy/executor.py



# Building a Hazelcast based CRUD Repository

A Hazelcast based CRUD Repository is made up of four components:

1. EntityWrapper
1. HazelcastRepository
1. MapStore
1. MapStoreRespository

See FormRepository.class for example usage.

## 1. Entity Wrapper

The entity wrapper is the a extension of MapStoreItemWrapperEntity which is a generic for creating the DB entity that will be stored in the JdbcRepository.

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

This is the extend of the CruddableMapStoreRepository which is a extend of the Micronaut CrudRepository.
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
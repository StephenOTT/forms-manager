package formsmanager.camunda.engine.deploymentcache


//@Singleton
//@Named("hazelcast")
//@Primary
//class HazelcastCacheFactory(
//        private val hazelcastInstance: HazelcastInstance
//) : CacheFactory {
//
//    override fun <T : Any> createCache(maxNumberOfElementsInCache: Int): Cache<String, T> {
//        val cacheName = Class.forName(Thread.currentThread().stackTrace
//                .first {
//                    ResourceDefinitionCache::class.java.isAssignableFrom(Class.forName(it.className)) || ModelInstanceCache::class.java.isAssignableFrom(Class.forName(it.className))
//                }.className).simpleName
//
//        // ProcessDefinitionEntity is not serializaable... So
//        val map = hazelcastInstance.getMap<String, T>("camunda-cache-${cacheName}")
//
//        return HazelcastCache<T>(map)
//    }
//}
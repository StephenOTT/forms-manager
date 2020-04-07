WIP prototype to use micronaut with hazelcast to create a in-memory data source 
that micronaut works from and uses Hazelcast's system for cross-app communication


Notes:

1. Use Distributed Task for sync requests: Single Request <-> Single Response
1. Use ReliableTopic for broadcasts / Messages that do not have a single destination and can be received by many.
1. Added a InjectAware annotation to tell MN when to inject context 

questions
1. Is doing a hzInstance.getReliableTopic() is heavy action? Or can be done every time? Should these instances be kept in a map? (likely not?) 

todo:

1. Add Avro support


Python execution service:

https://groups.google.com/forum/#!topic/hazelcast/jGZcxpNDc5k
https://github.com/hazelcast/hazelcast-python-client
https://github.com/hazelcast/hazelcast-python-client/blob/master/hazelcast/proxy/executor.py




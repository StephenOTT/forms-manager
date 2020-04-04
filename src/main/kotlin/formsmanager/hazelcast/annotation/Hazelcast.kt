package formsmanager.hazelcast.annotation

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.DefaultScope
import io.micronaut.context.annotation.Executable
import io.micronaut.context.annotation.Parallel
import javax.inject.Singleton

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
@Bean
@DefaultScope(Singleton::class)
@Executable(processOnStartup = true)
@Parallel
annotation class Hazelcast(
        val value: String = "default"
) {}

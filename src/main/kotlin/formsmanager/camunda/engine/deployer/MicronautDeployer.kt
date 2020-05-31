package formsmanager.camunda.engine.deployer

import io.micronaut.core.order.Ordered
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer

/**
 * Wrapper for Camunda Deployer interface, and provides a Micronaut Ordered interface for control of execution order
 */
interface MicronautDeployer: Ordered, Deployer
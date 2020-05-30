package formsmanager.camunda.engine.parselistener

import io.micronaut.core.order.Ordered

/**
 * Wrapper for Camunda's BpmnParseListener.
 * Provides the Ordered interface to ensure desired BpmnParseListener execution ordering.
 */
interface MicronautBpmnParseListener: Ordered, org.camunda.bpm.engine.impl.bpmn.parser.BpmnParseListener
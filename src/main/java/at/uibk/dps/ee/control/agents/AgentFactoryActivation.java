package at.uibk.dps.ee.control.agents;

import com.google.inject.Inject;

import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.management.EnactmentAgent;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import at.uibk.dps.ee.model.graph.EnactmentGraph;

/**
 * The {@link AgentFactoryActivation} creates the activation agents.
 * 
 * @author Fedor Smirnov
 */
public class AgentFactoryActivation {

  protected final EnactmentQueues enactmentState;
  protected final ExecutorProvider executorProvider;
  protected final AgentFactoryScheduling schedulingFactory;
  protected final AgentFactoryTransmission transmissionFactory;
  protected final AgentFactoryEnactment enactmentFactory;
  protected final AgentFactoryExtraction extractionFactory;
  protected final AgentFactoryTransform transformFactory;
  protected final GraphAccess graphAccess;

  /**
   * The injection constructor.
   * 
   * @param enactmentState the state of the enactment
   * @param executorProvider the provider for the executor service
   * @param schedulingFactory the factory for the {@link AgentScheduling}s
   * @param transmissionFactory the factory for the {@link AgentTransmission}s
   * @param enactmentFactory the factory for the {@link AgentEnactment}s
   * @param extractionFactory the factory for the {@link AgentExtraction}s
   * @param transformFactory the factory for the {@link AgentTransform}s
   * @param graphAccess the access to the {@link EnactmentGraph}
   */
  @Inject
  public AgentFactoryActivation(final EnactmentQueues enactmentState,
      final ExecutorProvider executorProvider, final AgentFactoryScheduling schedulingFactory,
      final AgentFactoryTransmission transmissionFactory,
      final AgentFactoryEnactment enactmentFactory, final AgentFactoryExtraction extractionFactory,
      final AgentFactoryTransform transformFactory, final GraphAccess graphAccess) {
    this.enactmentState = enactmentState;
    this.executorProvider = executorProvider;
    this.schedulingFactory = schedulingFactory;
    this.transmissionFactory = transmissionFactory;
    this.enactmentFactory = enactmentFactory;
    this.extractionFactory = extractionFactory;
    this.transformFactory = transformFactory;
    this.graphAccess = graphAccess;
  }

  /**
   * Creates the agent monitoring the launchable queue.
   * 
   * @return the agent monitoring the launchable queue.
   */
  public AgentActivationScheduling createSchedulingActivationAgent() {
    return new AgentActivationScheduling(enactmentState, schedulingFactory, executorProvider);
  }

  /**
   * Creates the agent monitoring the available data queue.
   * 
   * @param mainAgent the class starting and stopping the continuous agents
   * @return the agent monitoring the available data queue.
   */
  public AgentActivationTransmission createTransmissionActivationAgent(
      final EnactmentAgent mainAgent) {
    return new AgentActivationTransmission(enactmentState, transmissionFactory, graphAccess,
        executorProvider, mainAgent);
  }

  /**
   * Creates the agent monitoring the scheduled queue.
   * 
   * @return the agent monitoring the scheduled queue.
   */
  public AgentActivationEnactment createEnactmentActivationAgent() {
    return new AgentActivationEnactment(enactmentState, executorProvider, enactmentFactory);
  }

  /**
   * Creates the agent monitoring the finished queue.
   * 
   * @return the agent monitoring the finished queue.
   */
  public AgentActivationExtraction createExtractionActivationAgent() {
    return new AgentActivationExtraction(enactmentState, executorProvider, graphAccess,
        extractionFactory);
  }

  /**
   * Creates the agent monitoring the transform queue.
   * 
   * @return the agent monitoring the transform queue
   */
  public AgentActivationTransform createTransformActicationAgent() {
    return new AgentActivationTransform(enactmentState, transformFactory, executorProvider);
  }
}

package at.uibk.dps.ee.control.agents;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.management.EnactmentAgents;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentActivationTransmission} monitors the queue of available data
 * and creates the transmission agents to transmit available data to the tasks.
 * 
 * @author Fedor Smirnov
 */
public class AgentActivationTransmission extends AgentContinuous implements AgentTaskCreator {

  protected final EnactmentState enactmentState;
  protected final AgentFactoryTransmission agentFactory;
  protected final GraphAccess graphAccess;
  protected final ExecutorService executor;
  protected final Set<AgentTaskListener> listeners = new HashSet<>();

  protected final Set<Task> leafNodes;
  protected final Set<Task> availableWfResults = new HashSet<>();
  protected final EnactmentAgents mainAgent;

  /**
   * The default constructor.
   * 
   * @param enactmentState the state of the enactment (to access the queues)
   * @param agentFactory the factory for the transmission agents
   * @param graphAccess the access to the enactment graph
   * @param executorProvider the provider for the executor service
   * @param mainAgent reference to the main agent (to notify it when the enactment
   *        is finished)
   */
  public AgentActivationTransmission(final EnactmentState enactmentState,
      final AgentFactoryTransmission agentFactory, final GraphAccess graphAccess,
      final ExecutorProvider executorProvider, final EnactmentAgents mainAgent) {
    this.enactmentState = enactmentState;
    this.agentFactory = agentFactory;
    this.graphAccess = graphAccess;
    this.executor = executorProvider.getExecutorService();
    this.leafNodes = graphAccess.getLeafDataNodes();
    this.mainAgent = mainAgent;
  }

  @Override
  protected void operationOnTask(final Task availableData) {
    if (leafNodes.contains(availableData)) {
      availableWfResults.add(availableData);
      if (availableWfResults.containsAll(leafNodes)) {
        mainAgent.wakeUp();
      }
    } else {
      graphAccess.getOutEdges(availableData).forEach(edgeTuple -> executor
          .submit(agentFactory.createTransmissionAgent(edgeTuple, getAgentTaskListeners())));
    }
  }

  @Override
  protected Task getTaskFromBlockingQueue() {
    try {
      return enactmentState.takeAvailableData();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Transmission Activation agent interrupted.", e);
    }
  }

  @Override
  public Set<AgentTaskListener> getAgentTaskListeners() {
    return listeners;
  }

  @Override
  public void addAgentTaskListener(final AgentTaskListener listener) {
    listeners.add(listener);
  }
}

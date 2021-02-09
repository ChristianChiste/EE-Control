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
 * The {@link AgentActivationTransmission} monitors the queue of available data and creates the
 * transmission agents to transmit available data to the tasks.
 * 
 * @author Fedor Smirnov
 */
public class AgentActivationTransmission extends AgentContinuous {

  protected final EnactmentState enactmentState;
  protected final AgentFactoryTransmission agentFactory;
  protected final GraphAccess graphAccess;
  protected final ExecutorService executor;

  protected final Set<Task> leafNodes;
  protected final Set<Task> availableWfResults = new HashSet<>();
  protected final EnactmentAgents rootEnactable;

  public AgentActivationTransmission(EnactmentState enactmentState,
      AgentFactoryTransmission agentFactory, GraphAccess graphAccess,
      ExecutorProvider executorProvider, EnactmentAgents rootEnactable) {
    this.enactmentState = enactmentState;
    this.agentFactory = agentFactory;
    this.graphAccess = graphAccess;
    this.executor = executorProvider.getExecutorService();
    this.leafNodes = graphAccess.getLeafDataNodes();
    this.rootEnactable = rootEnactable;
  }

  @Override
  protected void operationOnTask(Task availableData) {
    if (leafNodes.contains(availableData)) {
      availableWfResults.add(availableData);
      if (availableWfResults.containsAll(leafNodes)) {
        rootEnactable.finishWfExecution();
      }
    } else {
      graphAccess.getOutEdges(availableData)
          .forEach(edgeTuple -> executor.submit(agentFactory.createTransmissionAgent(edgeTuple)));
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
}

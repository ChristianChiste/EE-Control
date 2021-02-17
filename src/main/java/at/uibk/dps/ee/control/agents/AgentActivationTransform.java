package at.uibk.dps.ee.control.agents;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import at.uibk.dps.ee.core.exception.StopException;
import net.sf.opendse.model.Task;

public class AgentActivationTransform extends AgentContinuous implements AgentTaskCreator{

  protected final EnactmentState enactmentState;
  protected final AgentFactoryTransform agentFactory;
  protected final ExecutorService executor;
  protected final Set<AgentTaskListener> listeners = new HashSet<>();
  
  public AgentActivationTransform(EnactmentState enactmentState, AgentFactoryTransform agentFactory,
      ExecutorProvider execProvider) {
    this.enactmentState = enactmentState;
    this.agentFactory = agentFactory;
    this.executor = execProvider.getExecutorService();
  }

  @Override
  public Set<AgentTaskListener> getAgentTaskListeners() {
    return listeners;
  }

  @Override
  public void addAgentTaskListener(AgentTaskListener listener) {
    listeners.add(listener);
  }

  @Override
  protected void operationOnTask(Task task) throws StopException {
    executor.submit(agentFactory.createTransformAgent(task, getAgentTaskListeners()));
  }

  @Override
  protected Task getTaskFromBlockingQueue() {
    try {
      return enactmentState.takeTransformTask();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Transform Activation agent interrupted.", e);
    }
  }
}

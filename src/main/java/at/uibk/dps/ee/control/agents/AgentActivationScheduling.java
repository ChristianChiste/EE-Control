package at.uibk.dps.ee.control.agents;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentActivationScheduling} monitors the queue containing the launchable tasks and
 * creates {@link AgentScheduling}s to process them.
 * 
 * @author Fedor Smirnov
 */
public class AgentActivationScheduling extends AgentContinuous implements AgentTaskCreator {

  protected final EnactmentState enactmentState;
  protected final AgentFactoryScheduling agentFactory;
  protected final ExecutorService executor;
  protected final Set<AgentTaskListener> listeners = new HashSet<>();

  public AgentActivationScheduling(EnactmentState enactmentState,
      AgentFactoryScheduling agentFactory, ExecutorProvider executorProvider) {
    this.enactmentState = enactmentState;
    this.agentFactory = agentFactory;
    this.executor = executorProvider.getExecutorService();
  }

  @Override
  protected void operationOnTask(Task schedulableTask) {
    executor.submit(agentFactory.createSchedulingAgent(schedulableTask, getAgentTaskListeners()));
  }

  @Override
  protected Task getTaskFromBlockingQueue() {
    try {
      return enactmentState.takeSchedulableTask();
    } catch (InterruptedException e) {
      throw new IllegalStateException("Scheduling Activation agent interrupted.", e);
    }
  }

  @Override
  public Set<AgentTaskListener> getAgentTaskListeners() {
    return listeners;
  }

  @Override
  public void addAgentTaskListener(AgentTaskListener listener) {
    listeners.add(listener);
  }
}

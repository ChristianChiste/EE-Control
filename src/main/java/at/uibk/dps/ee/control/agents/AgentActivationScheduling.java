package at.uibk.dps.ee.control.agents;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentActivationScheduling} monitors the queue containing the
 * launchable tasks and creates {@link AgentScheduling}s to process them.
 * 
 * @author Fedor Smirnov
 */
public class AgentActivationScheduling extends AgentContinuous implements AgentTaskCreator {

  protected final EnactmentState enactmentState;
  protected final AgentFactoryScheduling agentFactory;
  protected final ExecutorService executor;
  protected final Set<AgentTaskListener> listeners = new HashSet<>();

  /**
   * The default constructor.
   * 
   * @param enactmentState the state of the enactment (to access the queues)
   * @param agentFactory the factory for the scheduling agents
   * @param executorProvider the provider for the executor service
   */
  public AgentActivationScheduling(final EnactmentState enactmentState,
      final AgentFactoryScheduling agentFactory, final ExecutorProvider executorProvider) {
    this.enactmentState = enactmentState;
    this.agentFactory = agentFactory;
    this.executor = executorProvider.getExecutorService();
  }

  @Override
  protected void operationOnTask(final Task schedulableTask) {
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
  public void addAgentTaskListener(final AgentTaskListener listener) {
    listeners.add(listener);
  }
}

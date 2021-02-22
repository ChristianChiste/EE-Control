package at.uibk.dps.ee.control.agents;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import at.uibk.dps.ee.core.exception.StopException;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentActivationTransform} monitors the queue with the task
 * requiring a graph transformation and creates {@link AgentTransform}s to
 * address the tranformation requests.
 * 
 * @author Fedor Smirnov
 */
public class AgentActivationTransform extends AgentContinuous implements AgentTaskCreator {

  protected final EnactmentQueues enactmentState;
  protected final AgentFactoryTransform agentFactory;
  protected final ExecutorService executor;
  protected final Set<AgentTaskListener> listeners = new HashSet<>();

  /**
   * Default constructor.
   * 
   * @param enactmentState the state of the enactment (to access the queues)
   * @param agentFactory the factory for the transform agents
   * @param execProvider the provider for the executor services
   */
  public AgentActivationTransform(final EnactmentQueues enactmentState,
      final AgentFactoryTransform agentFactory, final ExecutorProvider execProvider) {
    this.enactmentState = enactmentState;
    this.agentFactory = agentFactory;
    this.executor = execProvider.getExecutorService();
  }

  @Override
  public Set<AgentTaskListener> getAgentTaskListeners() {
    return listeners;
  }

  @Override
  public void addAgentTaskListener(final AgentTaskListener listener) {
    listeners.add(listener);
  }

  @Override
  protected void operationOnTask(final Task task) throws StopException {
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

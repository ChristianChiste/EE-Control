package at.uibk.dps.ee.control.agents;

import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.control.enactment.PostEnactmentQueueing;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import net.sf.opendse.model.Task;

/**
 * The default factory for the creation of {@link AgentEnactment}s.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class AgentFactoryEnactment {

  protected final EnactmentQueues enactmentState;

  /**
   * The injection constructor
   * 
   * @param enactmentState the state of the enactment (to access the queues)
   */
  @Inject
  public AgentFactoryEnactment(final EnactmentQueues enactmentState) {
    this.enactmentState = enactmentState;
  }

  /**
   * Creates an agent for the enactment of the given function task.
   * 
   * @param task the given function task
   * @param listeners the agent task listeners
   * @return an agent for the enactment of the given function task
   */
  public AgentEnactment createEnactmentAgent(final Task task,
      final Set<AgentTaskListener> listeners) {
    return new AgentEnactment(task, new PostEnactmentQueueing(enactmentState), listeners);
  }
}

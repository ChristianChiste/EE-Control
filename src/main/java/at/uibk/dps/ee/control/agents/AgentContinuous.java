package at.uibk.dps.ee.control.agents;

import at.uibk.dps.ee.core.exception.StopException;
import net.sf.opendse.model.Task;

/**
 * A continuous agent is an agent which continuously monitors a queue until it
 * receives a {@link PoisonPill}.
 * 
 * @author Fedor Smirnov
 */
public abstract class AgentContinuous implements Agent {

  protected boolean stopped = false;

  @Override
  public Boolean call() throws Exception {
    while (!stopped) {
      final Task task = getTaskFromBlockingQueue();
      if (task instanceof PoisonPill) {
        // stopping
        stopped = true;
        continue;
      } else {
        operationOnTask(task);
      }
    }
    return true;
  }

  /**
   * The task of the continuous agent which is performed on the task retrieved
   * from the blocking queue.
   */
  protected abstract void operationOnTask(Task task) throws StopException;

  /**
   * Retrieves a task from the blocking queue monitored by this agent.
   * 
   * @return a task from the blocking queue monitored by this agent
   */
  protected abstract Task getTaskFromBlockingQueue();
}

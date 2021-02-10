package at.uibk.dps.ee.control.agents;

import java.util.Set;
import net.sf.opendse.model.Task;

/**
 * Parent class for agents which execute a single action based on a given {@link Task}.
 * 
 * @author Fedor Smirnov
 */
public abstract class AgentTask implements Agent {

  protected final Set<AgentTaskListener> listeners;

  public AgentTask(Set<AgentTaskListener> listeners) {
    this.listeners = listeners;
  }

  /**
   * Notifies all listeners that an exception occurred while processing a task.
   * 
   * @param task the processed task
   * @param exc the exception which occurred
   * @param info additional information
   */
  protected void notifyListeners(Exception exc, String info) {
    listeners.forEach(listener -> listener.reactToException(exc, info));
  }

  @Override
  public Boolean call() {
    try {
      return actualCall();
    } catch (Exception exc) {
      String message = formulateExceptionMessage();
      notifyListeners(exc, message);
      return false;
    }
  }

  /**
   * The actual behavior of the agent.
   * 
   * @return
   */
  protected abstract boolean actualCall() throws Exception;

  /**
   * Formulates a message related to the actual functionality of the agent, which is appended to the
   * message of potentially occurring exceptions.
   * 
   * @return a message related to the actual functionality of the agent, which is appended to the
   *         message of potentially occurring exceptions
   */
  protected abstract String formulateExceptionMessage();
}

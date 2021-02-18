package at.uibk.dps.ee.control.agents;

import java.util.Set;
import net.sf.opendse.model.Task;

/**
 * Parent class for agents which execute a single action based on a given
 * {@link Task}.
 * 
 * @author Fedor Smirnov
 */
public abstract class AgentTask implements Agent {

  protected final Set<AgentTaskListener> listeners;

  /**
   * Default constructor
   * 
   * @param listeners the {@link AgentTaskListener}s
   */
  public AgentTask(final Set<AgentTaskListener> listeners) {
    this.listeners = listeners;
  }

  /**
   * Notifies all listeners that an exception occurred while processing a task.
   * 
   * @param task the processed task
   * @param exc the exception which occurred
   * @param info additional information
   */
  protected void notifyListeners(final Exception exc, final String info) {
    listeners.forEach(listener -> listener.reactToException(exc, info));
  }

  @Override
  public Boolean call() {
    try {
      return actualCall();
    } catch (Exception exc) {
      final String message = formulateExceptionMessage();
      notifyListeners(exc, message);
      return false;
    }
  }

  /**
   * The actual behavior of the agent.
   * 
   * @return true iff the action was successful
   */
  protected abstract boolean actualCall() throws Exception;

  /**
   * Formulates a message related to the actual functionality of the agent, which
   * is appended to the message of potentially occurring exceptions.
   * 
   * @return a message related to the actual functionality of the agent, which is
   *         appended to the message of potentially occurring exceptions
   */
  protected abstract String formulateExceptionMessage();
}

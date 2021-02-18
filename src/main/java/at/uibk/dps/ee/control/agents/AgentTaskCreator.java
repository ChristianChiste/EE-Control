package at.uibk.dps.ee.control.agents;

import java.util.Set;

/**
 * Interface for the agents which create {@link AgentTask}s.
 * 
 * @author Fedor Smirnov
 *
 */
public interface AgentTaskCreator {

  /**
   * Returns the list of {@link AgentTaskListener}s which are injected into each
   * created {@link AgentTask}.
   * 
   * @return the list of {@link AgentTaskListener}s which are injected into each
   *         created {@link AgentTask}
   */
  Set<AgentTaskListener> getAgentTaskListeners();

  /**
   * Add the given listener to the list of {@link AgentTaskListener}s which are
   * injected into the created {@link AgentTask}s.
   * 
   * @param listener the listener to add
   */
  void addAgentTaskListener(AgentTaskListener listener);
}

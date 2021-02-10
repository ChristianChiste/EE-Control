package at.uibk.dps.ee.control.agents;

import net.sf.opendse.model.Task;

/**
 * Interface for the classes capable of reacting to certain events occurring during the execution of
 * an action based on a {@link Task} by an agent.
 * 
 * @author Fedor Smirnov
 */
public interface AgentTaskListener {

  /**
   * Reacts to an exception occurring during the processing of the given task.
   * 
   * @param task the given task
   * @param exc the exception which occurred
   * @param additionalInformation additional information about the failure/exception
   */
  void reactToException(Exception exc, String additionalInformation);
}

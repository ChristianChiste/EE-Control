package at.uibk.dps.ee.control.management;

import com.google.inject.ImplementedBy;
import at.uibk.dps.ee.control.agents.AgentTask;
import at.uibk.dps.ee.control.agents.AgentTaskListener;
import at.uibk.dps.ee.core.exception.StopException;

/**
 * The {@link EmergencyManager} defines the reaction to exceptions occurring
 * within {@link AgentTask}s.
 * 
 * @author Fedor Smirnov
 */
@ImplementedBy(EmergencyManagerReact.class)
public interface EmergencyManager extends AgentTaskListener {

  /**
   * Checks whether an emergency occurred since the last check. If so, performs a
   * reaction.
   */
  boolean isEmergency();

  /**
   * Defines the actions which are necessary in case of an emergency.
   */
  void emergencyProtocol() throws StopException;

  /**
   * Registers the main agent (so that it can be woken up).
   * 
   * @param mainAgent the main agent
   */
  void registerMain(EnactmentAgent mainAgent);
}

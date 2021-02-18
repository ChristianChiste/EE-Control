package at.uibk.dps.ee.control.agents;

import net.sf.opendse.model.Task;

/**
 * Task used to terminate the continuous agents.
 * 
 * @author Fedor Smirnov
 */
public class PoisonPill extends Task {

  /**
   * Default constructor.
   */
  public PoisonPill() {
    super("Poison Pill");
  }
}

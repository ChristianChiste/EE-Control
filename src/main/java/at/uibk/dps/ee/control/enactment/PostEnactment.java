package at.uibk.dps.ee.control.enactment;

import at.uibk.dps.ee.control.management.EnactmentState;
import net.sf.opendse.model.Task;

/**
 * Interface for the operations which are to be performed after the enactment
 * associates with a task has been completed (such as putting the task into a
 * queue of the {@link EnactmentState}).
 * 
 * @author Fedor Smirnov
 */
public interface PostEnactment {

  /**
   * Performs the required post enactment operation for the given task.
   * 
   * @param enactedTask the task which was just enacted.
   */
  void postEnactmentTreatment(Task enactedTask);
}

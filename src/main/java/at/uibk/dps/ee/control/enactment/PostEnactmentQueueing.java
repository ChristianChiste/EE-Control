package at.uibk.dps.ee.control.enactment;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.UsageType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow.DataFlowType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow;
import net.sf.opendse.model.Task;

/**
 * The {@link PostEnactmentQueueing} is the operation of putting a task into the
 * appropriate queue of the {@link EnactmentState} after it was enacted.
 * 
 * @author Fedor Smirnov
 */
public class PostEnactmentQueueing implements PostEnactment {

  protected final EnactmentState enactmentState;

  public PostEnactmentQueueing(EnactmentState enactmentState) {
    this.enactmentState = enactmentState;
  }

  @Override
  public void postEnactmentTreatment(Task enactedTask) {
    if (requiresTransformation(enactedTask)) {
      enactmentState.putTransformTask(enactedTask);
    }else {
      enactmentState.putFinishedTask(enactedTask);
    }
  }

  /**
   * Returns true iff the provided task requires a graph transformation after its
   * enactment.
   * 
   * @param task the provided task.
   * @return true iff the provided task requires a graph transformation after its
   *         enactment
   */
  protected boolean requiresTransformation(Task task) {
    return PropertyServiceFunction.getUsageType(task).equals(UsageType.DataFlow)
        && PropertyServiceFunctionDataFlow.getDataFlowType(task).equals(DataFlowType.Collections);
  }
}

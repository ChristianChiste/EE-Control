package at.uibk.dps.ee.control.enactment;

import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.UsageType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow.DataFlowType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow;
import net.sf.opendse.model.Task;

/**
 * The {@link PostEnactmentQueueing} is the operation of putting a task into the
 * appropriate queue of the {@link EnactmentQueues} after it was enacted.
 * 
 * @author Fedor Smirnov
 */
public class PostEnactmentQueueing implements PostEnactment {

	protected final EnactmentQueues enactmentState;

	/**
	 * Default constructor
	 * 
	 * @param enactmentState the enactment state (for the access to the queues)
	 */
	public PostEnactmentQueueing(final EnactmentQueues enactmentState) {
		this.enactmentState = enactmentState;
	}

	@Override
	public void postEnactmentTreatment(final Task enactedTask) {
		if (requiresTransformation(enactedTask)) {
			enactmentState.putTransformTask(enactedTask);
		} else if (PropertyServiceFunction.getEnactable(enactedTask).getState() == State.STOPPED){
			PropertyServiceFunction.getEnactable(enactedTask).setState(State.SCHEDULABLE);
			enactmentState.putSchedulableTask(enactedTask);
		} else {
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
	protected boolean requiresTransformation(final Task task) {
		return PropertyServiceFunction.getUsageType(task).equals(UsageType.DataFlow)
				&& PropertyServiceFunctionDataFlow.getDataFlowType(task).equals(DataFlowType.Collections);
	}
}

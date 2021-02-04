package at.uibk.dps.ee.control.agents;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentEnactment} is responsible for the execution of a single
 * enactable.
 * 
 * @author Fedor Smirnov
 *
 */
public class AgentEnactment implements Agent {

	protected final EnactmentState enactmentState;
	protected final Task functionTask;

	public AgentEnactment(EnactmentState enactmentState, Task functionTask) {
		this.enactmentState = enactmentState;
		this.functionTask = functionTask;
	}

	@Override
	public Boolean call() {
		Enactable enactable = PropertyServiceFunction.getEnactable(functionTask);
		try {
			enactable.play();
		} catch (StopException e) {
			throw new IllegalStateException("Exception while executing the enactable.", e);
		}
		enactmentState.putFinishedTask(functionTask);
		return true;
	}
}

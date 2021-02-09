package at.uibk.dps.ee.control.agents;

import java.util.Set;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.EnactmentFunction;
import at.uibk.dps.ee.enactables.schedule.ScheduleModel;
import at.uibk.dps.ee.enactables.schedule.Scheduler;
import at.uibk.dps.ee.enactables.schedule.ScheduleInterpreter;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentScheduling} is used to check whether a launchable task has
 * already been scheduled and to schedule it, if it is not yet scheduled.
 * 
 * @author Fedor Smirnov
 */
public class AgentScheduling implements Agent {

	protected final ScheduleModel schedule;
	protected final Scheduler scheduler;
	protected final Task functionNode;
	protected final EnactmentState enactmentState;
	protected final ScheduleInterpreter interpreter;

	public AgentScheduling(ScheduleModel schedule, Scheduler scheduler, Task functionNode,
			EnactmentState enactmentState, ScheduleInterpreter interpreter) {
		this.schedule = schedule;
		this.scheduler = scheduler;
		this.functionNode = functionNode;
		this.enactmentState = enactmentState;
		this.interpreter = interpreter;
	}

	@Override
	public Boolean call() throws Exception {
	    System.out.println("start scheduling");
		if (!schedule.isScheduled(functionNode)) {
			Set<Mapping<Task, Resource>> taskSchedule = scheduler.scheduleTask(functionNode);
			schedule.setTaskSchedule(functionNode, taskSchedule);
			System.out.println("schedule task set");
			Enactable taskEnactable = PropertyServiceFunction.getEnactable(functionNode);
			System.out.println("enactable read");
			EnactmentFunction enactmentFunction = interpreter.interpretSchedule(functionNode, taskSchedule);
			System.out.println("enactment function retrieved");
			taskEnactable.schedule(enactmentFunction);
			System.out.println("schedule annotated");
		} else {
			throw new IllegalStateException("Somehow, the task is already scheduled.");
		}
		System.out.println("scheduling done");
		enactmentState.putLaunchableTask(functionNode);
		return true;
	}
}

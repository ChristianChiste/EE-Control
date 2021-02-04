package at.uibk.dps.ee.control.agents;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.scheduling.SchedulerProxy;
import at.uibk.dps.ee.enactables.EESchedule;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentScheduling} is used to check whether a launchable task has
 * already been scheduled and to schedule it, if it is not yet scheduled.
 * 
 * @author Fedor Smirnov
 */
public class AgentScheduling implements Agent {

	protected final EESchedule schedule;
	protected final SchedulerProxy scheduler;
	protected final Task functionNode;
	protected final EnactmentState enactmentState;

	public AgentScheduling(EESchedule schedule, SchedulerProxy scheduler, Task functionNode,
			EnactmentState enactmentState) {
		this.schedule = schedule;
		this.scheduler = scheduler;
		this.functionNode = functionNode;
		this.enactmentState = enactmentState;
	}

	@Override
	public Boolean call() throws Exception {
		if (!schedule.isScheduled(functionNode)) {
			schedule.setTaskSchedule(functionNode, scheduler.getTaskSchedule(functionNode));
		}
		enactmentState.putScheduledTask(functionNode);
		return true;
	}
}

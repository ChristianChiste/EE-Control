package at.uibk.dps.ee.control.agents;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.scheduling.SchedulerProxy;
import at.uibk.dps.ee.enactables.EESchedule;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentFactoryScheduling} creates the agents used for the scheduling
 * of launchable tasks.
 * 
 * @author Fedor Smirnov
 */
public class AgentFactoryScheduling {

	protected final EESchedule schedule;
	protected final SchedulerProxy scheduler;
	protected final EnactmentState enactmentState;

	public AgentFactoryScheduling(EESchedule schedule, SchedulerProxy scheduler, EnactmentState enactmentState) {
		this.schedule = schedule;
		this.scheduler = scheduler;
		this.enactmentState = enactmentState;
	}

	/**
	 * Returns a scheduling agent for the provided function node.
	 * 
	 * @param functionNode the provided function node
	 * @return a scheduling agent for the provided function node
	 */
	public AgentScheduling createSchedulingAgent(Task functionNode) {
		return new AgentScheduling(schedule, scheduler, functionNode, enactmentState);
	}
}

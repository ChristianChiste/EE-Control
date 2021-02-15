package at.uibk.dps.ee.control.agents;

import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.sc.core.ScheduleModel;
import at.uibk.dps.sc.core.interpreter.ScheduleInterpreter;
import at.uibk.dps.sc.core.scheduler.Scheduler;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentFactoryScheduling} creates the agents used for the scheduling
 * of launchable tasks.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class AgentFactoryScheduling {

  protected final ScheduleModel schedule;
  protected final Scheduler scheduler;
  protected final EnactmentState enactmentState;
  protected final ScheduleInterpreter scheduleInterpreter;

  @Inject
  public AgentFactoryScheduling(ScheduleModel schedule, ScheduleInterpreter scheduleInterpreter,
      Scheduler scheduler, EnactmentState enactmentState) {
    this.schedule = schedule;
    this.scheduler = scheduler;
    this.enactmentState = enactmentState;
    this.scheduleInterpreter = scheduleInterpreter;
  }

  /**
   * Returns a scheduling agent for the provided function node.
   * 
   * @param functionNode the provided function node
   * @return a scheduling agent for the provided function node
   */
  public AgentScheduling createSchedulingAgent(Task functionNode,
      Set<AgentTaskListener> listeners) {
    return new AgentScheduling(schedule, scheduler, functionNode, enactmentState,
        scheduleInterpreter, listeners);
  }
}

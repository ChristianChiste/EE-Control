package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.sc.core.ScheduleModel;
import at.uibk.dps.sc.core.interpreter.ScheduleInterpreter;
import at.uibk.dps.sc.core.scheduler.Scheduler;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import java.util.HashSet;
import java.util.Set;

public class AgentFactorySchedulingTest {

  @Test
  public void test() {
    ScheduleModel scheduleMock = mock(ScheduleModel.class);
    Scheduler schedulerMock = mock(Scheduler.class);
    EnactmentState stateMock = mock(EnactmentState.class);
    ScheduleInterpreter interpreterMock = mock(ScheduleInterpreter.class);

    Task task = new Task("task");
    Set<AgentTaskListener> listeners = new HashSet<>();

    AgentFactoryScheduling tested =
        new AgentFactoryScheduling(scheduleMock, interpreterMock, schedulerMock, stateMock);
    AgentScheduling result = tested.createSchedulingAgent(task, listeners);
    assertEquals(task, result.functionNode);
    assertEquals(listeners, result.listeners);
    assertEquals(schedulerMock, result.scheduler);
    assertEquals(scheduleMock, result.schedule);
    assertEquals(interpreterMock, result.interpreter);
    assertEquals(stateMock, result.enactmentState);
  }
}

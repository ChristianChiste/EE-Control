package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Task;

public class AgentActivationSchedulingTest {

  @Test
  public void test() {
    Task task1 = new Task("task1");
    Task task2 = new Task("task2");
    Task task3 = new Task("task3");

    AgentScheduling mockAgent1 = mock(AgentScheduling.class);
    AgentScheduling mockAgent2 = mock(AgentScheduling.class);
    AgentScheduling mockAgent3 = mock(AgentScheduling.class);

    ExecutorProvider execProvider = mock(ExecutorProvider.class);
    ExecutorService mockExecutor = mock(ExecutorService.class);
    when(execProvider.getExecutorService()).thenReturn(mockExecutor);
    AgentTaskListener mockListener = mock(AgentTaskListener.class);
    Set<AgentTaskListener> listeners = new HashSet<>();
    listeners.add(mockListener);

    AgentFactoryScheduling mockFactory = mock(AgentFactoryScheduling.class);
    when(mockFactory.createSchedulingAgent(task1, listeners)).thenReturn(mockAgent1);
    when(mockFactory.createSchedulingAgent(task2, listeners)).thenReturn(mockAgent2);
    when(mockFactory.createSchedulingAgent(task3, listeners)).thenReturn(mockAgent3);

    EnactmentState state = new EnactmentState();

    AgentActivationScheduling tested =
        new AgentActivationScheduling(state, mockFactory, execProvider);
    tested.addAgentTaskListener(mockListener);

    ExecutorService exec = Executors.newCachedThreadPool();
    Future<Boolean> future = exec.submit(tested);

    state.putSchedulableTask(task1);
    state.putSchedulableTask(task2);
    state.putSchedulableTask(task3);
    assertFalse(future.isDone());
    state.putSchedulableTask(new PoisonPill());
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      fail();
    }
    assertTrue(future.isDone());
    verify(mockExecutor).submit(mockAgent1);
    verify(mockExecutor).submit(mockAgent2);
    verify(mockExecutor).submit(mockAgent3);
  }
}

package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import static org.mockito.Mockito.verify;

public class AgentActivationEnactmentTest {

  @Test
  public void test() {
    Task task1 = new Task("task1");
    Task task2 = new Task("task2");
    Task task3 = new Task("task3");

    AgentEnactment mockAgent1 = mock(AgentEnactment.class);
    AgentEnactment mockAgent2 = mock(AgentEnactment.class);
    AgentEnactment mockAgent3 = mock(AgentEnactment.class);

    ExecutorProvider execProvider = mock(ExecutorProvider.class);
    ExecutorService mockExecutor = mock(ExecutorService.class);
    when(execProvider.getExecutorService()).thenReturn(mockExecutor);
    AgentTaskListener mockListener = mock(AgentTaskListener.class);
    Set<AgentTaskListener> listeners = new HashSet<>();
    listeners.add(mockListener);

    AgentFactoryEnactment mockFactory = mock(AgentFactoryEnactment.class);
    when(mockFactory.createEnactmentAgent(task1, listeners)).thenReturn(mockAgent1);
    when(mockFactory.createEnactmentAgent(task2, listeners)).thenReturn(mockAgent2);
    when(mockFactory.createEnactmentAgent(task3, listeners)).thenReturn(mockAgent3);

    EnactmentQueues state = new EnactmentQueues();

    AgentActivationEnactment tested =
        new AgentActivationEnactment(state, execProvider, mockFactory);
    tested.addAgentTaskListener(mockListener);

    ExecutorService exec = Executors.newCachedThreadPool();
    Future<Boolean> future = exec.submit(tested);

    state.putLaunchableTask(task1);
    state.putLaunchableTask(task2);
    state.putLaunchableTask(task3);
    assertFalse(future.isDone());
    state.putLaunchableTask(new PoisonPill());
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

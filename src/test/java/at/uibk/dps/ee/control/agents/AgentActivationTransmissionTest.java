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
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.control.management.EnactmentAgents;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

public class AgentActivationTransmissionTest {

  @Test
  public void test() {
    Communication data = new Communication("data");
    Task task1 = new Task("task1");
    Task task2 = new Task("task2");
    Task task3 = new Task("task3");
    Communication leaf = new Communication("leaf");
    Dependency dep1 = new Dependency("dep1");
    Dependency dep2 = new Dependency("dep2");
    Dependency dep3 = new Dependency("dep3");
    Set<EdgeTupleAppl> tuples = new HashSet<>();

    EdgeTupleAppl tuple1 = new EdgeTupleAppl(data, task1, dep1);
    EdgeTupleAppl tuple2 = new EdgeTupleAppl(data, task2, dep2);
    EdgeTupleAppl tuple3 = new EdgeTupleAppl(data, task3, dep3);
    tuples.add(tuple1);
    tuples.add(tuple2);
    tuples.add(tuple3);

    GraphAccess gAccess = mock(GraphAccess.class);
    when(gAccess.getOutEdges(data)).thenReturn(tuples);

    Set<Task> leaves = new HashSet<>();
    leaves.add(leaf);
    when(gAccess.getLeafDataNodes()).thenReturn(leaves);

    EnactmentAgents mockRoot = mock(EnactmentAgents.class);

    AgentTransmission mockAgent1 = mock(AgentTransmission.class);
    AgentTransmission mockAgent2 = mock(AgentTransmission.class);
    AgentTransmission mockAgent3 = mock(AgentTransmission.class);

    ExecutorProvider execProvider = mock(ExecutorProvider.class);
    ExecutorService mockExecutor = mock(ExecutorService.class);
    when(execProvider.getExecutorService()).thenReturn(mockExecutor);
    AgentTaskListener mockListener = mock(AgentTaskListener.class);
    Set<AgentTaskListener> listeners = new HashSet<>();
    listeners.add(mockListener);

    AgentFactoryTransmission mockFactory = mock(AgentFactoryTransmission.class);
    when(mockFactory.createTransmissionAgent(tuple1, listeners)).thenReturn(mockAgent1);
    when(mockFactory.createTransmissionAgent(tuple2, listeners)).thenReturn(mockAgent2);
    when(mockFactory.createTransmissionAgent(tuple3, listeners)).thenReturn(mockAgent3);

    EnactmentState state = new EnactmentState();

    AgentActivationTransmission tested =
        new AgentActivationTransmission(state, mockFactory, gAccess, execProvider, mockRoot);
    tested.addAgentTaskListener(mockListener);

    ExecutorService exec = Executors.newCachedThreadPool();
    Future<Boolean> future = exec.submit(tested);
    assertFalse(future.isDone());
    state.putAvailableData(data);
    assertFalse(future.isDone());
    state.putAvailableData(leaf);
    state.putAvailableData(new PoisonPill());
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      fail();
    }
    assertTrue(future.isDone());
    verify(mockExecutor).submit(mockAgent1);
    verify(mockExecutor).submit(mockAgent2);
    verify(mockExecutor).submit(mockAgent3);
    verify(mockRoot).wakeUp();
  }

}

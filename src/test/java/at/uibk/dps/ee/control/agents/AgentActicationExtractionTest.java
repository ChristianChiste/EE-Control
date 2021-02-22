package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.Test;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.verify;

public class AgentActicationExtractionTest {

  @Test
  public void test() {
    Task finished = new Task("task1");
    Communication data1 = new Communication("data1");
    Communication data2 = new Communication("data2");
    Communication data3 = new Communication("data3");
    Dependency dep1 = new Dependency("dep1");
    Dependency dep2 = new Dependency("dep2");
    Dependency dep3 = new Dependency("dep3");
    Set<EdgeTupleAppl> tuples = new HashSet<>();
    EdgeTupleAppl tuple1 = new EdgeTupleAppl(finished, data1, dep1);
    EdgeTupleAppl tuple2 = new EdgeTupleAppl(finished, data2, dep2);
    EdgeTupleAppl tuple3 = new EdgeTupleAppl(finished, data3, dep3);
    tuples.add(tuple1);
    tuples.add(tuple2);
    tuples.add(tuple3);

    GraphAccess gAccess = mock(GraphAccess.class);
    when(gAccess.getOutEdges(finished)).thenReturn(tuples);

    AgentExtraction mockAgent1 = mock(AgentExtraction.class);
    AgentExtraction mockAgent2 = mock(AgentExtraction.class);
    AgentExtraction mockAgent3 = mock(AgentExtraction.class);

    ExecutorProvider execProvider = mock(ExecutorProvider.class);
    ExecutorService mockExecutor = mock(ExecutorService.class);
    when(execProvider.getExecutorService()).thenReturn(mockExecutor);
    AgentTaskListener mockListener = mock(AgentTaskListener.class);
    Set<AgentTaskListener> listeners = new HashSet<>();
    listeners.add(mockListener);

    AgentFactoryExtraction mockFactory = mock(AgentFactoryExtraction.class);
    when(mockFactory.createExtractionAgent(tuple1, listeners)).thenReturn(mockAgent1);
    when(mockFactory.createExtractionAgent(tuple2, listeners)).thenReturn(mockAgent2);
    when(mockFactory.createExtractionAgent(tuple3, listeners)).thenReturn(mockAgent3);

    EnactmentQueues state = new EnactmentQueues();

    AgentActivationExtraction tested =
        new AgentActivationExtraction(state, execProvider, gAccess, mockFactory);
    tested.addAgentTaskListener(mockListener);

    ExecutorService exec = Executors.newCachedThreadPool();
    Future<Boolean> future = exec.submit(tested);
    assertFalse(future.isDone());
    state.putFinishedTask(finished);
    assertFalse(future.isDone());
    state.putFinishedTask(new PoisonPill());
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

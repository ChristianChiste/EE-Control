package at.uibk.dps.ee.control.graph;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gson.JsonPrimitive;
import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GraphAccessConcurrentTest {

  protected static class WriteCallable implements Callable<Boolean> {

    protected final Task task;
    protected final long waitTime;
    protected final String attrName = "attr";
    protected final GraphAccessConcurrent graphAccess;

    public WriteCallable(Task task, long waitTime, GraphAccessConcurrent graphAccess) {
      super();
      this.task = task;
      this.waitTime = waitTime;
      this.graphAccess = graphAccess;
    }

    @Override
    public Boolean call() throws Exception {
      graphAccess.writeOperationTask(this::writeStuffInNode, task);
      return true;
    }

    protected void writeStuffInNode(EnactmentGraph graph, Task task) {
      task.setAttribute(attrName, true);
      try {
        Thread.sleep(waitTime);
      } catch (InterruptedException e) {
        fail();
      }
    }
  }

  protected static class ReadCallable implements Callable<Boolean> {

    protected final Task task;
    protected final GraphAccessConcurrent graphAccess;
    protected final long waitTime;

    public ReadCallable(Task task, GraphAccessConcurrent graphAccess, long waitTime) {
      this.task = task;
      this.graphAccess = graphAccess;
      this.waitTime = waitTime;
    }

    @Override
    public Boolean call() throws Exception {
      graphAccess.getOutEdges(task);
      try {
        Thread.sleep(waitTime);
        return true;
      } catch (InterruptedException e) {
        fail();
        return false;
      }
    }
  }

  @Test
  public void testNoReadLock() {
    EnactmentGraph graph = new EnactmentGraph();
    Task task1 = new Task("task1");
    graph.addVertex(task1);
    GraphProviderEnactables providerMock = mock(GraphProviderEnactables.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    GraphAccessConcurrent tested = new GraphAccessConcurrent(providerMock);

    ReadCallable reader1 = new ReadCallable(task1, tested, 0);
    ReadCallable reader2 = new ReadCallable(task1, tested, 250);


    ExecutorService exec = Executors.newCachedThreadPool();

    Instant before = Instant.now();
    Future<Boolean> readFuture2 = exec.submit(reader2);
    try {
      Thread.sleep(50);
    } catch (InterruptedException e1) {
      fail();
    }
    Future<Boolean> readFuture1 = exec.submit(reader1);

    try {
      assertTrue(readFuture1.get());
      assertFalse(readFuture2.isDone());
      Instant after = Instant.now();
      long readTime = Duration.between(before, after).toMillis();
      assertTrue(readTime < reader2.waitTime);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testWriteLock() {
    EnactmentGraph graph = new EnactmentGraph();
    Task task1 = new Task("task1");
    graph.addVertex(task1);
    GraphProviderEnactables providerMock = mock(GraphProviderEnactables.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    GraphAccessConcurrent tested = new GraphAccessConcurrent(providerMock);

    WriteCallable writer = new WriteCallable(task1, 250, tested);
    ReadCallable reader = new ReadCallable(task1, tested, 0);

    ExecutorService exec = Executors.newCachedThreadPool();

    Instant before = Instant.now();
    Future<Boolean> writeFuture = exec.submit(writer);
    try {
      Thread.sleep(50);
    } catch (InterruptedException e1) {
      fail();
    }
    Future<Boolean> readFuture = exec.submit(reader);

    try {
      assertTrue(readFuture.get());
      assertTrue(writeFuture.isDone());
      Instant after = Instant.now();
      assertTrue(writer.task.getAttribute(writer.attrName));
      long readTime = Duration.between(before, after).toMillis();
      assertTrue(readTime >= writer.waitTime);
    } catch (Exception e) {
      fail();
    }
  }


  @Test
  public void testGetOutEdges() {
    EnactmentGraph graph = new EnactmentGraph();
    Task task1 = new Task("task1");
    Dependency edge1 = new Dependency("edge1");
    Task task2 = new Task("task2");
    Dependency edge2 = new Dependency("edge2");
    Task task3 = new Task("task3");
    graph.addEdge(edge1, task1, task2, EdgeType.DIRECTED);
    graph.addEdge(edge2, task1, task3, EdgeType.DIRECTED);
    GraphProviderEnactables providerMock = mock(GraphProviderEnactables.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    GraphAccessConcurrent tested = new GraphAccessConcurrent(providerMock);
    Set<EdgeTupleAppl> result = tested.getOutEdges(task1);
    assertEquals(2, result.size());
    Iterator<EdgeTupleAppl> iter = result.iterator();
    EdgeTupleAppl first = iter.next();
    EdgeTupleAppl second = iter.next();
    assertEquals(task1, first.getSrc());
    assertEquals(task1, second.getSrc());
    assertTrue(first.getEdge().equals(edge1) || first.getEdge().equals(edge2));
    assertTrue(second.getEdge().equals(edge1) || second.getEdge().equals(edge2));
    assertTrue(first.getDst().equals(task2) || first.getDst().equals(task3));
    assertTrue(second.getDst().equals(task2) || second.getDst().equals(task3));
  }

  @Test
  public void testGetConstantDataNodes() {
    EnactmentGraph graph = new EnactmentGraph();
    Task data1 =
        PropertyServiceData.createConstantNode("data1", DataType.Boolean, new JsonPrimitive(true));
    Task data2 =
        PropertyServiceData.createConstantNode("data2", DataType.Number, new JsonPrimitive(42));
    graph.addVertex(data1);
    graph.addVertex(data2);

    GraphProviderEnactables providerMock = mock(GraphProviderEnactables.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    GraphAccessConcurrent tested = new GraphAccessConcurrent(providerMock);
    Set<Task> constants = tested.getConstantDataNodes();
    assertEquals(2, constants.size());
    assertTrue(constants.contains(data1));
    assertTrue(constants.contains(data2));
  }

  @Test
  public void testGetLeafDataNodes() {
    EnactmentGraph graph = new EnactmentGraph();
    Communication data1 = new Communication("data1");
    Communication data2 = new Communication("data2");
    PropertyServiceData.makeLeaf(data1);
    PropertyServiceData.makeLeaf(data2);
    graph.addVertex(data1);
    graph.addVertex(data2);

    GraphProviderEnactables providerMock = mock(GraphProviderEnactables.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    GraphAccessConcurrent tested = new GraphAccessConcurrent(providerMock);
    Set<Task> leaves = tested.getLeafDataNodes();
    assertEquals(2, leaves.size());
    assertTrue(leaves.contains(data1));
    assertTrue(leaves.contains(data2));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetLeafDataNodesExc() {
    EnactmentGraph graph = new EnactmentGraph();
    Communication data1 = new Communication("data1");
    Communication data2 = new Communication("data2");
    Communication data3 = new Communication("data3");
    PropertyServiceData.makeLeaf(data1);
    PropertyServiceData.makeLeaf(data2);
    graph.addVertex(data1);
    graph.addVertex(data2);
    graph.addVertex(data3);

    GraphProviderEnactables providerMock = mock(GraphProviderEnactables.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    GraphAccessConcurrent tested = new GraphAccessConcurrent(providerMock);
    tested.getLeafDataNodes();
  }

  @Test
  public void testGetRootDataNodes() {
    EnactmentGraph graph = new EnactmentGraph();
    Communication data1 = new Communication("data1");
    Communication data2 = new Communication("data2");
    PropertyServiceData.makeRoot(data1);
    PropertyServiceData.makeRoot(data2);
    graph.addVertex(data1);
    graph.addVertex(data2);

    GraphProviderEnactables providerMock = mock(GraphProviderEnactables.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    GraphAccessConcurrent tested = new GraphAccessConcurrent(providerMock);
    Set<Task> roots = tested.getRootDataNodes();
    assertEquals(2, roots.size());
    assertTrue(roots.contains(data1));
    assertTrue(roots.contains(data2));
  }

  @Test(expected = IllegalStateException.class)
  public void testGetRootDataNodesExc() {
    EnactmentGraph graph = new EnactmentGraph();
    Communication data1 = new Communication("data1");
    Communication data2 = new Communication("data2");
    Communication data3 = new Communication("data3");
    PropertyServiceData.makeRoot(data1);
    PropertyServiceData.makeRoot(data2);
    graph.addVertex(data1);
    graph.addVertex(data2);
    graph.addVertex(data3);

    GraphProviderEnactables providerMock = mock(GraphProviderEnactables.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    GraphAccessConcurrent tested = new GraphAccessConcurrent(providerMock);
    tested.getRootDataNodes();
  }
}

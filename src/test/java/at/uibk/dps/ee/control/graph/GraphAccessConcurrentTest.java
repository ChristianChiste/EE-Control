package at.uibk.dps.ee.control.graph;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.util.Iterator;
import java.util.Set;

public class GraphAccessConcurrentTest {

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

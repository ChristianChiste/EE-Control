package at.uibk.dps.ee.control.graph;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphProviderEnactablesTest {

  @Test
  public void test() {
    EnactmentGraph graph = new EnactmentGraph();
    Task task1 = new Task("task1");
    Communication comm = new Communication("comm");
    Task task2 = new Task("task2");
    graph.addVertex(comm);
    graph.addVertex(task2);
    graph.addVertex(task1);
    EnactmentGraphProvider providerMock = mock(EnactmentGraphProvider.class);
    when(providerMock.getEnactmentGraph()).thenReturn(graph);
    EnactableFactory factoryMock = mock(EnactableFactory.class);
    EnactableAtomic enactable1 = mock(EnactableAtomic.class);
    EnactableAtomic enactable2 = mock(EnactableAtomic.class);
    when(factoryMock.createEnactable(task1)).thenReturn(enactable1);
    when(factoryMock.createEnactable(task2)).thenReturn(enactable2);
    GraphProviderEnactables tested = new GraphProviderEnactables(providerMock, factoryMock);
    EnactmentGraph result = tested.getEnactmentGraph();
    assertEquals(graph, result);
    assertEquals(enactable1, PropertyServiceFunction.getEnactable(task1));
    assertEquals(enactable2, PropertyServiceFunction.getEnactable(task2));
  }
}

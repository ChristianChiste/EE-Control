package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import java.util.HashSet;
import java.util.Set;

public class AgentTransmissionTest {

  @Test
  public void testActualCall() {
    EnactmentState stateMock = mock(EnactmentState.class);
    Communication dataNode = new Communication("data");
    JsonElement content = new JsonPrimitive(42);
    PropertyServiceData.setContent(dataNode, content);
    Dependency edge = new Dependency("dep");
    Task function = new Task("function");
    GraphAccess gMock = mock(GraphAccess.class);
    Set<AgentTaskListener> listeners = new HashSet<>();
    String key = "key";
    PropertyServiceDependency.setJsonKey(edge, key);
    Enactable enactableMock = mock(Enactable.class);
    PropertyServiceFunction.setEnactable(function, enactableMock);

    AgentTransmission tested =
        new AgentTransmission(stateMock, dataNode, edge, function, gMock, listeners);
    String expected = ConstantsAgents.ExcMessageTransmissionPrefix + dataNode.getId()
        + ConstantsAgents.ExcMessageTransmissionSuffix + function.getId();
    assertEquals(expected, tested.formulateExceptionMessage());
    try {
      tested.actualCall();
      verify(enactableMock).setInputValue(key, content);
    } catch (Exception e) {
      fail();
    }
  }

  @Test
  public void testTransmitData() {
    EnactmentState stateMock = mock(EnactmentState.class);
    Communication dataNode = new Communication("data");
    JsonElement content = new JsonPrimitive(42);
    PropertyServiceData.setContent(dataNode, content);
    Dependency edge = new Dependency("dep");
    Task function = new Task("function");
    GraphAccess gMock = mock(GraphAccess.class);
    Set<AgentTaskListener> listeners = new HashSet<>();
    String key = "key";
    PropertyServiceDependency.setJsonKey(edge, key);
    Enactable enactableMock = mock(Enactable.class);
    PropertyServiceFunction.setEnactable(function, enactableMock);

    AgentTransmission tested =
        new AgentTransmission(stateMock, dataNode, edge, function, gMock, listeners);

    assertFalse(PropertyServiceDependency.isTransmissionDone(edge));

    Dependency otherEdge1 = new Dependency("e1");
    Dependency otherEdge2 = new Dependency("e2");
    Set<Dependency> inEdges = new HashSet<>();
    inEdges.add(otherEdge2);
    inEdges.add(otherEdge1);
    inEdges.add(edge);
    Communication comm1 = new Communication("comm1");
    Communication comm2 = new Communication("comm2");
    Communication comm3 = new Communication("comm3");
    EnactmentGraph graph = new EnactmentGraph();
    graph.addEdge(edge, comm1, function, EdgeType.DIRECTED);
    graph.addEdge(otherEdge1, comm2, function, EdgeType.DIRECTED);
    graph.addEdge(otherEdge2, comm3, function, EdgeType.DIRECTED);

    PropertyServiceDependency.annotateFinishedTransmission(otherEdge1);

    tested.annotateTransmission(graph, function);
    assertTrue(PropertyServiceDependency.isTransmissionDone(edge));
    verify(stateMock, times(0)).putSchedulableTask(function);

    PropertyServiceDependency.annotateFinishedTransmission(otherEdge2);
    tested.annotateTransmission(graph, function);
    assertTrue(PropertyServiceDependency.isTransmissionDone(edge));
    verify(stateMock, times(1)).putSchedulableTask(function);
  }
}

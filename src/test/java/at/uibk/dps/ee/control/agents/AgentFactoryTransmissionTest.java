package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.control.transmission.SchedulabilityCheckDefault;
import at.uibk.dps.ee.control.transmission.SchedulabilityCheckMuxer;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUser;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow.DataFlowType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import java.util.HashSet;
import java.util.Set;

public class AgentFactoryTransmissionTest {

  @Test
  public void test() {
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    GraphAccess graphMock = mock(GraphAccess.class);
    Communication data = new Communication("data");
    Dependency edge = new Dependency("edge");
    Task task = PropertyServiceFunctionUser.createUserTask("task", "addition");
    EdgeTupleAppl tuple = new EdgeTupleAppl(data, task, edge);
    Set<AgentTaskListener> listeners = new HashSet<>();
    AgentFactoryTransmission tested = new AgentFactoryTransmission(stateMock, graphMock);
    AgentTransmission result = tested.createTransmissionAgent(tuple, listeners);
    assertEquals(stateMock, result.enactmentState);
    assertEquals(graphMock, result.graphAccess);
    assertEquals(data, result.dataNode);
    assertEquals(edge, result.edge);
    assertEquals(task, result.functionNode);
    assertEquals(listeners, result.listeners);
  }

  @Test
  public void testGetCheckForTarget() {
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    GraphAccess graphMock = mock(GraphAccess.class);
    AgentFactoryTransmission tested = new AgentFactoryTransmission(stateMock, graphMock);
    Task normalTask = PropertyServiceFunctionUser.createUserTask("userTask", "addition");
    Task muxer =
        PropertyServiceFunctionDataFlow.createDataFlowFunction("muxer", DataFlowType.Multiplexer);
    assertTrue(tested.getCheckForTarget(normalTask) instanceof SchedulabilityCheckDefault);
    assertTrue(tested.getCheckForTarget(muxer) instanceof SchedulabilityCheckMuxer);
  }
}

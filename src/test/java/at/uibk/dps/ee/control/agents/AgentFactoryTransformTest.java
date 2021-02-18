package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphTransformAggregation;
import at.uibk.dps.ee.control.graph.GraphTransformDistribution;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections.OperationType;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import java.util.HashSet;

public class AgentFactoryTransformTest {

  @Test
  public void test() {
    GraphAccess gMock = mock(GraphAccess.class);
    EnactableFactory factoryMock = mock(EnactableFactory.class);
    EnactmentState stateMock = mock(EnactmentState.class);
    AgentFactoryTransform tested = new AgentFactoryTransform(gMock, factoryMock, stateMock);
    Task aggregationTask = PropertyServiceFunctionDataFlowCollections
        .createCollectionDataFlowTask("t1", OperationType.Aggregation, "scope");
    Task distributionTask = PropertyServiceFunctionDataFlowCollections
        .createCollectionDataFlowTask("t2", OperationType.Distribution, "scope");
    AgentTransform resultAgg = tested.createTransformAgent(aggregationTask, new HashSet<>());
    assertTrue(resultAgg.modification instanceof GraphTransformAggregation);
    AgentTransform resultDist = tested.createTransformAgent(distributionTask, new HashSet<>());
    assertTrue(resultDist.modification instanceof GraphTransformDistribution);
  }
}

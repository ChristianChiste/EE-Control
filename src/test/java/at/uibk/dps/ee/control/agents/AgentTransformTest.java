package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import java.util.HashSet;
import org.junit.Test;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphTransform;
import at.uibk.dps.ee.control.management.EnactmentState;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AgentTransformTest {

  @Test
  public void test() {
    Task taskNode = new Task("task");
    GraphTransform modification = mock(GraphTransform.class);
    String modName = "modification";
    when(modification.getTransformName()).thenReturn(modName);
    GraphAccess gAccess = mock(GraphAccess.class);
    EnactmentState eState = mock(EnactmentState.class);
    AgentTransform tested =
        new AgentTransform(new HashSet<>(), gAccess, modification, taskNode, eState);

    String expectedMessage = ConstantsAgents.ExcMessageTransformPrefix + modName
        + ConstantsAgents.ExcMessageTransformSuffix + taskNode.getId();
    assertEquals(expectedMessage, tested.formulateExceptionMessage());

    try {
      assertTrue(tested.actualCall());
      verify(modification).modifyEnactmentGraph(gAccess, taskNode);
      verify(eState).putFinishedTask(taskNode);
    } catch (Exception e) {
      fail();
    }
  }
}

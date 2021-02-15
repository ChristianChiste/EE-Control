package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.HashSet;;

public class AgentEnactmentTest {

  @Test
  public void test() {

    EnactmentState mockState = mock(EnactmentState.class);
    Enactable mockEnactable = mock(Enactable.class);
    Task task = new Task("task");
    PropertyServiceFunction.setEnactable(task, mockEnactable);
    AgentEnactment tested = new AgentEnactment(mockState, task, new HashSet<>());
    assertEquals(ConstantsAgents.ExcMessageEnactment + task.getId(),
        tested.formulateExceptionMessage());
    try {
      tested.actualCall();
      verify(mockEnactable).play();
    } catch (Exception e) {
      fail();
    }
  }
}

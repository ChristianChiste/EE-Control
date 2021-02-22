package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import java.util.HashSet;
import java.util.Set;

public class AgentFactoryEnactmentTest {

  @Test
  public void test() {
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    AgentFactoryEnactment tested = new AgentFactoryEnactment(stateMock);
    Task task = new Task("task");
    Set<AgentTaskListener> listeners = new HashSet<>();
    AgentEnactment result = tested.createEnactmentAgent(task, listeners);
    assertEquals(task, result.taskNode);
    assertEquals(listeners, result.listeners);
  }
}

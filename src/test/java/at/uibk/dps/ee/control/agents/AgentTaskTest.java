package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class AgentTaskTest {

  protected static class MockAgent extends AgentTask {
    protected final String message = "myMessage";
    protected final Exception exc = new IllegalArgumentException("exception");

    public MockAgent(Set<AgentTaskListener> listeners) {
      super(listeners);
    }

    @Override
    protected boolean actualCall() throws Exception {
      throw exc;
    }

    @Override
    protected String formulateExceptionMessage() {
      return message;
    }
  }

  @Test
  public void test() {
    AgentTaskListener listener1 = mock(AgentTaskListener.class);
    AgentTaskListener listener2 = mock(AgentTaskListener.class);
    Set<AgentTaskListener> listeners = new HashSet<>();
    listeners.add(listener1);
    listeners.add(listener2);
    MockAgent tested = new MockAgent(listeners);
    assertFalse(tested.call());
    verify(listener1).reactToException(tested.exc, tested.message);
    verify(listener2).reactToException(tested.exc, tested.message);
  }
}

package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import at.uibk.dps.ee.core.exception.StopException;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doReturn;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import static org.mockito.Mockito.verify;

public class AgentActivationTransformTest {

  @Test
  public void testGetSetListener() {
    EnactmentState stateMock = mock(EnactmentState.class);
    AgentFactoryTransform factoryMock = mock(AgentFactoryTransform.class);
    ExecutorService execMock = mock(ExecutorService.class);
    ExecutorProvider providerMock = mock(ExecutorProvider.class);
    when(providerMock.getExecutorService()).thenReturn(execMock);
    AgentActivationTransform tested =
        new AgentActivationTransform(stateMock, factoryMock, providerMock);
    AgentTaskListener listenerMock = mock(AgentTaskListener.class);
    tested.addAgentTaskListener(listenerMock);
    assertTrue(tested.getAgentTaskListeners().contains(listenerMock));
  }

  @Test
  public void testOperationOnTask() {
    EnactmentState stateMock = mock(EnactmentState.class);
    AgentFactoryTransform factoryMock = mock(AgentFactoryTransform.class);
    ExecutorService execMock = mock(ExecutorService.class);
    ExecutorProvider providerMock = mock(ExecutorProvider.class);
    when(providerMock.getExecutorService()).thenReturn(execMock);
    AgentActivationTransform tested =
        new AgentActivationTransform(stateMock, factoryMock, providerMock);

    Task task = new Task("task");
    AgentTransform agentMock = mock(AgentTransform.class);
    when(factoryMock.createTransformAgent(task, new HashSet<>())).thenReturn(agentMock);
    try {
      tested.operationOnTask(task);
      verify(execMock).submit(agentMock);
    } catch (StopException e) {
      fail();
    }
  }

  @Test
  public void getTaskFromQueueTest() {
    EnactmentState stateMock = mock(EnactmentState.class);
    AgentFactoryTransform factoryMock = mock(AgentFactoryTransform.class);
    ExecutorService execMock = mock(ExecutorService.class);
    ExecutorProvider providerMock = mock(ExecutorProvider.class);
    when(providerMock.getExecutorService()).thenReturn(execMock);
    AgentActivationTransform tested =
        new AgentActivationTransform(stateMock, factoryMock, providerMock);
    Task task = new Task("task");
    try {
      doReturn(task).when(stateMock).takeTransformTask();
    } catch (InterruptedException e) {
      fail();
    }
    assertEquals(task, tested.getTaskFromBlockingQueue());
  }
}

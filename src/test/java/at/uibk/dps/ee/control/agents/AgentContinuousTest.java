package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Test;
import at.uibk.dps.ee.core.exception.StopException;
import net.sf.opendse.model.Task;

public class AgentContinuousTest {

  protected static class ContinuousMock extends AgentContinuous {

    protected final LinkedBlockingQueue<Task> queue = new LinkedBlockingQueue<>();
    protected int processedTasks = 0;

    @Override
    protected void operationOnTask(Task task) throws StopException {
      try {
        Thread.sleep(250);
      } catch (InterruptedException e) {
        fail();
      }
      processedTasks++;
    }

    protected void putTask(Task task) {
      try {
        queue.put(task);
      } catch (InterruptedException e) {
        fail();
      }
    }

    @Override
    protected Task getTaskFromBlockingQueue() {
      try {
        return queue.take();
      } catch (InterruptedException e) {
        fail();
        return null;
      }
    }
  }

  @Test
  public void test() {
    ExecutorService executor = Executors.newCachedThreadPool();
    ContinuousMock mock = new ContinuousMock();
    Future<Boolean> future = executor.submit(mock);
    assertFalse(future.isDone());
    Task task = new Task("task");
    Task task2 = new Task("task2");
    mock.putTask(task);
    mock.putTask(task2);
    assertFalse(future.isDone());
    assertTrue(mock.processedTasks < 2);
    try {
      Thread.sleep(600);
    } catch (InterruptedException e) {
      fail();
    }
    assertFalse(future.isDone());
    assertEquals(2, mock.processedTasks);
    mock.putTask(new PoisonPill());
    try {
      Thread.sleep(50);
    } catch (InterruptedException e) {
      fail();
    }
    assertTrue(future.isDone());
  }
}

package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import org.junit.Test;
import at.uibk.dps.ee.core.EnactmentState;
import at.uibk.dps.ee.core.exception.StopException;
import net.sf.opendse.model.Task;

public class AgentContinuousTest {

  protected static class WakeUpCall implements Runnable {

    protected final ContinuousMock mock;
    protected final long waitTime = 75;

    protected WakeUpCall(ContinuousMock mock) {
      this.mock = mock;
    }

    @Override
    public void run() {
      try {
        Thread.sleep(waitTime);
        mock.stopped = false;
        mock.reactToStateChange(EnactmentState.PAUSED, EnactmentState.RUNNING);
      } catch (InterruptedException | StopException e) {
        fail();
      }
    }
  }

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
  public void testPauseBehavior() {
    ContinuousMock mock = new ContinuousMock();
    mock.putTask(new PoisonPill());
    mock.paused = true;
    ExecutorService cached = Executors.newCachedThreadPool();
    WakeUpCall wakeUp = new WakeUpCall(mock);
    Instant before = Instant.now();
    cached.submit(wakeUp);
    Future<Boolean> future = cached.submit(mock);
    try {
      future.get();
    } catch (InterruptedException e) {
      fail();
    } catch (ExecutionException e) {
      fail();
    }
    Instant after = Instant.now();
    assertTrue(future.isDone());
    assertTrue(Duration.between(before, after).toMillis() >= wakeUp.waitTime);
  }

  @Test
  public void testReact() {
    ContinuousMock mock = new ContinuousMock();
    assertFalse(mock.paused);
    try {
      mock.reactToStateChange(EnactmentState.RUNNING, EnactmentState.PAUSED);
    } catch (StopException e) {
      fail();
    }
    assertTrue(mock.paused);
    try {
      mock.reactToStateChange(EnactmentState.PAUSED, EnactmentState.RUNNING);
    } catch (StopException e) {
      fail();
    }
    assertFalse(mock.paused);
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

package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;

public class AgentContinuousTest {

	protected class TestAgentContinuous extends AgentContinuous{
		protected int count = 0;
		protected boolean waiting = false;
		protected boolean isSleeping = false;
		
		@Override
		protected void repeatedTask() {
			if (++count == 1000 && waiting) {
				try {
					isSleeping = true;
					this.wait();
				} catch (InterruptedException e) {
					fail();
				}
			}
		}
	}

	@Test
	public void testWaiting() {
		TestAgentContinuous tested = new TestAgentContinuous();
		tested.waiting = true;
		ExecutorService execService = Executors.newCachedThreadPool();
		Future<Boolean> future = execService.submit(tested);
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}
		int curCount = tested.count;
		assertEquals(1000, curCount);
		assertFalse(tested.isStopped());
		assertTrue(tested.isSleeping);
		tested.stop();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}
		int curCount2 = tested.count;
		assertEquals(1000, curCount2);
		assertTrue(tested.isStopped());
		assertTrue(future.isDone());
	}
	
	@Test
	public void testNoWaiting() {
		TestAgentContinuous tested = new TestAgentContinuous();
		ExecutorService execService = Executors.newCachedThreadPool();
		Future<Boolean> future = execService.submit(tested);
		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}
		int curCount = tested.count;
		assertTrue(curCount > 0);
		assertFalse(tested.isStopped());
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}
		assertFalse(future.isDone());
		tested.stop();
		int curCount2 = tested.count;
		assertTrue(curCount2 > curCount);
		assertTrue(tested.isStopped());		
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			fail();
		}
		int curCount3 = tested.count;
		assertEquals(curCount2, curCount3);
		assertTrue(future.isDone());
	}

}

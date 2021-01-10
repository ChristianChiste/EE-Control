package at.uibk.dps.ee.control.runnable;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.core.exception.StopException.StoppingReason;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import net.sf.opendse.model.Task;

import java.util.HashSet;
import java.util.Set;


public class AtomicEnactmentTest {

	protected static class EnactableMock extends EnactableAtomic{
		protected int playCalled = 0;
		protected EnactableMock(Set<EnactableStateListener> stateListeners, Set<String> inputKeys, Task functionNode) {
			super(stateListeners, inputKeys, functionNode);
		}
		@Override
		protected void atomicPlay() throws StopException {
			playCalled++;
		}
		@Override
		protected void myPause() {
		}
	}
	
	protected static class EnactableMockExc extends EnactableAtomic{
		protected EnactableMockExc(Set<EnactableStateListener> stateListeners, Set<String> inputKeys, Task functionNode) {
			super(stateListeners, inputKeys, functionNode);
		}
		@Override
		protected void atomicPlay() throws StopException {
			throw new StopException(StoppingReason.ERROR);
		}
		@Override
		protected void myPause() {
		}
	}
	
	@Test
	public void testCorrect() {
		Task task = new Task("task");
		Set<EnactableStateListener> stateListeners = new HashSet<>();
		Set<String> inputString = new HashSet<>();
		EnactableMock mockEnactable = new EnactableMock(stateListeners, inputString, task);
		mockEnactable.init();
		mockEnactable.setState(State.READY);
		
		AtomicEnactment tested = new AtomicEnactment(mockEnactable, task);
		
		tested.run();
		assertEquals(1, mockEnactable.playCalled);
	}
	
	@Test(expected = IllegalStateException.class)
	public void testIncorrect() {
		Task task = new Task("task");
		Set<EnactableStateListener> stateListeners = new HashSet<>();
		Set<String> inputString = new HashSet<>();
		EnactableMockExc mockEnactable = new EnactableMockExc(stateListeners, inputString, task);
		mockEnactable.init();
		mockEnactable.setState(State.READY);
		
		AtomicEnactment tested = new AtomicEnactment(mockEnactable, task);
		tested.run();
	}
}

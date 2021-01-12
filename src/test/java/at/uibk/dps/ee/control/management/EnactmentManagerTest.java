package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import at.uibk.dps.ee.control.command.Control;
import at.uibk.dps.ee.core.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.enactables.EnactableFactory;

public class EnactmentManagerTest {

	@Test
	public void reactToStateChangePausedRunning() {
		Set<EnactableStateListener> stateListeners = new HashSet<>();
		EnactableFactory factoryMock = mock(EnactableFactory.class);
		DataLogistics mockLogistics = mock(DataLogistics.class);
		Control control = mock(Control.class);
		EnactmentManager tested = new EnactmentManager(stateListeners, mockLogistics, factoryMock, control);
		EnactmentManager spy = spy(tested);
		try {
			spy.reactToStateChange(EnactmentState.PAUSED, EnactmentState.RUNNING);
		} catch (StopException e) {
			fail();
		}

		assertEquals(State.RUNNING, spy.getState());
	}

	@Test
	public void reactToStateChangeRunningPaused() {
		Set<EnactableStateListener> stateListeners = new HashSet<>();
		EnactableFactory factoryMock = mock(EnactableFactory.class);
		Control control = mock(Control.class);
		DataLogistics mockLogistics = mock(DataLogistics.class);
		EnactmentManager tested = new EnactmentManager(stateListeners, mockLogistics, factoryMock, control);
		EnactmentManager spy = spy(tested);
		try {
			spy.reactToStateChange(EnactmentState.RUNNING, EnactmentState.PAUSED);
		} catch (StopException e) {
			fail();
		}

		verify(spy).pause();
		assertEquals(State.PAUSED, spy.getState());
	}
}

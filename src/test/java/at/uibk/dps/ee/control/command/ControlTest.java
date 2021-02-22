package at.uibk.dps.ee.control.command;

import static org.junit.Assert.*;

import org.junit.Test;

import at.uibk.dps.ee.core.ControlStateListener;
import at.uibk.dps.ee.core.EnactmentState;
import at.uibk.dps.ee.core.exception.StopException;

import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.verify;

public class ControlTest {

  @Test
  public void testSetState() {
    Control tested = new Control(false);
    assertFalse(tested.init);
    assertFalse(tested.isInit());
    assertEquals(EnactmentState.PAUSED, tested.enactmentState);
    assertTrue(tested.listeners.isEmpty());
    ControlStateListener listenerMock = mock(ControlStateListener.class);
    tested.addListener(listenerMock);
    tested.setState(EnactmentState.STOPPED);
    assertEquals(EnactmentState.STOPPED, tested.enactmentState);
    assertEquals(EnactmentState.STOPPED, tested.getEnactmentState());
    try {
      verify(listenerMock).reactToStateChange(EnactmentState.PAUSED, EnactmentState.STOPPED);
    } catch (StopException e) {
      fail();
    }
  }

  @Test
  public void testPlay() {
    Control tested = new Control(false);
    tested.enactmentStarted();
    assertTrue(tested.init);
    assertEquals(tested.enactmentState, EnactmentState.RUNNING);
    tested.pause();
    assertEquals(EnactmentState.PAUSED, tested.enactmentState);
    tested.play();
    assertEquals(EnactmentState.RUNNING, tested.enactmentState);
  }

  @Test(expected = IllegalStateException.class)
  public void testPlayNoInit() {
    Control tested = new Control(false);
    tested.play();
  }
}

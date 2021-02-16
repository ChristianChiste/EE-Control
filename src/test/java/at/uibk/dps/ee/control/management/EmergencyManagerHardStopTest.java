package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.core.exception.StopException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;;

public class EmergencyManagerHardStopTest {

  @Test
  public void test() {
    EmergencyManagerHardStop tested = new EmergencyManagerHardStop();
    EnactmentAgents mainMock = mock(EnactmentAgents.class);
    
    assertTrue(tested.mainAgent.isEmpty());
    tested.registerMain(mainMock);
    assertEquals(mainMock, tested.mainAgent.get());
    
    StopException exc = new StopException("MyMessage", new IllegalArgumentException("Other message"));
    String additionalInformation = "Additional information";
    
    assertFalse(tested.isEmergency());
    tested.reactToException(exc, additionalInformation);
    assertTrue(tested.isEmergency());
    assertEquals(exc, tested.exc.get());
    assertEquals(additionalInformation, tested.additionalInformation);
    verify(mainMock).wakeUp();
    
    try {
      tested.emergencyProtocol();
      fail();
    }catch(StopException stopExc) {
      assertEquals(exc, stopExc.getInitialException());
    }
  }
}

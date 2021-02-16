package at.uibk.dps.ee.control.management;

import java.util.Optional;
import at.uibk.dps.ee.core.exception.StopException;

/**
 * An implementation of the {@link EmergencyManager} which reacts to each
 * exception by ending the enactment process via a
 * 
 * @author Fedor Smirnov
 *
 */
public class EmergencyManagerHardStop implements EmergencyManager {

  protected boolean emergencyState = false;
  protected Optional<Exception> exc = Optional.empty();
  protected String additionalInformation;
  protected Optional<EnactmentAgents> mainAgent = Optional.empty();

  @Override
  public void registerMain(EnactmentAgents mainAgent) {
    this.mainAgent = Optional.of(mainAgent);
  }

  @Override
  public void reactToException(Exception exc, String additionalInformation) {
    emergencyState = true;
    this.exc = Optional.of(exc);
    this.additionalInformation = additionalInformation;
    mainAgent.get().wakeUp();
  }

  @Override
  public boolean isEmergency() {
    return emergencyState;
  }

  @Override
  public void emergencyProtocol() throws StopException{
    String message = additionalInformation + "\n";
    message += exc.get().getMessage();
    throw new StopException(message, exc.get());
  }
}

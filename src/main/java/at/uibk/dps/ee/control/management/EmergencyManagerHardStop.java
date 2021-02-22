package at.uibk.dps.ee.control.management;

import java.io.PrintWriter;
import java.io.StringWriter;
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

  protected boolean emergencyState;
  protected Optional<Exception> exc = Optional.empty();
  protected String additionalInformation;
  protected Optional<EnactmentAgent> mainAgent = Optional.empty();

  @Override
  public void registerMain(final EnactmentAgent mainAgent) {
    this.mainAgent = Optional.of(mainAgent);
  }

  @Override
  public void reactToException(final Exception exc, final String additionalInformation) {
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
  public void emergencyProtocol() throws StopException {
    String message = additionalInformation + "\n";
    message += exc.get().getMessage() + "\n";
    // convert the exc stack trace to string
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    exc.get().printStackTrace(printWriter);
    message += stringWriter.toString();
    throw new StopException(message, exc.get());
  }
}

package at.uibk.dps.ee.control.management;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Task;

public class EmergencyManagerReact implements EmergencyManager{

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
	    final Enactable enactable = PropertyServiceFunction.getEnactable(new Task(additionalInformation));
	    enactable.setState(State.SCHEDULABLE);
	    mainAgent.get().enactmentState.schedulableTasks.add(new Task(additionalInformation));
	    if(mainAgent.get().enactmentStopped)
	    	mainAgent.get().wakeUp();
	  }

	  @Override
	  public boolean isEmergency() {
	    return emergencyState;
	  }

	  @Override
	  public void emergencyProtocol() throws StopException {
	  }

}

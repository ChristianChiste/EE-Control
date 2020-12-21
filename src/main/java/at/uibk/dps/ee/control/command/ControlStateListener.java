package at.uibk.dps.ee.control.command;

import at.uibk.dps.ee.core.enactable.EnactmentState;
import at.uibk.dps.ee.core.exception.StopException;

/**
 * 
 * Interface for all classes which alter their behaviour depending on the state of
 * the enactment or have to react to control state changes.
 * 
 * @author Fedor Smirnov
 *
 */
public interface ControlStateListener {

	/**
	 * Triggered on the change of the state of the enactment. This method should
	 * contain the reaction required in the case of the state change described by
	 * the method parameters.
	 * 
	 * @param previousState the previous state of the enactment
	 * @param currentState  the now active state of the enactment
	 */
	void reactToStateChange(EnactmentState previousState, EnactmentState currentState) throws StopException;

}

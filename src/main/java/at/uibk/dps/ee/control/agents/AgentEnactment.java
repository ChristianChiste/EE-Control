package at.uibk.dps.ee.control.agents;

import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.exception.StopException;

/**
 * The {@link AgentEnactment} is responsible for the execution of a single
 * enactable.
 * 
 * @author Fedor Smirnov
 *
 */
public class AgentEnactment implements Agent {

	protected final Enactable enactable;

	public AgentEnactment(Enactable enactable) {
		this.enactable = enactable;
	}

	@Override
	public Boolean call() {
		try {
			enactable.play();
		} catch (StopException e) {
			return false;
		}
		return true;
	}
}

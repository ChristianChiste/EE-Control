package at.uibk.dps.ee.control.agents;

import at.uibk.dps.ee.core.enactable.Enactable;

/**
 * The default factory for the creation of {@link AgentEnactment}s.
 * 
 * @author Fedor Smirnov
 */
public class AgentEnactmentFactory {

	/**
	 * Creates an agent for the enactment of the given enactable.
	 * 
	 * @param enactable the given enactable
	 * @return an agent for the enactment of the given enactable
	 */
	public AgentEnactment createAgentEnactment(Enactable enactable) {
		return new AgentEnactment(enactable);
	}
}

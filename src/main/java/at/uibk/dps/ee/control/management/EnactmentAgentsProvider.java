package at.uibk.dps.ee.control.management;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.core.EnactableProvider;
import at.uibk.dps.ee.core.enactable.EnactableRoot;

/**
 * Guice interface to provide the enactment control classes based on agents to
 * the rest of the EE.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class EnactmentAgentsProvider implements EnactableProvider {

	protected final EnactmentAgents enactmentAgents;
	
	@Inject
	public EnactmentAgentsProvider(EnactmentAgents enactmentAgents) {
		this.enactmentAgents = enactmentAgents;
	}
	
	@Override
	public EnactableRoot getEnactableApplication() {
		return enactmentAgents;
	}
}

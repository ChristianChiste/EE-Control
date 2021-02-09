package at.uibk.dps.ee.control.management;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.core.EnactableProvider;
import at.uibk.dps.ee.core.enactable.EnactableRoot;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;

/**
 * Guice interface to provide the enactment control classes based on agents to
 * the rest of the EE.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class EnactmentAgentsProvider implements EnactableProvider {

	protected final EnactableRoot rootEnactableAgents;
	
	@Inject
	public EnactmentAgentsProvider(EnactmentAgents enactmentAgents, Set<EnactableStateListener> listeners) {
		this.rootEnactableAgents = new EnactableRoot(listeners, enactmentAgents);
	}
	
	@Override
	public EnactableRoot getEnactableApplication() {
		return rootEnactableAgents;
	}
}

package at.uibk.dps.ee.control.management;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.core.EnactableProvider;
import at.uibk.dps.ee.core.enactable.EnactableRoot;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;

/**
 * The {@link EnactableManagerProvider} provides the manager which is used as
 * the root enactable.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class EnactableManagerProvider implements EnactableProvider {

	protected final EnactmentManager manager;

	@Inject
	public EnactableManagerProvider(Set<EnactableStateListener> stateListeners, EnactmentGraphProvider graphProvider) {
		this.manager = new EnactmentManager(stateListeners, graphProvider);
	}

	@Override
	public EnactableRoot getEnactableApplication() {
		return this.manager;
	}
}

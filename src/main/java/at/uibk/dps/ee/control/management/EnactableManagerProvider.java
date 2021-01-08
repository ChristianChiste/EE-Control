package at.uibk.dps.ee.control.management;

import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.control.command.Control;
import at.uibk.dps.ee.core.EnactableProvider;
import at.uibk.dps.ee.core.enactable.EnactableRoot;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
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

	/**
	 * Constructor used for the dynamic dependency injection.
	 * 
	 * @param stateListeners the enactable state listeners added via guice
	 * @param graphProvider  the object providing the {@link EnactmentGraph}
	 * @param control        the control object for the implementation of user
	 *                       commands
	 */
	@Inject
	public EnactableManagerProvider(final Set<EnactableStateListener> stateListeners,
			final EnactmentGraphProvider graphProvider, final Control control) {
		this.manager = new EnactmentManager(stateListeners, graphProvider, control);
	}

	@Override
	public EnactableRoot getEnactableApplication() {
		return this.manager;
	}
}

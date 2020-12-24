package at.uibk.dps.ee.control.modules;

import at.uibk.dps.ee.control.management.EnactableManagerProvider;
import at.uibk.dps.ee.core.EnactableProvider;
import at.uibk.dps.ee.guice.modules.EeModule;

/**
 * The {@link EnactmentManagementModule} determines which root enactable is used
 * for the management of the enactment.
 * 
 * @author Fedor Smirnov
 *
 */
public class EnactmentManagementModule extends EeModule {

	@Override
	protected void config() {
		bind(EnactableProvider.class).to(EnactableManagerProvider.class);
	}
}

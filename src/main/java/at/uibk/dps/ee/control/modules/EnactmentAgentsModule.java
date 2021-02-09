package at.uibk.dps.ee.control.modules;

import at.uibk.dps.ee.control.command.Control;
import at.uibk.dps.ee.control.management.EnactmentAgentsProvider;
import at.uibk.dps.ee.core.EnactableProvider;
import at.uibk.dps.ee.guice.modules.EeModule;

/**
 * The module configuring the enactment agents.
 * 
 * @author Fedor Smirnov
 *
 */
public class EnactmentAgentsModule extends EeModule {

  @Override
  protected void config() {
    bind(EnactableProvider.class).to(EnactmentAgentsProvider.class);
    addEnactmentStateListener(Control.class);
  }
}

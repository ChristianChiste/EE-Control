package at.uibk.dps.ee.control.modules;

import at.uibk.dps.ee.control.command.Control;
import at.uibk.dps.ee.control.management.EnactmentAgentProvider;
import at.uibk.dps.ee.core.EnactableProvider;
import at.uibk.dps.ee.guice.modules.EeModule;

/**
 * The module configuring the main enactment agent.
 * 
 * @author Fedor Smirnov
 *
 */
public class EnactmentAgentModule extends EeModule {

  @Override
  protected void config() {
    bind(EnactableProvider.class).to(EnactmentAgentProvider.class);
    addEnactmentStateListener(Control.class);
  }
}

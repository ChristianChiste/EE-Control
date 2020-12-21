package at.uibk.dps.ee.control.elemental;

import java.util.Set;

import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import net.sf.opendse.model.Task;

/**
 * The {@link EnactableFactory} is used for the creation of elemental
 * {@link Enactable}s.
 * 
 * @author Fedor Smirnov
 *
 */
public class EnactableFactory {

	protected final Set<EnactableStateListener> stateListeners;

	public EnactableFactory(Set<EnactableStateListener> stateListeners) {
		this.stateListeners = stateListeners;
	}

	/**
	 * Creates an enactable which can be used to perform the enactment modeled by
	 * the provided function node.
	 * 
	 * @param functionNode the provided function node
	 * @return an enactable which can be used to perform the enactment modeled by
	 *         the provided function node
	 */
	public Enactable createEnactable(Task functionNode) {
		throw new IllegalStateException("Enactable Creation not yet implemented.");
	}

}

package at.uibk.dps.ee.control.elemental;

import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;

import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableBuilder;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.FunctionType;
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
	protected final Set<EnactableBuilder> enactableBuilders;

	public EnactableFactory(Set<EnactableStateListener> stateListeners, Set<EnactableBuilder> enactableBuilders) {
		this.stateListeners = stateListeners;
		this.enactableBuilders = enactableBuilders;
	}

	/**
	 * Creates an enactable which can be used to perform the enactment modeled by
	 * the provided function node.
	 * 
	 * @param functionNode the provided function node
	 * @param inputMap     the map containing the keys (but not yet the content) of
	 *                     the inputs of the function
	 * @return an enactable which can be used to perform the enactment modeled by
	 *         the provided function node
	 */
	public EnactableAtomic createEnactable(Task functionNode, Map<String, JsonElement> inputMap) {
		// look for the right builder
		FunctionType funcType = PropertyServiceFunction.getType(functionNode);
		for (EnactableBuilder builder : enactableBuilders) {
			if (builder.getType().equals(funcType)) {
				return builder.buildEnactable(functionNode, inputMap, stateListeners);
			}
		}
		throw new IllegalStateException("No builder provided for enactables of type " + funcType.name());
	}
}

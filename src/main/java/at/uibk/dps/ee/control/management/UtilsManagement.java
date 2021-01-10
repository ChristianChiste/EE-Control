package at.uibk.dps.ee.control.management;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Static method container offering utility methods used throughout the
 * enactment management process.
 * 
 * @author Fedor Smirnov
 */
public final class UtilsManagement {

	/**
	 * No constructor.
	 */
	private UtilsManagement() {
	}

	/**
	 * Generates a map mapping each function task of the given enactment graph onto
	 * the corresponding enactable.
	 * 
	 * @param graph   the enactment graph
	 * @param factory the factory used to create enactables
	 * @return a map mapping each function task of the given enactment graph onto
	 *         the corresponding enactable
	 */
	public static Map<Task, EnactableAtomic> generateTask2EnactableMap(final EnactmentGraph graph,
			final EnactableFactory factory) {
		final Map<Task, EnactableAtomic> result = new ConcurrentHashMap<>();
		for (final Task task : graph) {
			if (TaskPropertyService.isProcess(task)) {
				final Set<String> inputKeys = UtilsManagement.getInputKeys(task, graph);
				final EnactableAtomic enactable = factory.createEnactable(task, inputKeys);
				PropertyServiceFunction.setEnactableState(task, enactable.getState());
				result.put(task, enactable);
			}
		}
		return result;
	}

	/**
	 * Returns the input key set for the provided function task.
	 * 
	 * @param task  the provided function task
	 * @param graph the enactment graph
	 * @return the set of the string keys the enactable associated with the provided
	 *         task uses to access the input data from the provided Json object
	 */
	public static Set<String> getInputKeys(final Task task, final EnactmentGraph graph) {
		if (TaskPropertyService.isCommunication(task)) {
			throw new IllegalArgumentException("Task " + task.getId() + " does not model a function.");
		}
		final Set<String> result = new HashSet<>();
		for (final Dependency inEdge : graph.getInEdges(task)) {
			if (PropertyServiceDependency.getType(inEdge).equals(TypeDependency.Data)
					|| PropertyServiceDependency.getType(inEdge).equals(TypeDependency.ControlIf)) {
				result.add(PropertyServiceDependency.getJsonKey(inEdge));
			} else {
				throw new IllegalStateException("The dependency " + inEdge.getId() + " has an unknown type.");
			}
		}
		return result;
	}

	/**
	 * Returns the leaf nodes of the given graph.
	 * 
	 * @param graph the enactment graph
	 * @return the leaf nodes of the given graph
	 */
	public static Set<Task> getLeafNodes(final EnactmentGraph graph) {
		final Set<Task> result = new HashSet<>();
		for (final Task task : graph) {
			if (TaskPropertyService.isCommunication(task) && PropertyServiceData.isLeaf(task)) {
				if (!graph.getOutEdges(task).isEmpty()) {
					throw new IllegalArgumentException("The leaf node " + task.getId() + " has out edges.");
				}
				result.add(task);
			}
		}
		return result;
	}
}

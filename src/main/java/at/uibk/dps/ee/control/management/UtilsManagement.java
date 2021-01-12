package at.uibk.dps.ee.control.management;

import java.util.HashSet;
import java.util.Set;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
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
	 * For each function task in the graph, this method generates the corrsponsing
	 * enactable and annotates it to the task.
	 * 
	 * @param graph   the enactment graph
	 * @param factory the factory used to create enactables
	 */
	public static void annotateTaskEnactables(final EnactmentGraph graph, final EnactableFactory factory) {
		for (final Task task : graph) {
			if (TaskPropertyService.isProcess(task)) {
				final Set<String> inputKeys = UtilsManagement.getInputKeys(task, graph);
				final EnactableAtomic enactable = factory.createEnactable(task, inputKeys);
				PropertyServiceFunction.setEnactable(task, enactable);
			}
		}
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
}

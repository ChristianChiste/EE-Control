package at.uibk.dps.ee.control.management;

import java.util.HashSet;
import java.util.Set;

import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
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
	 * Returns the input key set for the provided function task.
	 * 
	 * @param task  the provided function task
	 * @param graph the enactment graph
	 * @return the set of the string keys the enactable associated with the provided
	 *         task uses to access the input data from the provided Json object
	 */
	public static Set<String> getInputKeys(Task task, EnactmentGraph graph) {
		if (TaskPropertyService.isCommunication(task)) {
			throw new IllegalArgumentException("Task " + task.getId() + " does not model a function.");
		}
		Set<String> result = new HashSet<>();
		for (Dependency inEdge : graph.getInEdges(task)) {
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

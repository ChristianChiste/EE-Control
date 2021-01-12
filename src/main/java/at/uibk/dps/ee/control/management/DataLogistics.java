package at.uibk.dps.ee.control.management;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependencyControlIf;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceData.NodeType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Class providing the operations necessary for the management of the data
 * annotated within the EnactmentGraph.
 * 
 * @author Fedor Smirnov
 *
 */
public class DataLogistics {

	protected final EnactmentGraph graph;
	protected final Set<Task> leafNodes;

	@Inject
	public DataLogistics(EnactmentGraphProvider graphProvider, EnactableFactory factory) {
		this.graph = graphProvider.getEnactmentGraph();
		this.leafNodes = getLeafNodes(graph);
		UtilsManagement.annotateTaskEnactables(graph, factory);
	}

	/**
	 * Returns true if all leaf nodes have been annotated with content.
	 * 
	 * @return true if all leaf nodes have been annotated with content
	 */
	public boolean isWfFinished() {
		for (final Task task : leafNodes) {
			if (!PropertyServiceData.isDataAvailable(task)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the leaf nodes of the given graph.
	 * 
	 * @param graph the enactment graph
	 * @return the leaf nodes of the given graph
	 */
	protected Set<Task> getLeafNodes(EnactmentGraph graph) {
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

	/**
	 * Annotates the graph nodes with the data available at the start of the
	 * enactment (i.e., the wf input and the constant data).
	 * 
	 */
	public void initData(JsonObject wfInput) {
		for (Task node : graph) {
			if (TaskPropertyService.isCommunication(node)) {
				if (PropertyServiceData.isRoot(node)) {
					// root nodes
					initRootNodeContent(node, wfInput);
				} else if (PropertyServiceData.getNodeType(node).equals(NodeType.Constant)) {
					// constant nodes
					initConstantDataNode(node);
				}
			}
		}
	}

	/**
	 * Reads the wf output from the content of the leaf nodes.
	 * 
	 * @return the wf output read from the content of the leaf nodes
	 */
	public JsonObject readWfOutput() {
		if (!isWfFinished()) {
			throw new IllegalStateException("WF output requested before the WF was fully processed.");
		}
		JsonObject result = new JsonObject();
		// iterate the leaves
		for (Task task : graph) {
			if (TaskPropertyService.isCommunication(task) && PropertyServiceData.isLeaf(task)) {
				if (!PropertyServiceData.isDataAvailable(task)) {
					throw new IllegalStateException("No data in the leaf node " + task.getId());
				}
				String jsonKey = PropertyServiceData.getJsonKey(task);
				result.add(jsonKey, PropertyServiceData.getContent(task));
			}
		}
		return result;
	}

	/**
	 * Initializes the given constant node by notiying all of its function
	 * successors.
	 * 
	 * @param constantNode the given contant node.
	 */
	protected void initConstantDataNode(Task constantNode) {
		annotateDataConsumers(constantNode);
	}

	/**
	 * Initializes the content of the given root node and sets the input of its
	 * function successors.
	 * 
	 * @param rootNode the given root node
	 */
	protected void initRootNodeContent(Task rootNode, JsonObject wfInput) {
		String key = PropertyServiceData.getJsonKey(rootNode);
		if (!wfInput.has(key)) {
			throw new IllegalStateException("The input " + key + " was not provided in the WF input.");
		}
		JsonElement content = wfInput.get(key);
		PropertyServiceData.setContent(rootNode, content);
		annotateDataConsumers(rootNode);
	}

	/**
	 * Annotates the content of the given node in all enactables of its function
	 * successors.
	 * 
	 * @param dataNode the given node
	 */
	protected void annotateDataConsumers(Task dataNode) {
		for (Dependency outEdge : graph.getOutEdges(dataNode)) {
			if (PropertyServiceDependency.getType(outEdge).equals(TypeDependency.Data)) {
				annotateDataConsumer(dataNode, outEdge);
			} else if (PropertyServiceDependency.getType(outEdge).equals(TypeDependency.ControlIf)) {
				boolean decisionVariable = PropertyServiceData.getContent(dataNode).getAsBoolean();
				if (PropertyServiceDependencyControlIf.getActivation(outEdge) == decisionVariable) {
					annotateDataConsumer(dataNode, outEdge);
				}
			} else {
				throw new IllegalStateException("The edge " + outEdge.getId() + " has an unknown type.");
			}
		}
	}

	/**
	 * Annotates the consumer connected to the given data node by the provided edge.
	 * 
	 * @param dataNode       the given data node
	 * @param edgeToConsumer the provided edge
	 */
	protected void annotateDataConsumer(Task dataNode, Dependency edgeToConsumer) {
		Task functionNode = graph.getDest(edgeToConsumer);
		Enactable enactable = PropertyServiceFunction.getEnactable(functionNode);
		if (!(enactable instanceof EnactableAtomic)) {
			throw new IllegalStateException(
					"The enactable annotated on the task " + functionNode.getId() + " is not atomic");
		}
		EnactableAtomic atomic = (EnactableAtomic) enactable;
		String jsonKey = PropertyServiceDependency.getJsonKey(edgeToConsumer);
		atomic.setInput(jsonKey, PropertyServiceData.getContent(dataNode));
	}

	/**
	 * Reflects the results of the enactable execution by annotating the data node
	 * successors of the corresponding function node.
	 * 
	 * @param enactable the enactable which was executed
	 */
	public void annotateExecutionResults(EnactableAtomic enactable) {
		Task task = enactable.getFunctionNode();
		JsonObject result = enactable.getJsonResult();
		for (Dependency outEdge : graph.getOutEdges(task)) {
			if (PropertyServiceDependency.getType(outEdge).equals(TypeDependency.Data)) {
				Task dataNode = graph.getDest(outEdge);
				String jsonKey = PropertyServiceDependency.getJsonKey(outEdge);
				if (!result.has(jsonKey)) {
					throw new IllegalStateException("The enactment of task " + task.getId()
							+ " did not provide a result for the key " + jsonKey);
				}
				JsonElement content = result.get(jsonKey);
				PropertyServiceData.setContent(dataNode, content);
				annotateDataConsumers(dataNode);
			}
		}
	}
}

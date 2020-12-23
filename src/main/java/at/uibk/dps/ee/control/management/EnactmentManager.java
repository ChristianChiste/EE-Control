package at.uibk.dps.ee.control.management;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.control.elemental.AtomicEnactment;
import at.uibk.dps.ee.control.elemental.EnactableFactory;
import at.uibk.dps.ee.core.ControlStateListener;
import at.uibk.dps.ee.core.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.EnactableRoot;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableBuilder;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * The {@link EnactmentManager} maintains the {@link EnactmentGraph}, monitors
 * the state of the elemental enactables, and triggers their execution as soon
 * as the data is available.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class EnactmentManager extends EnactableRoot implements ControlStateListener, EnactableStateListener {

	protected final EnactmentGraph graph;
	protected final Map<Task, EnactableAtomic> task2EnactableMap;
	protected final Map<Enactable, Task> enactable2TaskMap;
	protected final Set<Task> leafNodes;

	protected final EnactableFactory factory;

	// Set of tasks which can be started
	protected final Set<Task> readyTasks = new HashSet<>();

	@Inject
	public EnactmentManager(Set<EnactableStateListener> stateListeners, EnactmentGraph graph,
			Set<EnactableBuilder> enactableBuilders) {
		super(stateListeners);
		stateListeners.add(this);
		this.graph = graph;
		this.factory = new EnactableFactory(stateListeners, enactableBuilders);
		this.task2EnactableMap = generateTask2EnactableMap();
		this.enactable2TaskMap = generateEnactable2TaskMap();
		this.leafNodes = getLeafNodes();
	}

	/**
	 * Generates a map mapping function tasks on their corresponding enactables.
	 * 
	 * @return a map mapping function tasks on their corresponding enactables
	 */
	protected Map<Task, EnactableAtomic> generateTask2EnactableMap() {
		Map<Task, EnactableAtomic> result = new HashMap<>();
		for (Task task : graph) {
			if (TaskPropertyService.isProcess(task)) {
				Map<String, JsonElement> inputMap = getInputMap(task);
				result.put(task, factory.createEnactable(task, inputMap));
			}
		}
		return result;
	}

	/**
	 * Generates a map mapping the enactables to the tasks.
	 * 
	 * @return a map mapping the enactables to the tasks
	 */
	protected Map<Enactable, Task> generateEnactable2TaskMap() {
		Map<Enactable, Task> result = new HashMap<>();
		for (Entry<Task, EnactableAtomic> entry : task2EnactableMap.entrySet()) {
			result.put(entry.getValue(), entry.getKey());
		}
		return result;
	}

	/**
	 * Gathers the leaf nodes from the graph
	 * 
	 * @return the leaf nodes from the graph
	 */
	protected Set<Task> getLeafNodes() {
		Set<Task> result = new HashSet<>();
		for (Task task : graph) {
			if (TaskPropertyService.isCommunication(task) && PropertyServiceData.isLeaf(task)) {
				result.add(task);
			}
		}
		return result;
	}

	/**
	 * Returns true if all leaf nodes have been annotated with content.
	 * 
	 * @return true if all leaf nodes have been annotated with content
	 */
	protected boolean wfFinished() {
		for (Task task : leafNodes) {
			if (!PropertyServiceData.isDataAvailable(task)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns an input map (with null values) for the given task node.
	 * 
	 * @param task the given task node
	 * @return an input map (with null values) for the given task node
	 */
	protected Map<String, JsonElement> getInputMap(Task task) {
		Map<String, JsonElement> result = new HashMap<>();
		for (Dependency inEdge : graph.getInEdges(task)) {
			if (PropertyServiceDependency.getType(inEdge).equals(TypeDependency.Data)) {
				String key = PropertyServiceDependency.getJsonKey(inEdge);
				result.put(key, null);
			}
		}
		return result;
	}

	@Override
	public void reactToStateChange(EnactmentState previousState, EnactmentState currentState) throws StopException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void myPlay() throws StopException {
		while (!wfFinished()) {
			if (!readyTasks.isEmpty()) {
				// enact an enactable and annotate the results
				enactReadyTasks();
			} else {
				// wait to be woken up by a worker thread
				try {
					wait();
				} catch (InterruptedException e) {
					throw new IllegalStateException("Enactment manager interrupted while waiting.");
				}
			}
		}
	}

	/**
	 * Creates a callable for each task which is ready. The callable triggers the
	 * execution of the enactable and then annotates the output data.
	 * 
	 * @param readyTasks the tasks which are ready
	 */
	protected void enactReadyTasks() {
		Set<Task> handled = new HashSet<>();
		for (Task task : readyTasks) {
			EnactableAtomic enactable = task2EnactableMap.get(task);
			AtomicEnactment atomicEnactment = new AtomicEnactment(enactable, task);
			Thread thread = new Thread(atomicEnactment);
			thread.start();
			handled.add(task);
		}
		readyTasks.removeAll(handled);
	}

	@Override
	protected void myPause() {
		// TODO Has to be implemented

	}

	@Override
	protected void myInit() {
		// annotates the root nodes of the graph with the data from the provided json
		// object
		for (Task node : graph) {
			if (TaskPropertyService.isCommunication(node) && PropertyServiceData.isRoot(node)) {
				String key = PropertyServiceData.getJsonKey(node);
				if (!wfInput.has(key)) {
					throw new IllegalStateException("The input " + key + " was not provided in the WF input.");
				}
				JsonElement content = wfInput.get(key);
				PropertyServiceData.setContent(node, content);
			}
		}
	}

	@Override
	public JsonObject getOutput() {
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
	 * Annotates the results of the task execution on the successor data nodes.
	 * 
	 * @param enactable the enactable which is finished with its execution
	 */
	protected void annotateExecutionResults(EnactableAtomic enactable) {
		Task task = enactable2TaskMap.get(enactable);
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
				setDataNodeContent(dataNode, content);
			}
		}
	}

	/**
	 * Sets the content of the data node and sets the corresponding value in all of
	 * its successor enactables.
	 * 
	 * @param dataNode the given node
	 * @param content  the content to set
	 */
	protected void setDataNodeContent(Task dataNode, JsonElement content) {
		PropertyServiceData.setContent(dataNode, content);
		for (Dependency outEdge : graph.getOutEdges(dataNode)) {
			if (PropertyServiceDependency.getType(outEdge).equals(TypeDependency.Data)) {
				Task functionNode = graph.getDest(outEdge);
				EnactableAtomic enactable = task2EnactableMap.get(functionNode);
				String jsonKey = PropertyServiceDependency.getJsonKey(outEdge);
				enactable.setInput(jsonKey, content);
			}
		}
	}

	@Override
	public void enactableStateChanged(Enactable enactable, State previousState, State currentState) {
		if (previousState.equals(State.WAITING) && currentState.equals(State.READY)) {
			// Enactable is ready => add its task to the ready list
			readyTasks.add(enactable2TaskMap.get(enactable));
			this.notifyAll();
		} else if (enactable instanceof EnactableAtomic && previousState.equals(State.RUNNING)
				&& currentState.equals(State.FINISHED)) {
			// Enactable finished execution => annotate the results
			annotateExecutionResults((EnactableAtomic) enactable);
		}
	}

	@Override
	protected void myReset() {
		// TODO Auto-generated method stub
	}
}

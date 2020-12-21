package at.uibk.dps.ee.control.management;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.control.command.ControlStateListener;
import at.uibk.dps.ee.control.elemental.EnactableFactory;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.EnactableRoot;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.enactable.EnactmentState;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
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
public class EnactmentManager extends EnactableRoot implements ControlStateListener {

	protected final EnactmentGraph graph;
	protected final Map<Task, Enactable> task2EnactableMap;

	protected final EnactableFactory factory;

	@Inject
	public EnactmentManager(Set<EnactableStateListener> stateListeners, EnactmentGraph graph) {
		super(stateListeners);
		this.graph = graph;
		this.factory = new EnactableFactory(stateListeners);
		this.task2EnactableMap = generateTask2EnactableMap();
	}

	/**
	 * Generates a map mapping function tasks on their corresponding enactables.
	 * 
	 * @return a map mapping function tasks on their corresponding enactables
	 */
	protected Map<Task, Enactable> generateTask2EnactableMap() {
		Map<Task, Enactable> result = new HashMap<>();
		for (Task task : graph) {
			if (TaskPropertyService.isProcess(task)) {
				result.put(task, factory.createEnactable(task));
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
		// TODO Auto-generated method stub
		throw new IllegalStateException("Not yet implemented.");
	}

	@Override
	protected void myPause() {
		// TODO Has to be implemented

	}

	@Override
	protected void myInit() {
		// annotates the root nodes of the graph with the data from the provided json
		// object

	}

	@Override
	public JsonObject getOutput() {
		// read the output from the graph
		throw new IllegalStateException("Not yet implemented.");
	}

}

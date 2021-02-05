package at.uibk.dps.ee.control.management;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.uibk.dps.ee.control.agents.AgentActivationEnactment;
import at.uibk.dps.ee.control.agents.AgentActivationExtraction;
import at.uibk.dps.ee.control.agents.AgentActivationScheduling;
import at.uibk.dps.ee.control.agents.AgentActivationTransmission;
import at.uibk.dps.ee.control.agents.AgentFactoryActivation;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.core.enactable.EnactableRoot;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import net.sf.opendse.model.Task;

/**
 * The {@link EnactableAgents}
 * 
 * @author Fedor Smirnov
 */
public class EnactableAgents extends EnactableRoot {

	protected final AgentActivationEnactment scheduledQueueMonitor;
	protected final AgentActivationExtraction finishedQueueMonitor;
	protected final AgentActivationTransmission availableDataQueueMonitor;
	protected final AgentActivationScheduling launchableQueueMonitor;

	protected final GraphAccess graphAccess;
	protected final EnactmentState enactmentState;
	protected final ExecutorService executor;

	protected EnactableAgents(Set<EnactableStateListener> stateListeners, AgentFactoryActivation agentFactory,
			GraphAccess graphAccess, EnactmentState enactmentState, ExecutorService executor) {
		super(stateListeners);
		this.scheduledQueueMonitor = agentFactory.createScheduledQueueMonitor();
		this.finishedQueueMonitor = agentFactory.createFinishedQueueMonitor();
		this.availableDataQueueMonitor = agentFactory.createAvalDataQueueMonitor(this);
		this.launchableQueueMonitor = agentFactory.createLaunchableQueueMonitor();
		this.graphAccess = graphAccess;
		this.enactmentState = enactmentState;
		this.executor = executor;
	}

	@Override
	protected void myPlay() throws StopException {
		// start up the queue monitors and go to sleep
		executor.submit(scheduledQueueMonitor);
		executor.submit(finishedQueueMonitor);
		executor.submit(availableDataQueueMonitor);
		executor.submit(launchableQueueMonitor);
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
				throw new IllegalArgumentException("Root enactable interrupted.", e);
			}
		}
	}

	@Override
	protected void myPause() {
		// TODO implement the pause behavior within the activation agents

	}

	@Override
	protected void myReset() {
		// TODO Auto-generated method stub

	}

	@Override
	protected void myInit() {
		// get the input, annotate the root nodes, and add them to the
		// availableDataQueue
		graphAccess.getRootDataNodes().forEach(rootNode -> processRootNode(rootNode));
		// the constant data is available from the start
		graphAccess.getConstantDataNodes().forEach(constantData -> enactmentState.putAvailableData(constantData));
	}

	/**
	 * Processes the given root node by annotating it with the entry from the json
	 * input
	 * 
	 * @param rootNode the given root node
	 */
	protected void processRootNode(Task rootNode) {
		String jsonKey = PropertyServiceData.getJsonKey(rootNode);
		JsonElement content = Optional.ofNullable(jsonInput.get(jsonKey)).orElseThrow(
				() -> new IllegalArgumentException("No entry with the key " + jsonKey + " in the WF input."));
		PropertyServiceData.setContent(rootNode, content);
		enactmentState.putAvailableData(rootNode);
	}

	/**
	 * Called when the workflow execution is finished.
	 */
	public void finishWfExecution() {
		// get the output and write it into the json result
		// stop the continuous agents and end the enactment
		graphAccess.getLeafDataNodes().forEach(leafNode -> processLeafNode(leafNode));
		stopMonitors();
		// back to the main thread, which wakes up and terminates
		synchronized (this) {
			notifyAll();
		}
	}

	/**
	 * Stop the continuous agents.
	 */
	protected void stopMonitors() {
		scheduledQueueMonitor.stop();
		finishedQueueMonitor.stop();
		launchableQueueMonitor.stop();
		availableDataQueueMonitor.stop();
	}

	/**
	 * Reads the content of the given leaf node and writes it into the JSON result.
	 * 
	 * @param leafNode the given leaf node
	 */
	protected void processLeafNode(Task leafNode) {
		String jsonKey = PropertyServiceData.getJsonKey(leafNode);
		JsonObject result = Optional.ofNullable(jsonResult).orElseGet(() -> new JsonObject());
		result.add(jsonKey, PropertyServiceData.getContent(leafNode));
	}
}

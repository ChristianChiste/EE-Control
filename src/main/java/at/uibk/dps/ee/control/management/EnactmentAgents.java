package at.uibk.dps.ee.control.management;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;

import at.uibk.dps.ee.control.agents.AgentActivationEnactment;
import at.uibk.dps.ee.control.agents.AgentActivationExtraction;
import at.uibk.dps.ee.control.agents.AgentActivationScheduling;
import at.uibk.dps.ee.control.agents.AgentActivationTransmission;
import at.uibk.dps.ee.control.agents.AgentFactoryActivation;
import at.uibk.dps.ee.control.agents.AgentTaskListener;
import at.uibk.dps.ee.control.agents.PoisonPill;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.core.enactable.EnactmentFunction;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import net.sf.opendse.model.Task;

/**
 * The {@link EnactmentAgents}
 * 
 * @author Fedor Smirnov
 */
public class EnactmentAgents implements EnactmentFunction, AgentTaskListener {

  protected final AgentActivationEnactment scheduledQueueMonitor;
  protected final AgentActivationExtraction finishedQueueMonitor;
  protected final AgentActivationTransmission availableDataQueueMonitor;
  protected final AgentActivationScheduling launchableQueueMonitor;

  protected final GraphAccess graphAccess;
  protected final EnactmentState enactmentState;
  protected final ExecutorService executor;

  protected boolean isEmergency = false;
  protected Optional<Exception> emergencyCause = Optional.empty();
  protected String emergencyInformation = "";

  @Inject
  protected EnactmentAgents(AgentFactoryActivation agentFactory, GraphAccess graphAccess,
      EnactmentState enactmentState, ExecutorProvider executorProvider) {
    this.scheduledQueueMonitor = agentFactory.createScheduledQueueMonitor();
    scheduledQueueMonitor.addAgentTaskListener(this);
    this.finishedQueueMonitor = agentFactory.createFinishedQueueMonitor();
    finishedQueueMonitor.addAgentTaskListener(this);
    this.availableDataQueueMonitor = agentFactory.createAvalDataQueueMonitor(this);
    availableDataQueueMonitor.addAgentTaskListener(this);
    this.launchableQueueMonitor = agentFactory.createLaunchableQueueMonitor();
    launchableQueueMonitor.addAgentTaskListener(this);
    this.graphAccess = graphAccess;
    this.enactmentState = enactmentState;
    this.executor = executorProvider.getExecutorService();
  }

  @Override
  public JsonObject processInput(JsonObject input) throws StopException {
    initAvailableData(input);
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
    if (isEmergency) {
      reactToEmergency();
    }
    stopMonitors();
    return extractWfResult();
  }

  protected void reactToEmergency() {
    String message = emergencyInformation + "\n";
    message += emergencyCause.get().getMessage();
    throw new RuntimeException("Emergency Exit:\n" + message, emergencyCause.get());
  }

  /**
   * Annotates the root nodes with the contents of the json input; Adds the root nodes and the
   * constant nodes to the available data queue.
   * 
   * @param jsonInput The json object containing the WF input
   */
  protected void initAvailableData(JsonObject jsonInput) {
    // get the input, annotate the root nodes, and add them to the
    // availableDataQueue
    graphAccess.getRootDataNodes().forEach(rootNode -> processRootNode(rootNode, jsonInput));
    // the constant data is available from the start
    graphAccess.getConstantDataNodes()
        .forEach(constantData -> enactmentState.putAvailableData(constantData));
  }

  /**
   * Processes the given root node by annotating it with the entry from the json input
   * 
   * @param rootNode the given root node
   */
  protected void processRootNode(Task rootNode, JsonObject jsonInput) {
    String jsonKey = PropertyServiceData.getJsonKey(rootNode);
    JsonElement content =
        Optional.ofNullable(jsonInput.get(jsonKey)).orElseThrow(() -> new IllegalArgumentException(
            "No entry with the key " + jsonKey + " in the WF input."));
    PropertyServiceData.setContent(rootNode, content);
    enactmentState.putAvailableData(rootNode);
  }

  /**
   * Called when the workflow execution is finished.
   */
  public void finishWfExecution() {
    // back to the main thread, which wakes up to terminate the queue monitors and
    // extract the wf result
    synchronized (this) {
      notifyAll();
    }
  }

  /**
   * Creates the json result by reading the contents of the leaf nodes.
   * 
   * @return the json object with the WF result.
   */
  protected JsonObject extractWfResult() {
    JsonObject result = new JsonObject();
    graphAccess.getLeafDataNodes().forEach(leafNode -> processLeafNode(leafNode, result));
    return result;
  }

  /**
   * Stop the continuous agents.
   */
  protected void stopMonitors() {
    PoisonPill poisonPill = new PoisonPill();
    enactmentState.putAvailableData(poisonPill);
    enactmentState.putFinishedTask(poisonPill);
    enactmentState.putLaunchableTask(poisonPill);
    enactmentState.putSchedulableTask(poisonPill);
  }

  /**
   * Reads the content of the given leaf node and writes it into the JSON result.
   * 
   * @param leafNode the given leaf node
   */
  protected void processLeafNode(Task leafNode, JsonObject result) {
    String jsonKey = PropertyServiceData.getJsonKey(leafNode);
    result.add(jsonKey, PropertyServiceData.getContent(leafNode));
  }

  @Override
  public void reactToException(Exception exc, String info) {
    synchronized (this) {
      isEmergency = true;
      emergencyCause = Optional.of(exc);
      emergencyInformation = info;
      notifyAll();
    }
  }
}

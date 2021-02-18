package at.uibk.dps.ee.control.management;

import java.util.concurrent.ExecutorService;

import com.google.gson.JsonObject;
import com.google.inject.Inject;

import at.uibk.dps.ee.control.agents.AgentActivationEnactment;
import at.uibk.dps.ee.control.agents.AgentActivationExtraction;
import at.uibk.dps.ee.control.agents.AgentActivationScheduling;
import at.uibk.dps.ee.control.agents.AgentActivationTransform;
import at.uibk.dps.ee.control.agents.AgentActivationTransmission;
import at.uibk.dps.ee.control.agents.AgentFactoryActivation;
import at.uibk.dps.ee.control.agents.PoisonPill;
import at.uibk.dps.ee.core.enactable.EnactmentFunction;
import at.uibk.dps.ee.core.exception.StopException;

/**
 * The {@link EnactmentAgent} is the main-thread agent for the enactment of the
 * workflow. It performs the init operations, creates the agents monitoring the
 * state queues, and defines the behavior in case of run-time problems.
 * 
 * @author Fedor Smirnov
 */
public class EnactmentAgent implements EnactmentFunction {

  protected final AgentActivationEnactment activationEnactment;
  protected final AgentActivationExtraction activationExtraction;
  protected final AgentActivationTransmission activationTransmission;
  protected final AgentActivationScheduling activationScheduling;
  protected final AgentActivationTransform activationTransform;

  protected final EnactmentState enactmentState;
  protected final DataHandler dataHandler;
  protected final EmergencyManager emergencyManager;
  protected final ExecutorService executor;

  /**
   * The injection constructor.
   * 
   * @param agentFactory the factory for the activation agents
   * @param enactmentState the state of the enactment
   * @param executorProvider the provider for the execution services
   * @param dataHandler the object handling the wf input and output
   * @param emergencyManager the object handling run-time problems
   */
  @Inject
  protected EnactmentAgent(final AgentFactoryActivation agentFactory,
      final EnactmentState enactmentState, final ExecutorProvider executorProvider,
      final DataHandler dataHandler, final EmergencyManager emergencyManager) {
    this.emergencyManager = emergencyManager;
    this.emergencyManager.registerMain(this);
    this.activationEnactment = agentFactory.createEnactmentActivationAgent();
    activationEnactment.addAgentTaskListener(emergencyManager);
    this.activationExtraction = agentFactory.createExtractionActivationAgent();
    activationExtraction.addAgentTaskListener(emergencyManager);
    this.activationTransmission = agentFactory.createTransmissionActivationAgent(this);
    activationTransmission.addAgentTaskListener(emergencyManager);
    this.activationScheduling = agentFactory.createSchedulingActivationAgent();
    activationScheduling.addAgentTaskListener(emergencyManager);
    this.activationTransform = agentFactory.createTransformActicationAgent();
    activationTransform.addAgentTaskListener(emergencyManager);
    this.enactmentState = enactmentState;
    this.dataHandler = dataHandler;
    this.executor = executorProvider.getExecutorService();
  }

  @Override
  public JsonObject processInput(final JsonObject input) throws StopException {
    dataHandler.annotateAvailableData(input);
    // start up the activation agents
    executor.submit(activationEnactment);
    executor.submit(activationExtraction);
    executor.submit(activationTransmission);
    executor.submit(activationScheduling);
    executor.submit(activationTransform);
    // go to sleep
    synchronized (this) {
      try {
        wait();
      } catch (InterruptedException e) {
        throw new IllegalArgumentException("Root enactable interrupted.", e);
      }
    }
    // woken up => stop the activation agents
    stopActivationAgents();
    // either execute the emergency protocol or return the wf result
    if (emergencyManager.isEmergency()) {
      emergencyManager.emergencyProtocol();
      throw new IllegalStateException("This should be dead code.");
    } else {
      return dataHandler.extractResult();
    }
  }

  /**
   * Called to wake up the main agent.
   */
  public void wakeUp() {
    synchronized (this) {
      notifyAll();
    }
  }

  /**
   * Stops the activation agents.
   */
  protected void stopActivationAgents() {
    final PoisonPill poisonPill = new PoisonPill();
    enactmentState.putAvailableData(poisonPill);
    enactmentState.putFinishedTask(poisonPill);
    enactmentState.putLaunchableTask(poisonPill);
    enactmentState.putSchedulableTask(poisonPill);
    enactmentState.putTransformTask(poisonPill);
  }
}
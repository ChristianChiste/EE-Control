package at.uibk.dps.ee.control.agents;

import java.util.concurrent.ExecutorService;

import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.management.EnactableAgents;
import at.uibk.dps.ee.control.management.EnactmentState;

/**
 * The {@link AgentFactoryActivation} creates the activation agents.
 * 
 * @author Fedor Smirnov
 */
public class AgentFactoryActivation {

	protected final EnactmentState enactmentState;
	protected final ExecutorService executor;
	protected final AgentFactoryScheduling schedulingFactory;
	protected final AgentFactoryTransmission transmissionFactory;
	protected final AgentFactoryEnactment enactmentFactory;
	protected final AgentFactoryExtraction extractionFactory;
	protected final GraphAccess graphAccess;

	public AgentFactoryActivation(EnactmentState enactmentState, ExecutorService executor,
			AgentFactoryScheduling schedulingFactory, AgentFactoryTransmission transmissionFactory,
			AgentFactoryEnactment enactmentFactory, AgentFactoryExtraction extractionFactory, GraphAccess graphAccess) {
		this.enactmentState = enactmentState;
		this.executor = executor;
		this.schedulingFactory = schedulingFactory;
		this.transmissionFactory = transmissionFactory;
		this.enactmentFactory = enactmentFactory;
		this.extractionFactory = extractionFactory;
		this.graphAccess = graphAccess;
	}

	/**
	 * Creates the agent monitoring the launchable queue.
	 * 
	 * @return the agent monitoring the launchable queue.
	 */
	public AgentActivationScheduling createLaunchableQueueMonitor() {
		return new AgentActivationScheduling(enactmentState, schedulingFactory, executor);
	}

	/**
	 * Creates the agent monitoring the available data queue.
	 * 
	 * @param rootEnactable the class starting and stopping the continuous agents
	 * @return the agent monitoring the available data queue.
	 */
	public AgentActivationTransmission createAvalDataQueueMonitor(EnactableAgents rootEnactable) {
		return new AgentActivationTransmission(enactmentState, transmissionFactory, graphAccess, executor, rootEnactable);
	}

	/**
	 * Creates the agent monitoring the scheduled queue.
	 * 
	 * @return the agent monitoring the scheduled queue.
	 */
	public AgentActivationEnactment createScheduledQueueMonitor() {
		return new AgentActivationEnactment(enactmentState, executor, enactmentFactory);
	}

	/**
	 * Creates the agent monitoring the finished queue.
	 * 
	 * @return the agent monitoring the finished queue.
	 */
	public AgentActivationExtraction createFinishedQueueMonitor() {
		return new AgentActivationExtraction(enactmentState, executor, graphAccess, extractionFactory);
	}
}

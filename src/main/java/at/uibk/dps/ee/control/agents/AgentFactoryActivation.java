package at.uibk.dps.ee.control.agents;

import com.google.inject.Inject;

import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.management.EnactmentAgents;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;

/**
 * The {@link AgentFactoryActivation} creates the activation agents.
 * 
 * @author Fedor Smirnov
 */
public class AgentFactoryActivation {

	protected final EnactmentState enactmentState;
	protected final ExecutorProvider executorProvider;
	protected final AgentFactoryScheduling schedulingFactory;
	protected final AgentFactoryTransmission transmissionFactory;
	protected final AgentFactoryEnactment enactmentFactory;
	protected final AgentFactoryExtraction extractionFactory;
	protected final GraphAccess graphAccess;

	@Inject
	public AgentFactoryActivation(EnactmentState enactmentState, ExecutorProvider executorProvider,
			AgentFactoryScheduling schedulingFactory, AgentFactoryTransmission transmissionFactory,
			AgentFactoryEnactment enactmentFactory, AgentFactoryExtraction extractionFactory, GraphAccess graphAccess) {
		this.enactmentState = enactmentState;
		this.executorProvider = executorProvider;
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
	public AgentActivationScheduling createSchedulingActivationAgent() {
		return new AgentActivationScheduling(enactmentState, schedulingFactory, executorProvider);
	}

	/**
	 * Creates the agent monitoring the available data queue.
	 * 
	 * @param rootEnactable the class starting and stopping the continuous agents
	 * @return the agent monitoring the available data queue.
	 */
	public AgentActivationTransmission createTransmissionActivationAgent(EnactmentAgents rootEnactable) {
		return new AgentActivationTransmission(enactmentState, transmissionFactory, graphAccess, executorProvider,
				rootEnactable);
	}

	/**
	 * Creates the agent monitoring the scheduled queue.
	 * 
	 * @return the agent monitoring the scheduled queue.
	 */
	public AgentActivationEnactment createEnactmentActivationAgent() {
		return new AgentActivationEnactment(enactmentState, executorProvider, enactmentFactory);
	}

	/**
	 * Creates the agent monitoring the finished queue.
	 * 
	 * @return the agent monitoring the finished queue.
	 */
	public AgentActivationExtraction createExtractionActivationAgent() {
		return new AgentActivationExtraction(enactmentState, executorProvider, graphAccess, extractionFactory);
	}
}

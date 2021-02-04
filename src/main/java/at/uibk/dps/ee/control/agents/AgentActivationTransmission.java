package at.uibk.dps.ee.control.agents;

import java.util.concurrent.ExecutorService;

import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.management.EnactmentState;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentActivationTransmission} monitors the queue of available data
 * and creates the transmission agents to transmit available data to the tasks.
 * 
 * @author Fedor Smirnov
 */
public class AgentActivationTransmission extends AgentContinuous {

	protected final EnactmentState enactmentState;
	protected final AgentFactoryTransmission agentFactory;
	protected final GraphAccess graphAccess;
	protected final ExecutorService executor;

	public AgentActivationTransmission(EnactmentState enactmentState, AgentFactoryTransmission agentFactory,
			GraphAccess graphAccess, ExecutorService executor) {
		this.enactmentState = enactmentState;
		this.agentFactory = agentFactory;
		this.graphAccess = graphAccess;
		this.executor = executor;
	}

	@Override
	protected void repeatedTask() {
		Task availableData;
		try {
			availableData = enactmentState.takeAvailableData();
			graphAccess.getOutEdges(availableData)
					.forEach(edgeTuple -> executor.submit(agentFactory.createTransmissionAgent(edgeTuple)));

		} catch (InterruptedException e) {
			throw new IllegalStateException("Transmission Activation agent interrupted.", e);
		}
	}
}

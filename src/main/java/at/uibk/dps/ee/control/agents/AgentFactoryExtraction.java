package at.uibk.dps.ee.control.agents;

import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.control.management.EnactmentState;

/**
 * The default factory for the creation of {@link AgentExtraction}s.
 * 
 * @author Fedor Smirnov
 */
public class AgentFactoryExtraction {

	protected final EnactmentState enactmentState;

	public AgentFactoryExtraction(EnactmentState enactmentState) {
		this.enactmentState = enactmentState;
	}

	public AgentExtraction createAgentTransmission(EdgeTupleAppl edgeTuple) {
		return new AgentExtraction(edgeTuple.getSrc(), edgeTuple.getEdge(), edgeTuple.getDst(), enactmentState);
	}
}

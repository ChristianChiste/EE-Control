package at.uibk.dps.ee.control.agents;

import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.control.management.EnactmentState;

/**
 * The {@link AgentFactoryTransmission} is used to create transmission agents.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class AgentFactoryTransmission {

  protected final EnactmentState enactmentState;
  protected final GraphAccess graphAccess;

  @Inject
  public AgentFactoryTransmission(EnactmentState enactmentState, GraphAccess graphAccess) {
    this.enactmentState = enactmentState;
    this.graphAccess = graphAccess;
  }

  /**
   * Returns the transmission agent for the transmission of the data.
   * 
   * @param edgeTuple the tuple describing the processed edge (from a data node to a function)
   * @param functionNodeInEdges the in edges of the function node
   * @return
   */
  public AgentTransmission createTransmissionAgent(EdgeTupleAppl edgeTuple,
      Set<AgentTaskListener> listeners) {
    return new AgentTransmission(enactmentState, edgeTuple.getSrc(), edgeTuple.getEdge(),
        edgeTuple.getDst(), graphAccess, listeners);
  }
}

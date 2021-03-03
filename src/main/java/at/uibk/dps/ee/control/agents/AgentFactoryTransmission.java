package at.uibk.dps.ee.control.agents;

import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.control.transmission.SchedulabilityCheck;
import at.uibk.dps.ee.control.transmission.SchedulabilityCheckDefault;
import at.uibk.dps.ee.control.transmission.SchedulabilityCheckMuxer;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlow;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentFactoryTransmission} is used to create transmission agents.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class AgentFactoryTransmission {

  protected final EnactmentQueues enactmentState;
  protected final GraphAccess graphAccess;

  /**
   * The injection constructor
   * 
   * @param enactmentState the state of the enactment (for the access to the
   *        queues)
   * @param graphAccess the access to the enactment graph
   */
  @Inject
  public AgentFactoryTransmission(final EnactmentQueues enactmentState,
      final GraphAccess graphAccess) {
    this.enactmentState = enactmentState;
    this.graphAccess = graphAccess;
  }

  /**
   * Returns the transmission agent for the transmission of the data over the
   * provided edge.
   * 
   * @param edgeTuple the tuple describing the processed edge (from a data node to
   *        a function)
   * @param functionNodeInEdges the in edges of the function node
   * @return the transmission agent for the transmission of the data over the
   *         provided edge
   */
  public AgentTransmission createTransmissionAgent(final EdgeTupleAppl edgeTuple,
      final Set<AgentTaskListener> listeners) {
    SchedulabilityCheck schedulabilityCheck = getCheckForTarget(edgeTuple.getDst());
    return new AgentTransmission(enactmentState, edgeTuple.getSrc(), edgeTuple.getEdge(),
        edgeTuple.getDst(), graphAccess, listeners, schedulabilityCheck);
  }

  /**
   * Gets the appropriate schedulability check for the provided function node.
   * 
   * @param target the provided function node
   * @return the appropriate schedulability check for the provided function node
   */
  protected SchedulabilityCheck getCheckForTarget(Task target) {
    if (PropertyServiceFunctionDataFlow.isMultiplexerNode(target)) {
      return new SchedulabilityCheckMuxer();
    } else {
      return new SchedulabilityCheckDefault();
    }
  }
}

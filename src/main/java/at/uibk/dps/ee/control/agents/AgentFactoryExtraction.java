package at.uibk.dps.ee.control.agents;

import java.util.Set;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.control.management.EnactmentState;

/**
 * The default factory for the creation of {@link AgentExtraction}s.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class AgentFactoryExtraction {

  protected final EnactmentState enactmentState;

  /**
   * The injection constructor.
   * 
   * @param enactmentState the state of the enactment (for the access to the
   *        queues)
   */
  @Inject
  public AgentFactoryExtraction(final EnactmentState enactmentState) {
    this.enactmentState = enactmentState;
  }

  public AgentExtraction createExtractionAgent(final EdgeTupleAppl edgeTuple,
      final Set<AgentTaskListener> listeners) {
    return new AgentExtraction(edgeTuple.getSrc(), edgeTuple.getEdge(), edgeTuple.getDst(),
        enactmentState, listeners);
  }
}

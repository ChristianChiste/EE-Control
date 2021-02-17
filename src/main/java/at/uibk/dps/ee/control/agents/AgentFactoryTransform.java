package at.uibk.dps.ee.control.agents;

import java.util.Set;
import com.google.inject.Inject;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphTransformAggregation;
import at.uibk.dps.ee.control.graph.GraphTransformDistribution;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections.OperationType;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentFactoryTransform} is used for the creation of
 * {@link AgentTransform}s.
 * 
 * @author Fedor Smirnov
 */
public class AgentFactoryTransform {

  protected final GraphAccess graphAccess;
  protected final EnactableFactory enactableFactory;
  protected final EnactmentState enactmentState;

  @Inject
  public AgentFactoryTransform(GraphAccess graphAccess, EnactableFactory enactableFactory,
      EnactmentState enactmentState) {
    this.graphAccess = graphAccess;
    this.enactableFactory = enactableFactory;
    this.enactmentState = enactmentState;
  }

  /**
   * Creates a transformation agent for the provided task from the transformation
   * queue.
   * 
   * @param taskNode
   * @param listeners
   * @return
   */
  public AgentTransform createTransformAgent(Task taskNode, Set<AgentTaskListener> listeners) {
    if (PropertyServiceFunctionDataFlowCollections.getOperationType(taskNode)
        .equals(OperationType.Distribution)) {
      return new AgentTransform(listeners, graphAccess,
          new GraphTransformDistribution(enactableFactory), taskNode, enactmentState);
    } else if (PropertyServiceFunctionDataFlowCollections.getOperationType(taskNode)
        .equals(OperationType.Aggregation)) {
      return new AgentTransform(listeners, graphAccess, new GraphTransformAggregation(), taskNode,
          enactmentState);
    } else {
      throw new IllegalArgumentException("Unknown type of data flow operation.");
    }
  }

}

package at.uibk.dps.ee.control.transmission;

import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependencyControlIf;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * The {@link SchedulabilityCheck} used for most function nodes.
 * 
 * @author Fedor Smirnov
 *
 */
public class SchedulabilityCheckDefault implements SchedulabilityCheck {

  @Override
  public boolean isTargetSchedulable(Task target, EnactmentGraph graph) {
    if (!graph.getInEdges(target).stream()
        .allMatch(edge -> PropertyServiceDependency.isTransmissionDone(edge))) {
      // Not all transmissions are done yet
      return false;
    } else if (graph.getInEdges(target).stream().anyMatch(
        edge -> PropertyServiceDependency.getType(edge).equals(TypeDependency.ControlIf))) {
      // Checking the activation of if Edges
      return graph.getInEdges(target).stream()
          .filter(edge -> PropertyServiceDependency.getType(edge).equals(TypeDependency.ControlIf))
          .allMatch(controlEdge -> isIfEdgeActive(graph, controlEdge));
    } else {
      return true;
    }
  }

  /**
   * Checks whether the given control if edge is active, i.e., whether its
   * activation matches the data in its src data node.
   * 
   * @param graph the enactment graph
   * @param edge the given edge
   * @return true if the edge is active
   */
  protected boolean isIfEdgeActive(EnactmentGraph graph, Dependency edge) {
    boolean edgeActivation = PropertyServiceDependencyControlIf.getActivation(edge);
    Task dataNode = graph.getSource(edge);
    boolean decisionVariable = PropertyServiceData.getContent(dataNode).getAsBoolean();
    return edgeActivation == decisionVariable;
  }

}

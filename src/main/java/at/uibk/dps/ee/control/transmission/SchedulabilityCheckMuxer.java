package at.uibk.dps.ee.control.transmission;

import java.util.Optional;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency.TypeDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependencyControlIf;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * The {@link SchedulabilityCheck} used for multiplexing nodes.
 * 
 * @author Fedor Smirnov
 */
public class SchedulabilityCheckMuxer implements SchedulabilityCheck {

  private static final int expectedInEdgeNumber = 3;

  @Override
  public boolean isTargetSchedulable(final Task target, final EnactmentGraph graph) {
    if (graph.getInEdges(target).size() != expectedInEdgeNumber) {
      throw new IllegalArgumentException(
          "The muxer node " + target.getId() + " does not have 3 in edges.");
    }
    Dependency decVarDep = null;
    Dependency trueDep = null;
    Dependency falseDep = null;
    for (final Dependency dep : graph.getInEdges(target)) {
      if (PropertyServiceDependency.getType(dep).equals(TypeDependency.ControlIf)) {
        if (PropertyServiceDependencyControlIf.getActivation(dep)) {
          if (Optional.ofNullable(trueDep).isPresent()) {
            throw new IllegalArgumentException(
                "Two if edges annotated as true for node " + target.getId());
          }
          trueDep = dep;
        } else {
          if (Optional.ofNullable(falseDep).isPresent()) {
            throw new IllegalArgumentException(
                "Two if edges annotated as false for node " + target.getId());
          }
          falseDep = dep;
        }
      } else {
        decVarDep = dep;
      }
    }
    if (!PropertyServiceDependency.isTransmissionDone(decVarDep)) {
      return false;
    }
    final boolean decVar =
        PropertyServiceData.getContent(graph.getSource(decVarDep)).getAsBoolean();
    if (decVar) {
      return PropertyServiceDependency.isTransmissionDone(trueDep);
    } else {
      return PropertyServiceDependency.isTransmissionDone(falseDep);
    }
  }
}

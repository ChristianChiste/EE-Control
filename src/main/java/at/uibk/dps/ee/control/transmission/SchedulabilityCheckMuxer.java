package at.uibk.dps.ee.control.transmission;

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

  @Override
  public boolean isTargetSchedulable(Task target, EnactmentGraph graph) {
    if (graph.getInEdges(target).size() != 3) {
      throw new IllegalArgumentException(
          "The muxer node " + target.getId() + " does not have 3 in edges.");
    }
    Dependency decVarDep = null, trueDep = null, falseDep = null;
    for (Dependency dep : graph.getInEdges(target)) {
      if (PropertyServiceDependency.getType(dep).equals(TypeDependency.ControlIf)) {
        if (PropertyServiceDependencyControlIf.getActivation(dep)) {
          trueDep = dep;
        } else {
          falseDep = dep;
        }
      } else {
        decVarDep = dep;
      }
    }
    if (!PropertyServiceDependency.isTransmissionDone(decVarDep)) {
      return false;
    }
    boolean decVar = PropertyServiceData.getContent(graph.getSource(decVarDep)).getAsBoolean();
    if (decVar) {
      return PropertyServiceDependency.isTransmissionDone(trueDep);
    } else {
      return PropertyServiceDependency.isTransmissionDone(falseDep);
    }
  }
}

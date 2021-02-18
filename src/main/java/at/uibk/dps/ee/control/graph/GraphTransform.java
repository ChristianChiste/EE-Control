package at.uibk.dps.ee.control.graph;

import net.sf.opendse.model.Task;

/**
 * Interface for an operation which transforms the enactment graph.
 * 
 * @author Fedor Smirnov
 *
 */
public interface GraphTransform {

  /**
   * Modifies the graph by using the interfaces provided by the graph access.
   * 
   * @param graphAccess the graph access
   * @param taskNode the task triggering the modification
   */
  void modifyEnactmentGraph(GraphAccess graphAccess, Task taskNode);

  /**
   * Returns the name of the performed modification (for logging/exception
   * purposes).
   * 
   * @return the name of the concrete modification operation
   */
  String getTransformName();
}

package at.uibk.dps.ee.control.agents;

import java.util.Set;

import com.google.gson.JsonElement;

import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentTransmission} is responsible for (a) transmitting the content of a data node to a
 * dependent function node and (b) checking whether the function node is ready for execution (in
 * which case it is put into the readyQueue).
 * 
 * @author Fedor Smirnov
 */
public class AgentTransmission extends AgentTask {

  protected final EnactmentState enactmentState;
  protected final Task dataNode;
  protected final Dependency edge;
  protected final Task functionNode;
  protected final GraphAccess graphAccess;

  public AgentTransmission(EnactmentState enactmentState, Task dataNode, Dependency edge,
      Task functionNode, GraphAccess graphAccess, Set<AgentTaskListener> listeners) {
    super(listeners);
    this.enactmentState = enactmentState;
    this.dataNode = dataNode;
    this.edge = edge;
    this.functionNode = functionNode;
    this.graphAccess = graphAccess;
  }

  @Override
  public boolean actualCall() throws Exception {
    // set the enactable data
    JsonElement content = PropertyServiceData.getContent(dataNode);
    String key = PropertyServiceDependency.getJsonKey(edge);
    Enactable enactable = PropertyServiceFunction.getEnactable(functionNode);
    synchronized (enactable) {
      enactable.setInputValue(key, content);
    }
    // annotate the edges
    graphAccess.writeOperationNodeInEdges(this::transmitData, functionNode);
    return true;
  }

  /**
   * Performs the actual data transmission.
   */
  protected void transmitData(Set<Dependency> functionNodeInEdges, Task functionNode) {
    // annotate the dependency
    PropertyServiceDependency.annotateFinishedTransmission(edge);
    // check the annotation of all in edges
    if (functionNodeInEdges.stream()
        .allMatch(edge -> PropertyServiceDependency.isTransmissionDone(edge))) {
      PropertyServiceFunction.getEnactable(functionNode).setState(State.SCHEDULABLE);
      enactmentState.putSchedulableTask(functionNode);
    }
  }

  @Override
  protected String formulateExceptionMessage() {
    return ConstantsAgents.ExcMessageTransmissionPrefix + dataNode.getId()
        + ConstantsAgents.ExcMessageTransmissionSuffix + functionNode.getId();
  }
}

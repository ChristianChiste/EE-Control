package at.uibk.dps.ee.control.agents;

import java.util.Set;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.control.graph.GraphTransform;
import at.uibk.dps.ee.control.management.EnactmentState;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentTransform} is used in cases where the enactment graph needs
 * to be transformed at run time. This agent should be provided with tasks whose
 * enactables have been executed.
 * 
 * @author Fedor Smirnov
 *
 */
public class AgentTransform extends AgentTask {

  protected final GraphTransform modification;
  protected final GraphAccess graphAccess;
  protected final Task taskNode;
  protected final EnactmentState enactmentState;

  public AgentTransform(Set<AgentTaskListener> listeners, GraphAccess graphAccess,
      GraphTransform modification, Task taskNode, EnactmentState enactmentState) {
    super(listeners);
    this.modification = modification;
    this.graphAccess = graphAccess;
    this.taskNode = taskNode;
    this.enactmentState = enactmentState;
  }

  @Override
  protected boolean actualCall() throws Exception {
    modification.modifyEnactmentGraph(graphAccess, taskNode);
    enactmentState.putFinishedTask(taskNode);
    return true;
  }

  @Override
  protected String formulateExceptionMessage() {
    String message = ConstantsAgents.ExcMessageTransformPrefix + modification.getTransformName()
        + ConstantsAgents.ExcMessageTransformSuffix + taskNode.getId();
    return message;
  }
}

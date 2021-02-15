package at.uibk.dps.ee.control.agents;

import java.util.Set;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentEnactment} is responsible for the execution of a single
 * enactable.
 * 
 * @author Fedor Smirnov
 *
 */
public class AgentEnactment extends AgentTask {

  protected final EnactmentState enactmentState;
  protected final Task taskNode;

  public AgentEnactment(EnactmentState enactmentState, Task taskNode,
      Set<AgentTaskListener> listeners) {
    super(listeners);
    this.enactmentState = enactmentState;
    this.taskNode = taskNode;
  }

  @Override
  protected boolean actualCall() throws Exception {
    Enactable enactable = PropertyServiceFunction.getEnactable(taskNode);
    enactable.play();
    enactmentState.putFinishedTask(taskNode);
    return true;
  }

  @Override
  protected String formulateExceptionMessage() {
    return ConstantsAgents.ExcMessageEnactment + taskNode.getId();
  }
}

package at.uibk.dps.ee.control.agents;

import java.util.Set;
import at.uibk.dps.ee.control.enactment.PostEnactment;
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

  protected final Task taskNode;
  protected final PostEnactment postEnactment;

  public AgentEnactment(Task taskNode, PostEnactment postEnactment,
      Set<AgentTaskListener> listeners) {
    super(listeners);
    this.taskNode = taskNode;
    this.postEnactment = postEnactment;
  }

  @Override
  protected boolean actualCall() throws Exception {
    Enactable enactable = PropertyServiceFunction.getEnactable(taskNode);
    enactable.play();
    postEnactment.postEnactmentTreatment(taskNode);
    return true;
  }

  @Override
  protected String formulateExceptionMessage() {
    return ConstantsAgents.ExcMessageEnactment + taskNode.getId();
  }
}

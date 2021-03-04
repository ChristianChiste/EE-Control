package at.uibk.dps.ee.control.agents;

import java.util.Set;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.NodeType;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentExtraction} is responsible for the annotation of the data
 * task nodes with content of finished tasks.
 * 
 * @author Fedor Smirnov
 *
 */
public class AgentExtraction extends AgentTask {

  protected final Task finishedFunction;
  protected final Dependency edge;
  protected final Task dataNode;
  protected final EnactmentQueues enactmentState;

  /**
   * Default constructor
   * 
   * @param finishedFunction the producer of the data to extract
   * @param edge the edge to the data node
   * @param dataNode the data node to fill with content
   * @param enactmentState the enactment state (to access the queues)
   * @param listeners the {@link AgentTaskListener}s
   */
  public AgentExtraction(final Task finishedFunction, final Dependency edge, final Task dataNode,
      final EnactmentQueues enactmentState, final Set<AgentTaskListener> listeners) {
    super(listeners);
    this.finishedFunction = finishedFunction;
    this.edge = edge;
    this.dataNode = dataNode;
    this.enactmentState = enactmentState;

  }

  @Override
  public boolean actualCall() throws Exception {
    final boolean dataNodeModelsSequentiality =
        PropertyServiceData.getNodeType(dataNode).equals(NodeType.Sequentiality);
    final Enactable finishedEnactable = PropertyServiceFunction.getEnactable(finishedFunction);
    final JsonObject enactmentResult = finishedEnactable.getResult();
    final String key = PropertyServiceDependency.getJsonKey(edge);
    if (!enactmentResult.has(key) && !dataNodeModelsSequentiality) {
      throw new IllegalStateException("The execution of the task " + finishedFunction.getId()
          + " did not produce an entry named " + key);
    }
    final JsonElement data =
        dataNodeModelsSequentiality ? new JsonPrimitive(true) : enactmentResult.get(key);
    PropertyServiceData.setContent(dataNode, data);
    enactmentState.putAvailableData(dataNode);
    return true;
  }

  @Override
  protected String formulateExceptionMessage() {
    return ConstantsAgents.ExcMessageExtractionPrefix + finishedFunction.getId()
        + ConstantsAgents.ExcMessageExtractionSuffix + dataNode.getId();
  }
}

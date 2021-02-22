package at.uibk.dps.ee.control.management;

import java.util.Optional;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.inject.Inject;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import net.sf.opendse.model.Task;

/**
 * The default implementation of the data handler.
 * 
 * @author Fedor Smirnov
 *
 */
public class DataHandlerDefault implements DataHandler {

  protected final GraphAccess graphAccess;
  protected final EnactmentQueues enactmentState;

  /**
   * Injection constructor.
   * 
   * @param graphAccess the access to the enactment graph
   * @param enactmentState the state of the enactment (to put the data into
   *        queues)
   */
  @Inject
  public DataHandlerDefault(final GraphAccess graphAccess, final EnactmentQueues enactmentState) {
    this.graphAccess = graphAccess;
    this.enactmentState = enactmentState;
  }

  @Override
  public void annotateAvailableData(final JsonObject input) {
    // get the input, annotate the root nodes, and add them to the
    // availableDataQueue
    graphAccess.getRootDataNodes().forEach(rootNode -> processRootNode(rootNode, input));
    // the constant data is available from the start
    graphAccess.getConstantDataNodes()
        .forEach(constantData -> enactmentState.putAvailableData(constantData));
  }

  /**
   * Processes the given root node by annotating it with the entry from the json
   * input
   * 
   * @param rootNode the given root node
   */
  protected void processRootNode(final Task rootNode, final JsonObject jsonInput) {
    final String jsonKey = PropertyServiceData.getJsonKey(rootNode);
    final JsonElement content =
        Optional.ofNullable(jsonInput.get(jsonKey)).orElseThrow(() -> new IllegalArgumentException(
            "No entry with the key " + jsonKey + " in the WF input."));
    PropertyServiceData.setContent(rootNode, content);
    enactmentState.putAvailableData(rootNode);
  }

  @Override
  public JsonObject extractResult() {
    final JsonObject result = new JsonObject();
    graphAccess.getLeafDataNodes().forEach(leafNode -> processLeafNode(leafNode, result));
    return result;
  }

  /**
   * Reads the content of the given leaf node and writes it into the JSON result.
   * 
   * @param leafNode the given leaf node
   */
  protected void processLeafNode(final Task leafNode, final JsonObject result) {
    final String jsonKey = PropertyServiceData.getJsonKey(leafNode);
    result.add(jsonKey, PropertyServiceData.getContent(leafNode));
  }
}

package at.uibk.dps.ee.control.management;

import com.google.gson.JsonObject;
import com.google.inject.ImplementedBy;

/**
 * The {@link DataHandler} is responsible for the annotation of the input data
 * to the graph and for the extraction of the enactment result from the leaf
 * nodes.
 * 
 * @author Fedor Smirnov
 *
 */
@ImplementedBy(DataHandlerDefault.class)
public interface DataHandler {

  /**
   * Annotates the input data and the constant data nodes before the enactment.
   * 
   * @param input the WF input
   */
  void annotateAvailableData(JsonObject input);

  /**
   * Reads the enactment result from the graph.
   * 
   * @return The {@link JsonObject} with the WF result
   */
  JsonObject extractResult();
}

package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import at.uibk.dps.ee.control.graph.GraphAccess;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.spy;
import java.util.HashSet;
import java.util.Set;

public class DataHandlerDefaultTest {

  @Test
  public void annotateAvailableData() {
    GraphAccess gAccess = mock(GraphAccess.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    DataHandlerDefault tested = new DataHandlerDefault(gAccess, stateMock);

    Communication root = new Communication("root");
    PropertyServiceData.makeRoot(root);
    String key = "key";
    PropertyServiceData.setJsonKey(root, key);
    JsonObject wfInput = new JsonObject();
    JsonElement content = new JsonPrimitive(42);
    wfInput.add(key, content);

    Task constant = PropertyServiceData.createConstantNode("constant", DataType.Boolean,
        new JsonPrimitive(true));
    
    Set<Task> roots = new HashSet<>();
    roots.add(root);
    Set<Task> constants = new HashSet<>();
    constants.add(constant);
    
    when(gAccess.getRootDataNodes()).thenReturn(roots);
    when(gAccess.getConstantDataNodes()).thenReturn(constants);
    
    DataHandlerDefault spy = spy(tested);
    
    spy.annotateAvailableData(wfInput);
    
    verify(stateMock).putAvailableData(root);
    verify(stateMock).putAvailableData(constant);
    verify(spy).processRootNode(root, wfInput);
  }

  @Test
  public void testProcessRootNode() {
    GraphAccess gAccess = mock(GraphAccess.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    DataHandlerDefault tested = new DataHandlerDefault(gAccess, stateMock);

    Communication root = new Communication("root");
    PropertyServiceData.makeRoot(root);
    String key = "key";
    PropertyServiceData.setJsonKey(root, key);
    JsonObject wfInput = new JsonObject();
    JsonElement content = new JsonPrimitive(42);
    wfInput.add(key, content);

    tested.processRootNode(root, wfInput);
    assertEquals(content, PropertyServiceData.getContent(root));
    verify(stateMock).putAvailableData(root);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testProcessRootNodeExc() {
    GraphAccess gAccess = mock(GraphAccess.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    DataHandlerDefault tested = new DataHandlerDefault(gAccess, stateMock);

    Communication root = new Communication("root");
    PropertyServiceData.makeRoot(root);
    PropertyServiceData.setJsonKey(root, "key");

    tested.processRootNode(root, new JsonObject());
  }

  @Test
  public void testExtractResult() {
    Communication leafNode1 = new Communication("leaf1");
    String key1 = "key1";
    PropertyServiceData.makeLeaf(leafNode1);
    PropertyServiceData.setJsonKey(leafNode1, key1);
    JsonElement content = new JsonPrimitive(42);
    PropertyServiceData.setContent(leafNode1, content);

    Communication leafNode2 = new Communication("leaf2");
    String key2 = "key2";
    PropertyServiceData.makeLeaf(leafNode2);
    PropertyServiceData.setJsonKey(leafNode2, key2);
    JsonElement content2 = new JsonPrimitive(true);
    PropertyServiceData.setContent(leafNode2, content2);

    Set<Task> leafNodes = new HashSet<>();
    leafNodes.add(leafNode1);
    leafNodes.add(leafNode2);

    GraphAccess gAccess = mock(GraphAccess.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    DataHandlerDefault tested = new DataHandlerDefault(gAccess, stateMock);
    when(gAccess.getLeafDataNodes()).thenReturn(leafNodes);

    JsonObject result = tested.extractResult();
    assertEquals(content, result.get(key1));
    assertEquals(content2, result.get(key2));
  }

  @Test
  public void testProcessLeafNode() {
    Communication leafNode = new Communication("leaf");
    String key = "key";
    PropertyServiceData.makeLeaf(leafNode);
    PropertyServiceData.setJsonKey(leafNode, key);
    JsonElement content = new JsonPrimitive(42);
    PropertyServiceData.setContent(leafNode, content);
    JsonObject result = new JsonObject();

    GraphAccess gAccess = mock(GraphAccess.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    DataHandlerDefault tested = new DataHandlerDefault(gAccess, stateMock);
    tested.processLeafNode(leafNode, result);
    assertEquals(content, result.get(key));
  }

}

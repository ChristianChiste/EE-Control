package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.mock;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import java.util.HashSet;

public class AgentExtractionTest {

  @Test
  public void test() {
    Task finished = new Task("finished");
    Dependency dep = new Dependency("dep");
    Communication dataNode = new Communication("data");
    Enactable mockEnactable = mock(Enactable.class);
    JsonObject result = new JsonObject();
    JsonElement content = new JsonPrimitive(42);
    result.add("key", content);
    when(mockEnactable.getResult()).thenReturn(result);
    PropertyServiceFunction.setEnactable(finished, mockEnactable);
    EnactmentState mockState = mock(EnactmentState.class);
    PropertyServiceDependency.setJsonKey(dep, "key");
    AgentExtraction tested =
        new AgentExtraction(finished, dep, dataNode, mockState, new HashSet<>());
    String expectedMessage = ConstantsAgents.ExcMessageExtractionPrefix + finished.getId()
        + ConstantsAgents.ExcMessageExtractionSuffix + dataNode.getId();
    assertEquals(expectedMessage, tested.formulateExceptionMessage());
    try {
      tested.actualCall();
    } catch (Exception e) {
      fail();
    }
    assertEquals(content, PropertyServiceData.getContent(dataNode));
    verify(mockState).putAvailableData(dataNode);
  }
}

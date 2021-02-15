package at.uibk.dps.ee.control.agents;

import static org.junit.Assert.*;
import org.junit.Test;
import at.uibk.dps.ee.control.graph.GraphAccess.EdgeTupleAppl;
import at.uibk.dps.ee.control.management.EnactmentState;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import java.util.HashSet;
import java.util.Set;

public class AgentFactoryExtractionTest {

  @Test
  public void test() {
    EnactmentState stateMock = mock(EnactmentState.class);
    Task src = new Task("src");
    Task dst = new Task("dst");
    Dependency edge = new Dependency("dep");
    EdgeTupleAppl tuple = new EdgeTupleAppl(src, dst, edge);
    Set<AgentTaskListener> listeners = new HashSet<>();

    AgentFactoryExtraction tested = new AgentFactoryExtraction(stateMock);
    AgentExtraction result = tested.createExtractionAgent(tuple, listeners);
    assertEquals(src, result.finishedFunction);
    assertEquals(dst, result.dataNode);
    assertEquals(edge, result.edge);
    assertEquals(stateMock, result.enactmentState);
  }
}

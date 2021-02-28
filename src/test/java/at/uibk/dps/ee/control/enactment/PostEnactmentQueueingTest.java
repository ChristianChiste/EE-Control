package at.uibk.dps.ee.control.enactment;

import org.junit.Test;
import at.uibk.dps.ee.control.management.EnactmentQueues;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections.OperationType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionUser;
import net.sf.opendse.model.Task;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class PostEnactmentQueueingTest {

  @Test
  public void testNormalTask() {
    EnactmentQueues queueMock = mock(EnactmentQueues.class);
    Task normalTask = PropertyServiceFunctionUser.createUserTask("task", "addition");
    PostEnactmentQueueing tested = new PostEnactmentQueueing(queueMock);
    tested.postEnactmentTreatment(normalTask);
    verify(queueMock).putFinishedTask(normalTask);
  }

  @Test
  public void testDistAggrTask() {
    EnactmentQueues queueMock = mock(EnactmentQueues.class);
    Task normalTask = PropertyServiceFunctionDataFlowCollections
        .createCollectionDataFlowTask("task", OperationType.Distribution, "scope");
    PostEnactmentQueueing tested = new PostEnactmentQueueing(queueMock);
    tested.postEnactmentTreatment(normalTask);
    verify(queueMock).putTransformTask(normalTask);
  }
}

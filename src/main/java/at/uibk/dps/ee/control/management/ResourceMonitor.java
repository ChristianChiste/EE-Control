package at.uibk.dps.ee.control.management;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.ResourceGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.UsageType;
import at.uibk.dps.ee.model.properties.PropertyServiceResource;
import at.uibk.dps.sc.core.ScheduleModel;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;

/**
 * The {@link ResourceMonitor} monitors the state of the resources during the
 * enactment.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class ResourceMonitor implements EnactableStateListener {

  protected final ScheduleModel scheduleModel;
  protected final Resource eeRes;

  @Inject
  public ResourceMonitor(ScheduleModel scheduleModel, ResourceGraphProvider rGraphProvider) {
    this.scheduleModel = scheduleModel;
    this.eeRes = rGraphProvider.getResourceGraph().getVertex(ConstantsEEModel.idLocalResource);
  }

  @Override
  public void enactableStateChanged(Enactable enactable, State previousState, State currentState) {
    if (enactable instanceof EnactableAtomic) {
      EnactableAtomic atomic = (EnactableAtomic) enactable;
      Task task = atomic.getFunctionNode();
      if (currentState.equals(State.RUNNING) && !previousState.equals(State.RUNNING)) {
        Set<Resource> taskResources = getResourceOfAtomic(atomic);
        // state change to running => resource is being used
        synchronized (this) {
          taskResources.forEach(res -> PropertyServiceResource.addUsingTask(task, res));
        }
      } else if (previousState.equals(State.RUNNING) && !currentState.equals(State.RUNNING)) {
        Set<Resource> taskResources = getResourceOfAtomic(atomic);
        // state change from running => resource in not being used any more
        synchronized (this) {
          taskResources.forEach(res -> PropertyServiceResource.removeUsingTask(task, res));
        }
      }
    }
  }

  /**
   * Gets the resources of the provided atomic enactable.
   * 
   * @param atomic the atomic enactable
   * @return the resources used by the provided atomic enactable.
   */
  protected Set<Resource> getResourceOfAtomic(EnactableAtomic atomic) {
    Set<Resource> result = new HashSet<>();
    Task task = atomic.getFunctionNode();
    if (PropertyServiceFunction.getUsageType(task).equals(UsageType.User)) {
      // user task
      if (!scheduleModel.isScheduled(task)) {
        throw new IllegalStateException("Task " + task.getId() + " is not yet scheduled.");
      }
      result.addAll(scheduleModel.getTaskSchedule(task).stream().map(mapping -> mapping.getTarget())
          .collect(Collectors.toSet()));
    }
    if (result.isEmpty()) {
      // utility or local task
      result.add(eeRes);
    }
    return result;
  }
}

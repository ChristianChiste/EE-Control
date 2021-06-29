package at.uibk.dps.ee.control.management;

import com.google.inject.Inject;

import at.uibk.dps.ee.core.ExecutionData;
import at.uibk.dps.ee.core.ExecutionData.ResourceType;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.local.Composite;
import at.uibk.dps.ee.enactables.local.calculation.FunctionFactoryModule;
import at.uibk.dps.ee.enactables.serverless.ServerlessFunctionWrapper;
import at.uibk.dps.sc.core.modules.SchedulerModule;
import net.sf.opendse.model.Task;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.enactable.EnactmentFunction;

/**
 * The {@link ExecutionMonitor} monitors the execution data for storing it into {@link ExecutionData}.
 * 
 * @author Christian Chisté
 */
public class ExecutionMonitor implements EnactableStateListener{

  protected final SchedulerModule schedulerModule;
  protected final FunctionFactoryModule functionFactoryModule;

  @Inject
  public ExecutionMonitor(SchedulerModule schedulerModule, FunctionFactoryModule functionFactoryModule) {
    this.schedulerModule = schedulerModule;
    this.functionFactoryModule = functionFactoryModule;
  }

  @Override
  public void enactableStateChanged(final Enactable enactable, final State previousState,
      final State currentState) {
    if (enactable instanceof EnactableAtomic) {
      final EnactableAtomic atomic = (EnactableAtomic) enactable;
      final Task task = atomic.getFunctionNode();
      final EnactmentFunction enactmentFunction = atomic.getEnactmentFunction();
      if (currentState.equals(State.RUNNING) && !previousState.equals(State.RUNNING)) {
        if (enactmentFunction instanceof Composite) {
          for (EnactmentFunction function:((Composite) enactmentFunction).getFunctions()) {
            final ResourceType resourceType;
            final String resourceRegion;
            if (function instanceof ServerlessFunctionWrapper) {
              ServerlessFunctionWrapper wrapper = (ServerlessFunctionWrapper)function;
              resourceType = extractProvider(wrapper.getUrl());
              resourceRegion = extractRegion(resourceType, wrapper.getUrl());
            }
            else {
              resourceType = ResourceType.Local;
              resourceRegion = "Local";
            }
            synchronized (this) {
              ExecutionData.startTimes.put(task.getId(), System.nanoTime());
              ExecutionData.resourceType.put(task.getId(), resourceType);
              ExecutionData.resourceRegion.put(task.getId(), resourceRegion);
            }
          }
        }
        else {
          final ResourceType resourceType;
          final String resourceRegion;
          if (enactmentFunction instanceof ServerlessFunctionWrapper) {
            ServerlessFunctionWrapper wrapper = (ServerlessFunctionWrapper)enactmentFunction;
            resourceType = extractProvider(wrapper.getUrl());
            resourceRegion = extractRegion(resourceType, wrapper.getUrl());
          }
          else {
            resourceType = ResourceType.Local;
            resourceRegion = "Local";
          }
          synchronized (this) {
            ExecutionData.startTimes.put(task.getId(), System.nanoTime());
            ExecutionData.resourceType.put(task.getId(), resourceType);
            ExecutionData.resourceRegion.put(task.getId(), resourceRegion);
          }
        }
      } else if (!currentState.equals(State.RUNNING) && previousState.equals(State.RUNNING)) {
        final long endTime = currentState.equals(State.FINISHED) ? System.nanoTime() : -1;
        synchronized (this) {
          if (enactmentFunction instanceof Composite) {
            for (EnactmentFunction function:((Composite) enactmentFunction).getFunctions()) {
              if (function.equals(((Composite) enactmentFunction).getSucceededFunction())) {
                ExecutionData.endTimes.put(task.getId(), endTime);
              }
              else {
                ExecutionData.endTimes.put(task.getId(), -1l);
              }
            }
          }
          else {
            ExecutionData.endTimes.put(task.getId(), endTime);
          }
          ExecutionData.schedulingType = schedulerModule.getSchedulingType().toString();
          ExecutionData.failRate = functionFactoryModule.getFailRateServerless();
        }
      }
    }
  }

  private ResourceType extractProvider(String url) {
    return url.contains("amazonaws.com") ? ResourceType.Amazon : ResourceType.IBM;
  }

  private String extractRegion(ResourceType resourceType, String url) {
    String region = "";
    if (resourceType.equals(ResourceType.Amazon)) {
      region = url.split("\\.")[2];
    }
    else if (resourceType.equals(ResourceType.IBM)) {
      region = url.substring(8, url.indexOf('.'));
    }
    return region;
  }
}

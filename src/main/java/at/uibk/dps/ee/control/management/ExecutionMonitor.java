package at.uibk.dps.ee.control.management;

import java.util.Set;

import com.google.inject.Inject;

import at.uibk.dps.ee.core.ExecutionData;
import at.uibk.dps.ee.core.ExecutionData.ResourceType;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.local.Composite;
import at.uibk.dps.ee.enactables.local.LocalFunctionWrapper;
import at.uibk.dps.ee.enactables.local.calculation.FunctionFactoryModule;
import at.uibk.dps.ee.enactables.serverless.ServerlessFunctionWrapper;
import at.uibk.dps.sc.core.modules.SchedulerModule;
import net.sf.opendse.model.Task;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.enactable.EnactmentFunction;

/**
 * The {@link ExecutionMonitor} monitors the execution data into {@link ExecutionData}.
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
          for(EnactmentFunction function:((Composite) enactmentFunction).getFunctions()) {
            final ResourceType resourceType;
            if (function instanceof ServerlessFunctionWrapper) {
              ServerlessFunctionWrapper wrapper = (ServerlessFunctionWrapper)function;
              resourceType = wrapper.getUrl().contains("functions.appdomain.cloud") ? ResourceType.IBM : ResourceType.Amazon;
            }
            else {
              resourceType = ResourceType.Local;
            }
            synchronized (this) {
              ExecutionData.startTimes.put(task.getId(), System.nanoTime());
              ExecutionData.resourceType.put(task.getId(), resourceType);
            }
          }
        }
        else {
          final ResourceType resourceType;
          if (enactmentFunction instanceof ServerlessFunctionWrapper) {
            ServerlessFunctionWrapper wrapper = (ServerlessFunctionWrapper)enactmentFunction;
            resourceType = wrapper.getUrl().contains("functions.appdomain.cloud") ? ResourceType.IBM : ResourceType.Amazon;
          }
          else  {
            resourceType = ResourceType.Local;
          }
          synchronized (this) {
            ExecutionData.startTimes.put(task.getId(), System.nanoTime());
            ExecutionData.resourceType.put(task.getId(), resourceType);
          }
        }

      } else if (!currentState.equals(State.RUNNING) && previousState.equals(State.RUNNING)) {
        final long endTime = currentState.equals(State.FINISHED) ? System.nanoTime() : -1;
        synchronized (this) {
          ExecutionData.endTimes.put(task.getId(), endTime);
          ExecutionData.schedulingType = schedulerModule.getSchedulingType().toString();
          ExecutionData.failRate = functionFactoryModule.getFailRateLocal();
        }
      }
    }
  }
}

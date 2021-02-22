package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;
import org.junit.Test;
import com.google.gson.JsonObject;
import at.uibk.dps.ee.control.agents.AgentActivationEnactment;
import at.uibk.dps.ee.control.agents.AgentActivationExtraction;
import at.uibk.dps.ee.control.agents.AgentActivationScheduling;
import at.uibk.dps.ee.control.agents.AgentActivationTransform;
import at.uibk.dps.ee.control.agents.AgentActivationTransmission;
import at.uibk.dps.ee.control.agents.AgentFactoryActivation;
import at.uibk.dps.ee.control.agents.PoisonPill;
import at.uibk.dps.ee.control.command.Control;
import at.uibk.dps.ee.core.exception.StopException;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static org.mockito.Mockito.verify;

import static org.mockito.ArgumentMatchers.any;

public class EnactmentAgentTest {

  protected static class WakeUpCall implements Callable<Boolean> {

    protected final EnactmentAgent mainAgent;

    public WakeUpCall(EnactmentAgent mainAgent) {
      super();
      this.mainAgent = mainAgent;
    }

    @Override
    public Boolean call() throws Exception {
      Thread.sleep(300);
      mainAgent.wakeUp();
      return true;
    }
  }

  protected static EnactmentAgent getTested() {
    DataHandler handlerMock = mock(DataHandler.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    ExecutorService execMock = mock(ExecutorService.class);
    ExecutorProvider providerMock = mock(ExecutorProvider.class);
    when(providerMock.getExecutorService()).thenReturn(execMock);
    EmergencyManager emerMan = mock(EmergencyManager.class);

    AgentFactoryActivation factoryMock = mock(AgentFactoryActivation.class);
    AgentActivationEnactment enacterMock = mock(AgentActivationEnactment.class);
    AgentActivationExtraction extractionMock = mock(AgentActivationExtraction.class);
    AgentActivationTransmission transmissionMock = mock(AgentActivationTransmission.class);
    AgentActivationScheduling schedulingMock = mock(AgentActivationScheduling.class);
    AgentActivationTransform transformMock = mock(AgentActivationTransform.class);

    when(factoryMock.createEnactmentActivationAgent()).thenReturn(enacterMock);
    when(factoryMock.createExtractionActivationAgent()).thenReturn(extractionMock);
    when(factoryMock.createSchedulingActivationAgent()).thenReturn(schedulingMock);
    when(factoryMock.createTransformActicationAgent()).thenReturn(transformMock);
    when(factoryMock.createTransmissionActivationAgent(any(EnactmentAgent.class)))
        .thenReturn(transmissionMock);
    Control controlMock = mock(Control.class);
    return new EnactmentAgent(factoryMock, stateMock, providerMock, handlerMock, emerMan,
        controlMock);
  }

  @Test
  public void testEmergencyExecution() {
    DataHandler handlerMock = mock(DataHandler.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    ExecutorService execMock = mock(ExecutorService.class);
    ExecutorProvider providerMock = mock(ExecutorProvider.class);
    when(providerMock.getExecutorService()).thenReturn(execMock);
    EmergencyManager emerMan = mock(EmergencyManager.class);

    AgentFactoryActivation factoryMock = mock(AgentFactoryActivation.class);
    AgentActivationEnactment enacterMock = mock(AgentActivationEnactment.class);
    AgentActivationExtraction extractionMock = mock(AgentActivationExtraction.class);
    AgentActivationTransmission transmissionMock = mock(AgentActivationTransmission.class);
    AgentActivationScheduling schedulingMock = mock(AgentActivationScheduling.class);
    AgentActivationTransform transformMock = mock(AgentActivationTransform.class);

    when(factoryMock.createEnactmentActivationAgent()).thenReturn(enacterMock);
    when(factoryMock.createExtractionActivationAgent()).thenReturn(extractionMock);
    when(factoryMock.createSchedulingActivationAgent()).thenReturn(schedulingMock);
    when(factoryMock.createTransformActicationAgent()).thenReturn(transformMock);
    when(factoryMock.createTransmissionActivationAgent(any(EnactmentAgent.class)))
        .thenReturn(transmissionMock);
    Control controlMock = mock(Control.class);
    EnactmentAgent tested =
        new EnactmentAgent(factoryMock, stateMock, providerMock, handlerMock, emerMan, controlMock);
    EnactmentAgent spy = spy(tested);

    JsonObject mockInput = new JsonObject();
    when(emerMan.isEmergency()).thenReturn(true);
    WakeUpCall wuCall = new WakeUpCall(spy);
    ExecutorService exec = Executors.newCachedThreadPool();

    exec.submit(wuCall);
    try {
      spy.processInput(mockInput);
      fail();
    } catch (StopException e) {
      fail();
    } catch (IllegalStateException isExc) {
    }

    verify(handlerMock).annotateAvailableData(mockInput);
    verify(execMock).submit(enacterMock);
    verify(execMock).submit(extractionMock);
    verify(execMock).submit(transmissionMock);
    verify(execMock).submit(schedulingMock);
    verify(spy).stopActivationAgents();
    try {
      verify(emerMan).emergencyProtocol();
    } catch (StopException e) {
      fail();
    }
  }

  @Test
  public void testNormalExecution() {
    DataHandler handlerMock = mock(DataHandler.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    ExecutorService execMock = mock(ExecutorService.class);
    ExecutorProvider providerMock = mock(ExecutorProvider.class);
    when(providerMock.getExecutorService()).thenReturn(execMock);
    EmergencyManager emerMan = mock(EmergencyManager.class);

    AgentFactoryActivation factoryMock = mock(AgentFactoryActivation.class);
    AgentActivationEnactment enacterMock = mock(AgentActivationEnactment.class);
    AgentActivationExtraction extractionMock = mock(AgentActivationExtraction.class);
    AgentActivationTransmission transmissionMock = mock(AgentActivationTransmission.class);
    AgentActivationScheduling schedulingMock = mock(AgentActivationScheduling.class);
    AgentActivationTransform transformMock = mock(AgentActivationTransform.class);

    when(factoryMock.createEnactmentActivationAgent()).thenReturn(enacterMock);
    when(factoryMock.createExtractionActivationAgent()).thenReturn(extractionMock);
    when(factoryMock.createSchedulingActivationAgent()).thenReturn(schedulingMock);
    when(factoryMock.createTransformActicationAgent()).thenReturn(transformMock);
    when(factoryMock.createTransmissionActivationAgent(any(EnactmentAgent.class)))
        .thenReturn(transmissionMock);
    Control controlMock = mock(Control.class);
    EnactmentAgent tested =
        new EnactmentAgent(factoryMock, stateMock, providerMock, handlerMock, emerMan, controlMock);
    EnactmentAgent spy = spy(tested);

    JsonObject mockInput = new JsonObject();
    when(emerMan.isEmergency()).thenReturn(false);
    WakeUpCall wuCall = new WakeUpCall(spy);
    ExecutorService exec = Executors.newCachedThreadPool();

    exec.submit(wuCall);
    try {
      spy.processInput(mockInput);
    } catch (StopException e) {
      fail();
    }

    verify(handlerMock).annotateAvailableData(mockInput);
    verify(execMock).submit(enacterMock);
    verify(execMock).submit(extractionMock);
    verify(execMock).submit(transmissionMock);
    verify(execMock).submit(schedulingMock);
    verify(spy).stopActivationAgents();
    verify(handlerMock).extractResult();
  }

  @Test
  public void testConstructor() {
    DataHandler handlerMock = mock(DataHandler.class);
    EnactmentQueues stateMock = mock(EnactmentQueues.class);
    ExecutorService execMock = mock(ExecutorService.class);
    ExecutorProvider providerMock = mock(ExecutorProvider.class);
    when(providerMock.getExecutorService()).thenReturn(execMock);
    EmergencyManager emerMan = mock(EmergencyManager.class);

    AgentFactoryActivation factoryMock = mock(AgentFactoryActivation.class);
    AgentActivationEnactment enacterMock = mock(AgentActivationEnactment.class);
    AgentActivationExtraction extractionMock = mock(AgentActivationExtraction.class);
    AgentActivationTransmission transmissionMock = mock(AgentActivationTransmission.class);
    AgentActivationScheduling schedulingMock = mock(AgentActivationScheduling.class);
    AgentActivationTransform transformMock = mock(AgentActivationTransform.class);

    when(factoryMock.createEnactmentActivationAgent()).thenReturn(enacterMock);
    when(factoryMock.createExtractionActivationAgent()).thenReturn(extractionMock);
    when(factoryMock.createSchedulingActivationAgent()).thenReturn(schedulingMock);
    when(factoryMock.createTransformActicationAgent()).thenReturn(transformMock);
    when(factoryMock.createTransmissionActivationAgent(any(EnactmentAgent.class)))
        .thenReturn(transmissionMock);
    Control controlMock = mock(Control.class);
    EnactmentAgent tested =
        new EnactmentAgent(factoryMock, stateMock, providerMock, handlerMock, emerMan, controlMock);

    assertEquals(schedulingMock, tested.activationScheduling);
    assertEquals(extractionMock, tested.activationExtraction);
    assertEquals(transmissionMock, tested.activationTransmission);
    assertEquals(enacterMock, tested.activationEnactment);
  }

  @Test
  public void testStopMonitors() {
    EnactmentAgent tested = getTested();
    tested.stopActivationAgents();
    verify(tested.enactmentState).putAvailableData(any(PoisonPill.class));
    verify(tested.enactmentState).putFinishedTask(any(PoisonPill.class));
    verify(tested.enactmentState).putLaunchableTask(any(PoisonPill.class));
    verify(tested.enactmentState).putSchedulableTask(any(PoisonPill.class));
    verify(tested.enactmentState).putTransformTask(any(PoisonPill.class));
  }
}

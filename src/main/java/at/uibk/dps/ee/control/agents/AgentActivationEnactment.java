package at.uibk.dps.ee.control.agents;

import java.util.concurrent.ExecutorService;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Task;

/**
 * The ActivationEnactment is responsible for the activation of the
 * {@link AgentEnactment}s to process the tasks in the ready queue.
 * 
 * @author Fedor Smirnov
 *
 */
public class AgentActivationEnactment extends AgentContinuous {

	protected final EnactmentState enactmentState;
	protected final ExecutorService executor;
	protected final AgentEnactmentFactory agentFactory;

	public AgentActivationEnactment(EnactmentState enactmentState, ExecutorService executor,
			AgentEnactmentFactory agentFactory) {
		this.enactmentState = enactmentState;
		this.executor = executor;
		this.agentFactory = agentFactory;
	}

	@Override
	protected void repeatedTask() {
		Task readyTask;
		try {
			readyTask = enactmentState.takeReadyTask();
			Enactable enactable = PropertyServiceFunction.getEnactable(readyTask);
			AgentEnactment enacterAgent = agentFactory.createAgentEnactment(enactable);
			executor.submit(enacterAgent);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Starter agent interrupted.", e);
		}
	}
}

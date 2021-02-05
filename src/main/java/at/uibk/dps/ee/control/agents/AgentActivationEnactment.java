package at.uibk.dps.ee.control.agents;

import java.util.concurrent.ExecutorService;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Task;

/**
 * The ActivationEnactment is responsible for the activation of the
 * {@link AgentEnactment}s to process the tasks in the ready queue.
 * 
 * @author Fedor Smirnov
 */
public class AgentActivationEnactment extends AgentContinuous {

	protected final EnactmentState enactmentState;
	protected final ExecutorService executor;
	protected final AgentFactoryEnactment agentFactory;

	public AgentActivationEnactment(EnactmentState enactmentState, ExecutorProvider executorProvider,
			AgentFactoryEnactment agentFactory) {
		this.enactmentState = enactmentState;
		this.executor = executorProvider.getExecutorService();
		this.agentFactory = agentFactory;
	}

	@Override
	protected void repeatedTask() {
		try {
			Task readyTask = enactmentState.takeScheduledTask();
			AgentEnactment enacterAgent = agentFactory.createAgentEnactment(readyTask);
			executor.submit(enacterAgent);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Enactment Activation agent interrupted.", e);
		}
	}
}

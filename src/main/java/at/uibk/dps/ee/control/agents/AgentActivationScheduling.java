package at.uibk.dps.ee.control.agents;

import java.util.concurrent.ExecutorService;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.control.management.ExecutorProvider;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentActivationScheduling} monitors the queue containing the
 * launchable tasks and creates {@link AgentScheduling}s to process them.
 * 
 * @author Fedor Smirnov
 */
public class AgentActivationScheduling extends AgentContinuous {

	protected final EnactmentState enactmentState;
	protected final AgentFactoryScheduling agentFactory;
	protected final ExecutorService executor;

	public AgentActivationScheduling(EnactmentState enactmentState, AgentFactoryScheduling agentFactory,
			ExecutorProvider executorProvider) {
		this.enactmentState = enactmentState;
		this.agentFactory = agentFactory;
		this.executor = executorProvider.getExecutorService();
	}

	@Override
	protected void repeatedTask() {
		try {
			Task launchableTask = enactmentState.takeLaunchableTask();
			executor.submit(agentFactory.createSchedulingAgent(launchableTask));
		} catch (InterruptedException e) {
			throw new IllegalStateException("Scheduling Activation agent interrupted.", e);
		}
	}
}

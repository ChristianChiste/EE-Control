package at.uibk.dps.ee.control.agents;

/**
 * A continuous agent is an agent with a continuous task which it pursuits
 * indefinitely unless actively stopped.
 * 
 * @author Fedor Smirnov
 *
 */
public abstract class AgentContinuous implements Agent {

	protected boolean stopped = false;

	/**
	 * Stops the agent so that it terminates at the next possible point.
	 */
	public synchronized void stop() {
		this.notifyAll();
		stopped = true;
	}

	public synchronized boolean isStopped() {
		return stopped;
	}

	@Override
	public Boolean call() throws Exception {
		while (!stopped) {
			synchronized (this) {
				if (!isStopped()) {
					repeatedTask();
				}
			}
		}
		return true;
	}

	/**
	 * The task of the continuous agent which (can contain periods of waiting and)
	 * is repeated indefinitely, until the agent is actively stopped.
	 */
	protected abstract void repeatedTask();
}

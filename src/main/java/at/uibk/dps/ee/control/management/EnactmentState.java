package at.uibk.dps.ee.control.management;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import net.sf.opendse.model.Task;

/**
 * The {@link EnactmentState} captures the current state of the enactment and
 * manages the access to the enactment graph.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class EnactmentState {

	protected final EnactmentGraph enactmentGraph;
	protected final LinkedBlockingQueue<Task> readyTasks;

	@Inject
	public EnactmentState(EnactmentGraphProvider graphProvider) {
		this.enactmentGraph = graphProvider.getEnactmentGraph();
		this.readyTasks = new LinkedBlockingQueue<>();
	}

	/**
	 * Returns a task node which is ready for execution or waits until the queue is
	 * non-empty.
	 * 
	 * @return a task node which is ready to be executed.
	 * @throws InterruptedException
	 */
	public Task takeReadyTask() throws InterruptedException {
		return readyTasks.take();
	}

	/**
	 * Adds a ready task to the queue (Blocking should never happen here since we do
	 * not have a queue capacity limit).
	 * 
	 * @param readyTask the task to be added to the queue.
	 */
	public void putReadyTask(Task readyTask) {
		try {
			readyTasks.put(readyTask);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Interrupted exception when putting ready task.", e);
		}
	}
}

package at.uibk.dps.ee.control.management;

import java.util.concurrent.LinkedBlockingQueue;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.sf.opendse.model.Element;
import net.sf.opendse.model.Task;

/**
 * The {@link EnactmentState} captures the current state of the enactment and
 * manages the access to the enactment graph.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class EnactmentState {

	protected final LinkedBlockingQueue<Task> scheduledTasks;
	protected final LinkedBlockingQueue<Task> launchableTasks;
	protected final LinkedBlockingQueue<Task> finishedTasks;
	protected final LinkedBlockingQueue<Task> availableData;

	@Inject
	public EnactmentState() {
		this.scheduledTasks = new LinkedBlockingQueue<>();
		this.finishedTasks = new LinkedBlockingQueue<>();
		this.availableData = new LinkedBlockingQueue<>();
		this.launchableTasks = new LinkedBlockingQueue<>();
	}

	/**
	 * Returns a function task which is ready to be launched. Blocks if the queue of
	 * launchable tasks is empty.
	 * 
	 * @return a function task which is ready to be launched
	 * @throws InterruptedException
	 */
	public Task takeLaunchableTask() throws InterruptedException {
		return takeFromQueue(launchableTasks);
	}

	/**
	 * Puts a function node into the queue of tasks which can be launched.
	 * 
	 * @param functionTask the task which is ready to be launched
	 */
	public void putLaunchableTask(Task functionTask) {
		putInQueue(launchableTasks, functionTask);
	}

	/**
	 * Returns a data node with available data. This method blocks until data is
	 * available.
	 * 
	 * @return a data node with content.
	 * @throws InterruptedException
	 */
	public Task takeAvailableData() throws InterruptedException {
		return takeFromQueue(availableData);
	}

	/**
	 * Puts a data node into the queue with the available data.
	 * 
	 * @param dataNode the data node with available content
	 */
	public void putAvailableData(Task dataNode) {
		putInQueue(availableData, dataNode);
	}

	/**
	 * Returns a task node which was executed, so that it is annotated with results.
	 * 
	 * @return a task node which finished its execution.
	 * @throws InterruptedException
	 */
	public Task takeFinishedTask() throws InterruptedException {
		return takeFromQueue(finishedTasks);
	}

	/**
	 * Adds a finished task to the queue (Blocking should never happen here since we
	 * do not have a queue capacity limit).
	 * 
	 * @param readyTask the finished task to be added to the queue.
	 */
	public void putFinishedTask(Task finishedTask) {
		putInQueue(finishedTasks, finishedTask);
	}

	/**
	 * Returns a task node which is ready for execution or waits until the queue is
	 * non-empty.
	 * 
	 * @return a task node which is ready to be executed.
	 * @throws InterruptedException
	 */
	public Task takeScheduledTask() throws InterruptedException {
		return takeFromQueue(scheduledTasks);
	}

	/**
	 * Adds a ready task to the queue (Blocking should never happen here since we do
	 * not have a queue capacity limit).
	 * 
	 * @param readyTask the task to be added to the queue.
	 */
	public void putScheduledTask(Task readyTask) {
		putInQueue(scheduledTasks, readyTask);
	}

	/**
	 * Returns an element from the blocking queue. Can cause consumers to wait until
	 * content is available.
	 * 
	 * @param <E>   task or dependency
	 * @param queue the blocking queue
	 * @return an element from the blocking queue
	 * @throws InterruptedException
	 */
	protected <E extends Element> E takeFromQueue(LinkedBlockingQueue<E> queue) throws InterruptedException {
		return queue.take();
	}

	/**
	 * Puts an element into one of the blocking queues. Since none of them have a
	 * capacity limit, blocking should never happen.
	 * 
	 * @param <E>     either task or dependency
	 * @param queue   the queue to put the element in
	 * @param element the element to put into the queue
	 */
	protected <E extends Element> void putInQueue(LinkedBlockingQueue<E> queue, E element) {
		try {
			queue.put(element);
		} catch (InterruptedException e) {
			throw new IllegalStateException("Interrupted exception when putting task in queue.", e);
		}
	}
}

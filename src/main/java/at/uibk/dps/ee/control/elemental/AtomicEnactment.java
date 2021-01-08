package at.uibk.dps.ee.control.elemental;

import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import net.sf.opendse.model.Task;

/**
 * Runnable for the enactment and result annotation of an atomic function.
 * 
 * @author Fedor Smirnov
 *
 */
public class AtomicEnactment implements Runnable {

	protected final EnactableAtomic enactable;
	protected final Task task;

	/**
	 * Standard constructor.
	 * 
	 * @param enactable the enactable which defines what is done when the runnable
	 *                  is executed
	 * @param task      the task node modeling this atomic enactment in the
	 *                  enactment graph
	 */
	public AtomicEnactment(final EnactableAtomic enactable, final Task task) {
		this.enactable = enactable;
		this.task = task;
	}

	@Override
	public void run() {
		try {
			enactable.play();
		} catch (StopException e) {
			// TODO here, we can have a better handling
			throw new IllegalStateException("Stop exception when processing atomic enactable of task " + task.getId(),
					e);
		}
	}
}

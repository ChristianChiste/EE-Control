package at.uibk.dps.ee.control.management;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.google.gson.JsonObject;

import at.uibk.dps.ee.control.command.Control;
import at.uibk.dps.ee.control.runnable.AtomicEnactment;
import at.uibk.dps.ee.core.ControlStateListener;
import at.uibk.dps.ee.core.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.EnactableRoot;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Task;

/**
 * The {@link EnactmentManager} maintains the {@link EnactmentGraph}, monitors
 * the state of the elemental enactables, and triggers their execution as soon
 * as their input data is available.
 * 
 * @author Fedor Smirnov
 *
 */
public class EnactmentManager extends EnactableRoot implements ControlStateListener, EnactableStateListener {

	protected final DataLogistics dataLogistics;

	// Set of tasks which can be started
	protected final Set<Task> readyTasks = Collections.synchronizedSet(new HashSet<>());

	/**
	 * Default constructor.
	 * 
	 * @param stateListeners the enactable state listeners added via guice
	 * @param graphProvider  the object providing the {@link EnactmentGraph}
	 * @param control        the control object for the implementation of user
	 *                       commands
	 */
	public EnactmentManager(final Set<EnactableStateListener> stateListeners, DataLogistics dataLogistics, EnactableFactory factory,
			final Control control) {
		super(stateListeners);
		this.dataLogistics = dataLogistics;
		control.addListener(this);
		factory.addEnactableStateListener(this);
	}

	@Override
	public synchronized void reactToStateChange(EnactmentState previousState, EnactmentState currentState)
			throws StopException {
		if (previousState.equals(EnactmentState.RUNNING) && currentState.equals(EnactmentState.PAUSED)) {
			// run => pause
			pause();
		} else if (previousState.equals(EnactmentState.PAUSED) && currentState.equals(EnactmentState.RUNNING)) {
			// pause => run
			notifyAll();
			setState(State.RUNNING);
		} else {
			throw new IllegalStateException("Transition from enactment state " + previousState.name() + " to "
					+ currentState.name() + " not yet implemented.");
		}
	}

	@Override
	protected void myPlay() throws StopException {
		while (!dataLogistics.isWfFinished()) {
			if (!readyTasks.isEmpty() && !state.equals(State.PAUSED)) {
				// enact an enactable and annotate the results
				enactReadyTasks();
			} else {
				// wait to be woken up by a worker thread
				try {
					synchronized (readyTasks) {
						readyTasks.wait();
					}
				} catch (InterruptedException e) {
					throw new IllegalStateException("Enactment manager interrupted while waiting.", e);
				}
			}
		}
	}

	/**
	 * Creates a callable for each task which is ready. The callable triggers the
	 * execution of the enactable and then annotates the output data.
	 * 
	 * @param readyTasks the tasks which are ready
	 */
	protected synchronized void enactReadyTasks() {
		Set<Task> handled = new HashSet<>();
		for (Task task : readyTasks) {
			EnactableAtomic enactable = (EnactableAtomic) PropertyServiceFunction.getEnactable(task);
			AtomicEnactment atomicEnactment = new AtomicEnactment(enactable, task);
			Thread thread = new Thread(atomicEnactment);
			thread.start();
			handled.add(task);
		}
		readyTasks.removeAll(handled);
	}

	@Override
	protected void myPause() {
		// the pause behavior depends on the state being set to paused => no extra
		// behavior in this method
	}

	@Override
	protected void myInit() {
		// annotates the data present at the start of the enactment
		dataLogistics.initData(wfInput);
	}

	@Override
	public void enactableStateChanged(Enactable enactable, State previousState, State currentState) {
		if (enactable instanceof EnactableAtomic) {
			if (previousState.equals(State.WAITING) && currentState.equals(State.READY)) {
				synchronized (readyTasks) {
					// Enactable is ready => add its task to the ready list
					readyTasks.add(((EnactableAtomic) enactable).getFunctionNode());
					readyTasks.notifyAll();
				}
			} else if (enactable instanceof EnactableAtomic && previousState.equals(State.RUNNING)
					&& currentState.equals(State.FINISHED)) {
				// Enactable finished execution => annotate the results
				if (enactable instanceof EnactableAtomic) {
					EnactableAtomic atomic = (EnactableAtomic) enactable;
					dataLogistics.annotateExecutionResults(atomic);
					if (dataLogistics.isWfFinished()) {
						synchronized (readyTasks) {
							readyTasks.notifyAll();
						}
					}
				} else {
					throw new IllegalStateException("Behavior for non atomic enactables not yet implemented.");
				}
			}
		}
	}

	@Override
	protected void myReset() {
		// No reset behavior for now
	}

	@Override
	public JsonObject getOutput() {
		return dataLogistics.readWfOutput();
	}
}

package at.uibk.dps.ee.control.command;

import java.util.HashSet;
import java.util.Set;

import com.google.inject.Singleton;

import at.uibk.dps.ee.core.ControlStateListener;
import at.uibk.dps.ee.core.EnactmentState;
import at.uibk.dps.ee.core.enactable.EnactmentStateListener;
import at.uibk.dps.ee.core.exception.StopException;

/**
 * Class implementing the reaction to external triggers, enabling a dynamic
 * control of the enactment process.
 * 
 * @author Fedor Smirnov
 */
@Singleton
public class Control implements EnactmentStateListener {

	protected EnactmentState enactmentState = EnactmentState.PAUSED;
	protected final Set<ControlStateListener> listeners = new HashSet<>();
	protected boolean init = false;

	/**
	 * Adds a {@link ControlStateListener}.
	 * 
	 * @param listener the listener to add
	 */
	public void addListener(ControlStateListener listener) {
		listeners.add(listener);
	}

	/**
	 * Run if paused. Otherwise this does nothing.
	 */
	public void play() {
		if (!init) {
			throw new IllegalStateException("Control play triggerred before control initialization.");
		}
		if (enactmentState.equals(EnactmentState.PAUSED)) {
			setState(EnactmentState.RUNNING);
		}
	}

	/**
	 * Pause if running. Otherwise nothing.
	 */
	public void pause() {
		if (enactmentState.equals(EnactmentState.RUNNING)) {
			setState(EnactmentState.PAUSED);
		}
	}

	public boolean isInit() {
		return init;
	}

	/**
	 * Sets the current state and notifies all listeners.
	 * 
	 * @param stateToSet the state to set
	 */
	protected void setState(final EnactmentState stateToSet) {
		final EnactmentState previous = enactmentState;
		final EnactmentState current = stateToSet;
		this.enactmentState = stateToSet;
		for (final ControlStateListener listener : listeners) {
			try {
				listener.reactToStateChange(previous, current);
			} catch (StopException stopExc) {
				throw new IllegalStateException("Stop exception when changing the control state.", stopExc);
			}
		}
	}

	@Override
	public void enactmentStarted() {
		enactmentState = EnactmentState.RUNNING;
		init = true;
	}

	public EnactmentState getEnactmentState() {
		return enactmentState;
	}

	@Override
	public void enactmentTerminated() {
		// Nothing to do here
	}
}

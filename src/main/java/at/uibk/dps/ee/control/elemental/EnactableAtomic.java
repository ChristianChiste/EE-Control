package at.uibk.dps.ee.control.elemental;

import java.util.Set;


import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.exception.StopException;

/**
 * The {@link EnactableAtomic} 
 * 
 * @author Fedor Smirnov
 *
 */
public class EnactableAtomic extends Enactable{

	protected EnactableAtomic(Set<EnactableStateListener> stateListeners) {
		super(stateListeners);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void myPlay() throws StopException {
		// TODO Auto-generated method stub
		throw new IllegalStateException("Not yet implemented.");
	}

	@Override
	protected void myPause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void myInit() {
		// TODO Auto-generated method stub
		
	}

}

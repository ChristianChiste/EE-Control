package at.uibk.dps.ee.control.agents;

import java.util.concurrent.Callable;

/**
 * Interface for all agents used during the enactment. Each agent corresponds
 * to a process which is performed in its own thread.
 * 
 * @author Fedor Smirnov
 *
 */
public interface Agent extends Callable<Boolean>{
}

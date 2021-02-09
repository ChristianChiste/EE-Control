package at.uibk.dps.ee.control.management;

import java.util.concurrent.ExecutorService;

import com.google.inject.ImplementedBy;

/**
 * Interface used to configure and provide the executor servide which the agents
 * use for the creation of threads.
 * 
 * @author Fedor Smirnov
 *
 */
@ImplementedBy(ExecutorProviderCachedThreads.class)
public interface ExecutorProvider {

	/**
	 * Returns an executor service which is to be used for the creation of threads.
	 * 
	 * @return the executor service which is to be used for the creation of threads
	 */
	ExecutorService getExecutorService();
}

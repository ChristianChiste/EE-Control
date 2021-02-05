package at.uibk.dps.ee.control.management;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Executor provider working with a single thread executor.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class ExecutorProviderSingleThread implements ExecutorProvider {

	protected final ExecutorService executor;

	@Inject
	public ExecutorProviderSingleThread() {
		this.executor = Executors.newSingleThreadExecutor();
	}

	@Override
	public ExecutorService getExecutorService() {
		return executor;
	}
}

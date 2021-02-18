package at.uibk.dps.ee.control.management;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Executor provider working with a cached thread pool executor.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class ExecutorProviderCachedThreads implements ExecutorProvider {

  protected final ExecutorService executor;

  /**
   * Injection constructor.
   */
  @Inject
  public ExecutorProviderCachedThreads() {
    this.executor = Executors.newCachedThreadPool();
  }

  @Override
  public ExecutorService getExecutorService() {
    return executor;
  }
}

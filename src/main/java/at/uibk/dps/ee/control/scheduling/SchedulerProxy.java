package at.uibk.dps.ee.control.scheduling;

import java.util.Set;

import net.sf.opendse.model.Mapping;
import net.sf.opendse.model.Resource;
import net.sf.opendse.model.Task;

/**
 * Interface for the class used for
 * 
 * @author Fedor Smirnov
 *
 */
public interface SchedulerProxy {

	/**
	 * Returns the schedule for the given task, specified by a set of (annotated)
	 * mappings.
	 * 
	 * @param functionTask the task to request the schedule for
	 * @return the schedule for the given task, specified by a set of (annotated)
	 *         mappings
	 */
	Set<Mapping<Task, Resource>> getTaskSchedule(Task functionTask);

}

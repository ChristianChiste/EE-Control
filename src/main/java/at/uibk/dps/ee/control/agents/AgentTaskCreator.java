package at.uibk.dps.ee.control.agents;

import java.util.Set;

public interface AgentTaskCreator {

  Set<AgentTaskListener> getAgentTaskListeners();

  void addAgentTaskListener(AgentTaskListener listener);

}

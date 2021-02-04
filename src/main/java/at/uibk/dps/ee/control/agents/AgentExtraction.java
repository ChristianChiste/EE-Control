package at.uibk.dps.ee.control.agents;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import at.uibk.dps.ee.control.management.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * The {@link AgentExtraction} is responsible for the annotation of the data task nodes with content of finished tasks. 
 * 
 * @author Fedor Smirnov
 *
 */
public class AgentExtraction implements Agent{

	protected final Task finishedFunction;
	protected final Dependency edge;
	protected final Task dataNode;
	protected final EnactmentState enactmentState;
	
	public AgentExtraction(Task finishedFunction, Dependency edge, Task dataNode, EnactmentState enactmentState) {
		this.finishedFunction = finishedFunction;
		this.edge = edge;
		this.dataNode = dataNode;
		this.enactmentState = enactmentState;
	}

	@Override
	public Boolean call() throws Exception {
		Enactable finishedEnactable = PropertyServiceFunction.getEnactable(finishedFunction);
		JsonObject enactmentResult = finishedEnactable.getResult();
		String key = PropertyServiceDependency.getJsonKey(edge);
		JsonElement data = enactmentResult.get(key);
		PropertyServiceData.setContent(dataNode, data);
		enactmentState.putAvailableData(dataNode);
		return true;
	}
}

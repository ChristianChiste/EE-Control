package at.uibk.dps.ee.control.graph;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * A kind-of decorator for the graph provider which handles the creation of
 * enactables for the function nodes.
 * 
 * @author Fedor Smirnov
 *
 */
@Singleton
public class GraphProviderEnactables implements EnactmentGraphProvider {

	protected final EnactmentGraph graph;

	@Inject
	public GraphProviderEnactables(EnactmentGraphProvider graphProvider, EnactableFactory factory) {
		EnactmentGraph graph = graphProvider.getEnactmentGraph();
		createEnactables(graph, factory);
		this.graph = graph;
	}

	/**
	 * Goes through the given graph and creates and annotates an enactable for each
	 * function node.
	 * 
	 * @param graph   the enactment graph
	 * @param factory the enactable factory
	 */
	protected final void createEnactables(EnactmentGraph graph, EnactableFactory factory) {
		graph.getVertices().stream().filter(task -> TaskPropertyService.isProcess(task))
				.forEach(task -> createTaskEnactable(task, factory));
	}

	/**
	 * Creates an enactable for the given function node and annotates it in the
	 * node.
	 * 
	 * @param functionNode the given function node
	 * @param factory      the factory for the creation of enactables
	 */
	protected void createTaskEnactable(Task functionNode, EnactableFactory factory) {
		EnactableAtomic enactable = factory.createEnactable(functionNode);
		PropertyServiceFunction.setEnactable(functionNode, enactable);
	}

	@Override
	public EnactmentGraph getEnactmentGraph() {
		return graph;
	}
}

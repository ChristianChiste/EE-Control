package at.uibk.dps.ee.control.management;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;
import net.sf.opendse.model.properties.TaskPropertyService;

/**
 * Class for operations changing the enactment graph at run time.
 * 
 * @author Fedor Smirnov
 */
public class GraphModifier {

	protected final EnactmentGraph graph;
	protected final EnactableFactory enactableFactory;

	@Inject
	public GraphModifier(EnactmentGraphProvider graphProvider, EnactableFactory enactableFactory) {
		this.graph = graphProvider.getEnactmentGraph();
		this.enactableFactory = enactableFactory;
	}

	/**
	 * Reverts the reproduction idenitified by the provided scope string.
	 * 
	 * @param graph the enactment graph
	 * @param scope the provided scope string
	 */
	public synchronized void revertDistributionReproduction(String scope) {
		if (!readyForRevert(scope)) {
			return;
		}

		// find the distribution node
		Set<Task> dNodes = graph.getVertices().stream()
				.filter(task -> PropertyServiceFunctionDataFlowCollections.isDistributionNode(task)
						&& PropertyServiceFunctionDataFlowCollections.getScope(task).equals(scope))
				.collect(Collectors.toSet());
		if (dNodes.size() > 1) {
			throw new IllegalArgumentException("Multiple distribution nodes with the scope " + scope);
		}

		// sweep the graph to find the reproduced and the original elements
		Set<Task> offspringTasks = new HashSet<>();
		Set<Dependency> offspringDependencies = new HashSet<>();
		Task startNode = dNodes.iterator().next();
		recSweepReproducedGraphSection(startNode, offspringTasks, offspringDependencies, scope);
		// add the original edges (vertices added automatically)
		offspringDependencies.forEach(dependency -> addOriginalEdge(dependency, scope));
		// remove the offsprings
		offspringDependencies.forEach(dependency -> graph.removeEdge(dependency));
		offspringTasks.forEach(task -> graph.removeVertex(task));
	}

	/**
	 * Finds the original edge and the original end points corresponding to the
	 * given offspring edge and adds them to the graph.
	 * 
	 * @param offspringEdge the offspring edge
	 * @param graph         the enactment graph
	 * @param scope         the reproduction scope
	 */
	protected void addOriginalEdge(Dependency offspringEdge, String scope) {
		if (graph.containsEdge((Dependency) offspringEdge.getParent())) {
			return;
		}
		Task offspringSrc = graph.getSource(offspringEdge);
		Task offspringDst = graph.getDest(offspringEdge);
		Task originalSrc = isEndNodeInScope(offspringSrc, scope) ? offspringSrc : (Task) offspringSrc.getParent();
		if (originalSrc == null) {
			throw new IllegalStateException("The offspring " + offspringSrc + " has no parent.");
		}
		Task originalDst = isEndNodeInScope(offspringDst, scope) ? offspringDst : (Task) offspringDst.getParent();
		if (originalDst == null) {
			throw new IllegalStateException("The offspring " + offspringDst + " has no parent.");
		}
		Dependency originalEdge = (Dependency) offspringEdge.getParent();
		graph.addEdge(originalEdge, originalSrc, originalDst, EdgeType.DIRECTED);
	}

	/**
	 * Recursively applied method realizing a traversal of the graph section created
	 * by the reproduction indicated by the provided scope.
	 * 
	 * @param currentNode the currently processed node
	 * @param originals   the set of the pre-reproduction elements
	 * @param offsprings  the set of the reproduction results
	 * @param graph       the enacment graph (post-reproduction state)
	 * @param scope       the reproduction scope
	 */
	protected void recSweepReproducedGraphSection(Task currentNode, Set<Task> offspringTasks,
			Set<Dependency> offspringDependencies, String scope) {
		if (isEndNodeInScope(currentNode, scope, false)) {
			// aggregation node as the base case
			return;
		} else {
			if (!isEndNodeInScope(currentNode, scope, true)) {
				// anything which is not a distribution node is an offspring
				offspringTasks.add(currentNode);
			}
			// all out edges are offsprings
			for (Dependency outEdge : graph.getOutEdges(currentNode)) {
				offspringDependencies.add(outEdge);
				Task nextNode = graph.getDest(outEdge);
				recSweepReproducedGraphSection(nextNode, offspringTasks, offspringDependencies, scope);
			}
		}
	}

	/**
	 * Returns true if the given task is either a distribution of an aggregation
	 * node in the current scope.
	 * 
	 * @param task  the task to check
	 * @param scope the considered reproduction scope
	 * @return true if the given task is either a distribution of an aggregation
	 *         node in the current scope
	 */
	protected boolean isEndNodeInScope(Task task, String scope) {
		return isEndNodeInScope(task, scope, false) || isEndNodeInScope(task, scope, true);
	}

	/**
	 * Returns true if the given task is either a distribution of an aggregation
	 * node in the current scope.
	 * 
	 * @param task         the task to check
	 * @param scope        the considered reproduction scope
	 * @param distribution true if we check for distribution nodes, false in case of
	 *                     aggregation
	 * @return true if the given task is either a distribution of an aggregation
	 *         node in the current scope
	 */
	protected boolean isEndNodeInScope(Task task, String scope, boolean distribution) {
		boolean collectionNode = distribution ? PropertyServiceFunctionDataFlowCollections.isDistributionNode(task)
				: PropertyServiceFunctionDataFlowCollections.isAggregationNode(task);
		return collectionNode && scope.equals(PropertyServiceFunctionDataFlowCollections.getScope(task));
	}

	/**
	 * Returns true if the reproduction indicated by the provided scope is ready to
	 * be reverted (which is the case of all of its aggregators have content
	 * available).
	 * 
	 * @param graph the enactment graph
	 * @param scope the reproduction scope
	 * @return true if the reproduction indicated by the provided scope is ready to
	 *         be reverted
	 */
	protected boolean readyForRevert(String scope) {
		// get the aggregators
		Set<Task> aggregators = graph.getVertices().stream()
				.filter(task -> PropertyServiceFunctionDataFlowCollections.isAggregationNode(task)
						&& PropertyServiceFunctionDataFlowCollections.getScope(task).equals(scope))
				.collect(Collectors.toSet());
		return aggregators.stream().allMatch(
				aggregator -> PropertyServiceFunction.getEnactable(aggregator).getState().equals(State.FINISHED));
	}

	/**
	 * Reproduces parts of the graph to model the parallel processing of distributed
	 * data. Scans the graph and finds the part between the distribution node and
	 * its aggregators. Creates i (i = number of parallel loop iterations) copies of
	 * the subgraph in between and adds them to the graph. Each original element of
	 * the graph is annotated as parent of each offspring. The parents are removed
	 * from the graph.
	 * 
	 * @param graph            the enactment graph
	 * @param distributionTask the distribution task
	 */
	public synchronized void applyDistributionReproduction(Task distributionTask) {
		int iterationNum = PropertyServiceFunctionDataFlowCollections.getIterationNumber(distributionTask);
		String scope = PropertyServiceFunctionDataFlowCollections.getScope(distributionTask);
		// find all edges which are relevant for the reproduction
		Set<Dependency> edgesToReproduce = findEdgesToReproduce(distributionTask);

		// reproduce each of the edges, while keeping track of the new nodes in the
		// graph
		Set<Task> tasksBeforeReproduction = graph.getVertices().stream()
				.filter(task -> TaskPropertyService.isProcess(task)).collect(Collectors.toSet());
		for (Dependency originalEdge : edgesToReproduce) {
			reproduceEdge(originalEdge, iterationNum, scope);
		}
		Set<Task> newTasks = graph.getVertices().stream()
				.filter(task -> TaskPropertyService.isProcess(task) && !tasksBeforeReproduction.contains(task))
				.collect(Collectors.toSet());
		// annotate the enactables for each new task
		newTasks.forEach(task -> UtilsManagement.annotateTaskEnactable(task, graph, enactableFactory));

		// remove the original elements
		removeOriginalElements(edgesToReproduce, scope);
	}
	
//	/**
//	 * Reproduces and annotates the enactable of the given offspring task.
//	 * 
//	 * @param offspring the offspring task.
//	 */
//	protected void reproduceTaskEnactable(Task offspring) {
//		UtilsManagement.annotateTaskEnactable(offspring, graph, enactableFactory);
//		Enactable offspringEnactable = PropertyServiceFunction.getEnactable(offspring);
//		Enactable parentEnactable = PropertyServiceFunction.getEnactable((Task) offspring.getParent());
//		parentEnactable.
//	}

	/**
	 * Removes the original elements from the graph.
	 * 
	 * @param graph           the enactment graph
	 * @param reproducedEdges the set of original edges between the distribution
	 *                        node and its aggregators
	 * @param scope           the reproduction scope
	 */
	protected void removeOriginalElements(Set<Dependency> reproducedEdges, String scope) {
		// gather the vertices to remove
		Set<Task> verticesToRemove = reproducedEdges.stream().map(edge -> graph.getSource(edge))
				.collect(Collectors.toSet());
		verticesToRemove.addAll(reproducedEdges.stream().map(edge -> graph.getDest(edge)).collect(Collectors.toSet()));
		Predicate<Task> distOrAggrPredicate = task -> {
			if (PropertyServiceFunctionDataFlowCollections.isAggregationNode(task)
					|| PropertyServiceFunctionDataFlowCollections.isDistributionNode(task)) {
				return scope.equals(PropertyServiceFunctionDataFlowCollections.getScope(task));
			} else {
				return false;
			}
		};
		verticesToRemove.removeIf(distOrAggrPredicate);
		// remove the edges
		reproducedEdges.stream().forEach(dependency -> graph.removeEdge(dependency));
		// remove the vertices
		verticesToRemove.stream().forEach(task -> graph.removeVertex(task));
	}

	/**
	 * Reproduces the given edge the requested amount of times. Adds the offspring
	 * edge and end points to the graph.
	 * 
	 * @param originalEdge the original edge
	 * @param graph        the enactment graph
	 * @param iterationNum the iteration number
	 */
	protected void reproduceEdge(Dependency originalEdge, int iterationNum, String scope) {
		Task originalSrc = graph.getSource(originalEdge);
		Task originalDst = graph.getDest(originalEdge);

		for (int reproductionIdx = 0; reproductionIdx < iterationNum; reproductionIdx++) {

			Optional<Task> offspringSrc = null;
			String jsonKey = PropertyServiceDependency.getJsonKey(originalEdge);
			Optional<Task> offspringDst = null;

			// assign src
			if (PropertyServiceFunctionDataFlowCollections.isDistributionNode(originalSrc)
					&& PropertyServiceFunctionDataFlowCollections.getScope(originalSrc).equals(scope)) {
				// edge from distribution node
				offspringSrc = Optional.of(originalSrc);
				String collectionName = PropertyServiceDependency.getJsonKey(originalEdge);
				jsonKey = ConstantsEEModel.getCollectionElementKey(collectionName, reproductionIdx);
			} else {
				// src needs to be reproduced
				offspringSrc = reproduceNode(originalSrc, reproductionIdx);
			}

			if (PropertyServiceFunctionDataFlowCollections.isAggregationNode(originalDst)
					&& PropertyServiceFunctionDataFlowCollections.getScope(originalDst).equals(scope)) {
				// edge to aggregation node
				offspringDst = Optional.of(originalDst);
				jsonKey = ConstantsEEModel.getCollectionElementKey(ConstantsEEModel.JsonKeyAggregation,
						reproductionIdx);
			} else {
				// dst needs to be reproduced
				offspringDst = reproduceNode(originalDst, reproductionIdx);
			}
			PropertyServiceDependency.addDataDependencyOffspring(offspringSrc.get(), offspringDst.get(), jsonKey, graph,
					originalEdge);
		}
	}

	/**
	 * Reproduces the given node and returns an optional of the offspring with the
	 * given reproduction index.
	 * 
	 * @param original        the node to reproduce
	 * @param reproductionIdx the reproduction index
	 * @param graph           the enactment graph
	 * @return an optional of the offspring with the given reproduction index
	 */
	protected Optional<Task> reproduceNode(Task original, int reproductionIdx) {
		String offspringId = getReproducedId(original.getId(), reproductionIdx);
		Task offspring = Optional.ofNullable(graph.getVertex(offspringId)).orElseGet(() -> {
			Task task = TaskPropertyService.isCommunication(original) ? new Communication(offspringId)
					: new Task(offspringId);
			task.setParent(original);
			return task;
		});
		return Optional.of(offspring);
	}

	/**
	 * Returns the edges which are relevant for the reproductions starting from the
	 * provided distribution node.
	 * 
	 * @param graph            the enactment graph
	 * @param distributionNode the distribution node
	 * @return the edges which are relevant for the reproductions starting from the
	 *         provided distribution node
	 */
	protected Set<Dependency> findEdgesToReproduce(Task distributionNode) {
		Set<Dependency> result = new HashSet<>();
		Task curNode = distributionNode;
		String scope = PropertyServiceFunctionDataFlowCollections.getScope(distributionNode);
		Set<Task> visited = new HashSet<>();
		recProcessOutEdgesNode(curNode, scope, visited, result);
		return result;
	}

	/**
	 * Recursive operation to check a node and gather all of its out edges which are
	 * relevant for reproduction.
	 * 
	 * @param curNode the node to check
	 * @param graph   the enactment graph
	 * @param scope   the reproduction scope
	 * @param visited the set of visited nodes
	 * @param result  the edges gathered so far
	 */
	protected void recProcessOutEdgesNode(Task curNode, String scope, Set<Task> visited, Set<Dependency> result) {
		visited.add(curNode);
		if (PropertyServiceFunctionDataFlowCollections.isAggregationNode(curNode)
				&& PropertyServiceFunctionDataFlowCollections.getScope(curNode).equals(scope)) {
			// recursion base case: arrival at an aggregation node.
			return;
		} else {
			for (Dependency outEdge : graph.getOutEdges(curNode)) {
				result.add(outEdge);
				Task dest = graph.getDest(outEdge);
				if (!visited.contains(dest)) {
					recProcessOutEdgesNode(dest, scope, visited, result);
				}
			}
		}
	}

	/**
	 * Generates the id for the offspring with the given idx
	 * 
	 * @param originalId      the id of the parent
	 * @param reproductionIdx the idx of the offspring.
	 * @return
	 */
	protected String getReproducedId(String originalId, int reproductionIdx) {
		return originalId + ConstantsEEModel.KeyWordSeparator2 + reproductionIdx;
	}
}

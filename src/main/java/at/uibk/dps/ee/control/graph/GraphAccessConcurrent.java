package at.uibk.dps.ee.control.graph;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

/**
 * Implements a threat-safe run-time access to the enactment graph based on a
 * ReadWriteLock.
 * 
 * @author Fedor Smirnov
 */
public class GraphAccessConcurrent implements GraphAccess {

	protected final EnactmentGraph graph;
	protected final ReadWriteLock readWriteLock;
	protected final Lock readLock;
	protected final Lock writeLock;

	public GraphAccessConcurrent(EnactmentGraphProvider graphProvider) {
		this.graph = graphProvider.getEnactmentGraph();
		this.readWriteLock = new ReentrantReadWriteLock();
		this.readLock = readWriteLock.readLock();
		this.writeLock = readWriteLock.writeLock();
	}

	@Override
	public Set<EdgeTupleAppl> getOutEdges(Task node) {
		try {
			readLock.lock();
			return graph.getOutEdges(node).stream().map(edge -> getEdgeTupleForEdge(edge)).collect(Collectors.toSet());
		} finally {
			readLock.unlock();
		}
	}

	/**
	 * Summarizes the edge to an edgeTuple
	 * 
	 * @param edge the edge to summarize
	 * @return the edgeTuple of the edge
	 */
	protected EdgeTupleAppl getEdgeTupleForEdge(Dependency edge) {
		Task src = graph.getSource(edge);
		Task dst = graph.getDest(edge);
		return new EdgeTupleAppl(src, dst, edge);
	}

	@Override
	public void writeOperationNodeInEdges(BiConsumer<Set<Dependency>, Task> writeOperation, Task node) {
		try {
			writeLock.lock();
			Set<Dependency> inEdges = new HashSet<>(graph.getInEdges(node));
			writeOperation.accept(inEdges, node);
		}finally {
			writeLock.unlock();
		}

	}

	@Override
	public Set<Task> getRootDataNodes() {
		try {
			readLock.lock();
			Set<Task> result = graph.getVertices().stream().filter(task -> graph.getInEdges(task).size() == 0)
					.collect(Collectors.toSet());
			if (result.stream().anyMatch(task -> !PropertyServiceData.isRoot(task))) {
				throw new IllegalStateException("Non-root nodes without in edges present.");
			}
			return result;
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Set<Task> getLeafDataNodes() {
		try {
			readLock.lock();
			Set<Task> result = graph.getVertices().stream().filter(task -> graph.getOutEdges(task).size() == 0)
					.collect(Collectors.toSet());
			if (result.stream().anyMatch(task -> !PropertyServiceData.isLeaf(task))) {
				throw new IllegalStateException("Non-root nodes without in edges present.");
			}
			return result;
		} finally {
			readLock.unlock();
		}
	}

}

package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependencyControlIf;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilsManagementTest {

	@Test
	public void testTask2EnactableMapGeneration() {
		
		Communication input1 = new Communication("input1");
		Communication input2 = new Communication("input2");
		Communication input3 = new Communication("input3");
		Communication output = new Communication("output");

		String key1 = "key1";
		String key2 = "key2";
		String key3 = "key3";
		String key4 = "key4";

		Task task = new Task("task");
		Dependency inEdge1 = PropertyServiceDependency.createDataDependency(input1, task, key1);
		Dependency inEdge2 = PropertyServiceDependency.createDataDependency(input2, task, key2);
		Dependency inEdge3 = PropertyServiceDependencyControlIf.createControlIfDependency(input3, task, key3, false);
		Dependency outEdge = PropertyServiceDependency.createDataDependency(task, output, key4);

		EnactmentGraph graph = new EnactmentGraph();
		graph.addEdge(inEdge1, input1, task, EdgeType.DIRECTED);
		graph.addEdge(inEdge2, input2, task, EdgeType.DIRECTED);
		graph.addEdge(inEdge3, input3, task, EdgeType.DIRECTED);
		graph.addEdge(outEdge, task, output, EdgeType.DIRECTED);
		
		Set<String> inputKeys = new HashSet<>();
		inputKeys.add(key1);
		inputKeys.add(key2);
		inputKeys.add(key3);
		
		EnactableFactory factoryMock = mock(EnactableFactory.class);
		EnactableAtomic enactableMock = mock(EnactableAtomic.class);
		when(enactableMock.getState()).thenReturn(State.STOPPED);
		when(factoryMock.createEnactable(task, inputKeys)).thenReturn(enactableMock);
		
		Map<Task, EnactableAtomic> result = UtilsManagement.generateTask2EnactableMap(graph, factoryMock);
		assertEquals(1, result.size());
		assertEquals(enactableMock, result.get(task));
		assertEquals(State.STOPPED, PropertyServiceFunction.getEnactableState(task));
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testGetLeavesExc() {
		Communication output = new Communication("output");

		String key4 = "key4";

		Task task = new Task("task");
		Task task2 = new Task("task2");
		Dependency outEdge = PropertyServiceDependency.createDataDependency(task, output, key4);
		Dependency outEdge2 = PropertyServiceDependency.createDataDependency(output, task2, key4);

		PropertyServiceData.makeLeaf(output);

		EnactmentGraph graph = new EnactmentGraph();
		graph.addEdge(outEdge, task, output, EdgeType.DIRECTED);
		graph.addEdge(outEdge2, output, task2, EdgeType.DIRECTED);

		UtilsManagement.getLeafNodes(graph);
	}

	@Test
	public void testGetLeaves() {
		Communication input1 = new Communication("input1");
		Communication input2 = new Communication("input2");
		Communication input3 = new Communication("input3");
		Communication output = new Communication("output");
		Communication output2 = new Communication("output2");
		Communication output3 = new Communication("output3");

		String key1 = "key1";
		String key2 = "key2";
		String key3 = "key3";
		String key4 = "key4";
		String key5 = "key5";
		String key6 = "key6";

		Task task = new Task("task");
		Dependency inEdge1 = PropertyServiceDependency.createDataDependency(input1, task, key1);
		Dependency inEdge2 = PropertyServiceDependency.createDataDependency(input2, task, key2);
		Dependency inEdge3 = PropertyServiceDependencyControlIf.createControlIfDependency(input3, task, key3, false);
		Dependency outEdge = PropertyServiceDependency.createDataDependency(task, output, key4);
		Dependency outEdge2 = PropertyServiceDependency.createDataDependency(task, output2, key5);
		Dependency outEdge3 = PropertyServiceDependency.createDataDependency(task, output3, key6);

		PropertyServiceData.makeLeaf(output2);
		PropertyServiceData.makeLeaf(output);

		EnactmentGraph graph = new EnactmentGraph();
		graph.addEdge(inEdge1, input1, task, EdgeType.DIRECTED);
		graph.addEdge(inEdge2, input2, task, EdgeType.DIRECTED);
		graph.addEdge(inEdge3, input3, task, EdgeType.DIRECTED);
		graph.addEdge(outEdge, task, output, EdgeType.DIRECTED);
		graph.addEdge(outEdge2, task, output2, EdgeType.DIRECTED);
		graph.addEdge(outEdge3, task, output3, EdgeType.DIRECTED);

		Set<Task> leaves = UtilsManagement.getLeafNodes(graph);
		assertEquals(2, leaves.size());
		assertTrue(leaves.contains(output));
		assertTrue(leaves.contains(output2));
	}

	@Test
	public void testGetInputKeys() {

		Communication input1 = new Communication("input1");
		Communication input2 = new Communication("input2");
		Communication input3 = new Communication("input3");
		Communication output = new Communication("output");

		String key1 = "key1";
		String key2 = "key2";
		String key3 = "key3";
		String key4 = "key4";

		Task task = new Task("task");
		Dependency inEdge1 = PropertyServiceDependency.createDataDependency(input1, task, key1);
		Dependency inEdge2 = PropertyServiceDependency.createDataDependency(input2, task, key2);
		Dependency inEdge3 = PropertyServiceDependencyControlIf.createControlIfDependency(input3, task, key3, false);
		Dependency outEdge = PropertyServiceDependency.createDataDependency(task, output, key4);

		EnactmentGraph graph = new EnactmentGraph();
		graph.addEdge(inEdge1, input1, task, EdgeType.DIRECTED);
		graph.addEdge(inEdge2, input2, task, EdgeType.DIRECTED);
		graph.addEdge(inEdge3, input3, task, EdgeType.DIRECTED);
		graph.addEdge(outEdge, task, output, EdgeType.DIRECTED);

		Set<String> result = UtilsManagement.getInputKeys(task, graph);
		assertEquals(3, result.size());
		assertTrue(result.contains(key1));
		assertTrue(result.contains(key2));
		assertTrue(result.contains(key3));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInputKeysComm() {
		Communication comm = new Communication("comm");
		EnactmentGraph graph = new EnactmentGraph();
		UtilsManagement.getInputKeys(comm, graph);
	}
}

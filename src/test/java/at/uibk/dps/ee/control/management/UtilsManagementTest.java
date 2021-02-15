package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
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
	public void testAnnotateTaskEnactables() {

		Communication input1 = new Communication("input1");
		Communication input2 = new Communication("input2");
		Communication input3 = new Communication("input3");
		Communication output = new Communication("output");

		String key1 = "key1";
		String key2 = "key2";
		String key3 = "key3";
		String key4 = "key4";

		Task task = new Task("task");
		Dependency inEdge3 = PropertyServiceDependencyControlIf.createControlIfDependency(input3, task, key3, false);

		EnactmentGraph graph = new EnactmentGraph();
		PropertyServiceDependency.addDataDependency(input1, task, key1, graph);
		PropertyServiceDependency.addDataDependency(input2, task, key2, graph);
		PropertyServiceDependency.addDataDependency(task, output, key4, graph);
		graph.addEdge(inEdge3, input3, task, EdgeType.DIRECTED);

		EnactableFactory factoryMock = mock(EnactableFactory.class);
		EnactableAtomic enactableMock = mock(EnactableAtomic.class);
		when(enactableMock.getState()).thenReturn(State.STOPPED);
		when(factoryMock.createEnactable(task)).thenReturn(enactableMock);

		UtilsManagement.annotateTaskEnactables(graph, factoryMock);
		assertEquals(enactableMock, PropertyServiceFunction.getEnactable(task));
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
		Dependency inEdge3 = PropertyServiceDependencyControlIf.createControlIfDependency(input3, task, key3, false);

		EnactmentGraph graph = new EnactmentGraph();
		PropertyServiceDependency.addDataDependency(input1, task, key1, graph);
		PropertyServiceDependency.addDataDependency(input2, task, key2, graph);
		PropertyServiceDependency.addDataDependency(task, output, key4, graph);
		graph.addEdge(inEdge3, input3, task, EdgeType.DIRECTED);

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

package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.Test;

import com.google.gson.JsonParser;

import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceDependencyControlIf;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import edu.uci.ics.jung.graph.util.EdgeType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

public class DataLogisticsTest {

	@Test
	public void testIsWfFinished() {
		Task data1 = new Communication("data1");
		Task data2 = new Communication("data2");
		Task data3 = new Communication("data3");

		PropertyServiceData.makeLeaf(data1);
		PropertyServiceData.makeLeaf(data2);

		PropertyServiceData.setDataType(data1, DataType.String);
		PropertyServiceData.setContent(data1, JsonParser.parseString("bla"));

		PropertyServiceData.setDataType(data3, DataType.Number);
		PropertyServiceData.setContent(data3, JsonParser.parseString("10.1"));

		PropertyServiceData.setDataType(data2, DataType.Number);

		EnactmentGraph graph = new EnactmentGraph();
		graph.addVertex(data1);
		graph.addVertex(data2);
		graph.addVertex(data3);

		EnactmentGraphProvider graphProvider = mock(EnactmentGraphProvider.class);
		when(graphProvider.getEnactmentGraph()).thenReturn(graph);
		EnactableFactory factoryMock = mock(EnactableFactory.class);
		DataLogistics tested = new DataLogistics(graphProvider, factoryMock);

		assertFalse(tested.isWfFinished());
		PropertyServiceData.setContent(data2, JsonParser.parseString("5.0"));
		assertTrue(tested.isWfFinished());
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

		EnactmentGraphProvider providerMock = mock(EnactmentGraphProvider.class);
		when(providerMock.getEnactmentGraph()).thenReturn(graph);
		EnactableFactory factoryMock = mock(EnactableFactory.class);
		DataLogistics tested = new DataLogistics(providerMock, factoryMock);

		Set<Task> leaves = tested.getLeafNodes(graph);
		assertEquals(2, leaves.size());
		assertTrue(leaves.contains(output));
		assertTrue(leaves.contains(output2));
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

		EnactmentGraphProvider providerMock = mock(EnactmentGraphProvider.class);
		when(providerMock.getEnactmentGraph()).thenReturn(graph);
		EnactableFactory factoryMock = mock(EnactableFactory.class);
		DataLogistics tested = new DataLogistics(providerMock, factoryMock);

		tested.getLeafNodes(graph);
	}
}

package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import at.uibk.dps.ee.core.ModelModificationListener;
import at.uibk.dps.ee.core.enactable.Enactable;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.enactables.EnactableAtomic;
import at.uibk.dps.ee.enactables.EnactableFactory;
import at.uibk.dps.ee.enactables.local.dataflow.Aggregation;
import at.uibk.dps.ee.model.constants.ConstantsEEModel;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceDependency;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections;
import at.uibk.dps.ee.model.properties.PropertyServiceFunction.UsageType;
import at.uibk.dps.ee.model.properties.PropertyServiceFunctionDataFlowCollections.OperationType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Dependency;
import net.sf.opendse.model.Task;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GraphModifierTest {

	@Test
	public void testFindRelevantEdges() {
		// create the input graph
		String scopeName = "scope";
		String collNameIn = "collectionInput";
		String funcInName = "funcIn";
		String funcName = "function";
		String funcOutName = "funcOut";

		EnactmentGraph testInput = new EnactmentGraph();

		Communication wfInput = new Communication("input");
		PropertyServiceData.makeRoot(wfInput);

		Communication wfOutput = new Communication("output");
		PropertyServiceData.makeLeaf(wfOutput);

		Task distributionNode = PropertyServiceFunctionDataFlowCollections.createCollectionDataFlowTask("distribution",
				OperationType.Distribution, scopeName);
		PropertyServiceFunctionDataFlowCollections.setIterationNumber(distributionNode, 3);

		Task function = new Task(funcName);
		PropertyServiceFunction.setUsageType(UsageType.Local, function);

		Task aggregation = PropertyServiceFunctionDataFlowCollections.createCollectionDataFlowTask("aggregation",
				OperationType.Aggregation, scopeName);

		Communication distributedData = new Communication("distributedData");
		Communication functionResult = new Communication("funcResult");

		Communication outsideInput = new Communication("outsideIn");
		String jsonKeyOutside = "outside";

		PropertyServiceDependency.addDataDependency(wfInput, distributionNode, collNameIn, testInput);
		PropertyServiceDependency.addDataDependency(distributionNode, distributedData, collNameIn, testInput);
		PropertyServiceDependency.addDataDependency(distributedData, function, funcInName, testInput);
		PropertyServiceDependency.addDataDependency(function, functionResult, funcOutName, testInput);
		PropertyServiceDependency.addDataDependency(functionResult, aggregation, ConstantsEEModel.JsonKeyAggregation,
				testInput);
		PropertyServiceDependency.addDataDependency(aggregation, wfOutput, ConstantsEEModel.JsonKeyAggregation,
				testInput);
		PropertyServiceDependency.addDataDependency(outsideInput, function, jsonKeyOutside, testInput);
		EnactmentGraphProvider providerMock = mock(EnactmentGraphProvider.class);
		when(providerMock.getEnactmentGraph()).thenReturn(testInput);
		EnactableFactory factoryMock = mock(EnactableFactory.class);
		Set<ModelModificationListener> listeners = new HashSet<>();
		GraphModifier tested = new GraphModifier(providerMock, factoryMock, listeners);
		Set<Dependency> result = tested.findEdgesToReproduce(distributionNode);
		assertEquals(5, result.size());
	}

	@Test
	public void testDistReproduction() {

		// create the input graph
		String scopeName = "scope";
		String collNameIn = "collectionInput";
		String funcInName = "funcIn";
		String funcName = "function";
		String funcOutName = "funcOut";

		EnactmentGraph testInput = new EnactmentGraph();

		Communication wfInput = new Communication("input");
		PropertyServiceData.makeRoot(wfInput);

		Communication wfOutput = new Communication("output");
		PropertyServiceData.makeLeaf(wfOutput);

		Task distributionNode = PropertyServiceFunctionDataFlowCollections.createCollectionDataFlowTask("distribution",
				OperationType.Distribution, scopeName);
		PropertyServiceFunctionDataFlowCollections.setIterationNumber(distributionNode, 3);

		Task function = new Task(funcName);
		PropertyServiceFunction.setUsageType(UsageType.Local, function);

		Task aggregation = PropertyServiceFunctionDataFlowCollections.createCollectionDataFlowTask("aggregation",
				OperationType.Aggregation, scopeName);

		String funcInDataName = "distributedData";
		String funcOutDataName = "funcResult";
		Communication distributedData = new Communication(funcInDataName);
		Communication functionResult = new Communication(funcOutDataName);

		PropertyServiceDependency.addDataDependency(wfInput, distributionNode, collNameIn, testInput);
		PropertyServiceDependency.addDataDependency(distributionNode, distributedData, collNameIn, testInput);
		PropertyServiceDependency.addDataDependency(distributedData, function, funcInName, testInput);
		PropertyServiceDependency.addDataDependency(function, functionResult, funcOutName, testInput);
		PropertyServiceDependency.addDataDependency(functionResult, aggregation, ConstantsEEModel.JsonKeyAggregation,
				testInput);
		PropertyServiceDependency.addDataDependency(aggregation, wfOutput, ConstantsEEModel.JsonKeyAggregation,
				testInput);
		Communication outsideInput = new Communication("outsideIn");
		String jsonKeyOutside = "outside";
		PropertyServiceDependency.addDataDependency(outsideInput, function, jsonKeyOutside, testInput);

		// run the reproduction
		EnactmentGraphProvider providerMock = mock(EnactmentGraphProvider.class);
		when(providerMock.getEnactmentGraph()).thenReturn(testInput);
		EnactableFactory factoryMock = mock(EnactableFactory.class);
		Set<ModelModificationListener> listeners = new HashSet<>();
		Enactable mockEnactable = mock(EnactableAtomic.class);
		Aggregation mockAggregation = mock(Aggregation.class);
		PropertyServiceFunction.setEnactable(function, mockEnactable);
		PropertyServiceFunction.setEnactable(aggregation, mockAggregation);
		GraphModifier tested = new GraphModifier(providerMock, factoryMock, listeners);

		tested.applyDistributionReproduction(distributionNode);
		// do the tests

		// element numbers
		assertEquals(14, testInput.getVertexCount());
		assertEquals(17, testInput.getEdgeCount());

		Task funcIn2 = testInput.getVertex(tested.getReproducedId(funcInDataName, 2));
		assertNotNull(funcIn2);
		Task func2 = testInput.getVertex(tested.getReproducedId(funcName, 2));
		assertNotNull(func2);
		Task funcOut2 = testInput.getVertex(tested.getReproducedId(funcOutDataName, 2));
		assertNotNull(funcOut2);

		Dependency distributionEdge = testInput.getInEdges(funcIn2).iterator().next();
		Dependency aggregationEdge = testInput.getOutEdges(funcOut2).iterator().next();
		Dependency funcOutEdge = testInput.getOutEdges(func2).iterator().next();

		// check the JSON keys
		String expectedDistJson = ConstantsEEModel.getCollectionElementKey(collNameIn, 2);
		String expectedAggrJson = ConstantsEEModel.getCollectionElementKey(ConstantsEEModel.JsonKeyAggregation, 2);
		String expectedFuncOutJson = funcOutName;

		assertEquals(expectedDistJson, PropertyServiceDependency.getJsonKey(distributionEdge));
		assertEquals(expectedAggrJson, PropertyServiceDependency.getJsonKey(aggregationEdge));
		assertEquals(expectedFuncOutJson, PropertyServiceDependency.getJsonKey(funcOutEdge));

		assertEquals(function, func2.getParent());
		assertEquals(distributedData, funcIn2.getParent());
		assertEquals(functionResult, funcOut2.getParent());

		Enactable enactableMock = mock(Enactable.class);
		when(enactableMock.getState()).thenReturn(State.WAITING);
		PropertyServiceFunction.setEnactable(aggregation, enactableMock);

		// test the reverse operation when enactable not ready
		tested.revertDistributionReproduction(scopeName);
		assertEquals(14, testInput.getVertexCount());
		assertEquals(17, testInput.getEdgeCount());

		when(enactableMock.getState()).thenReturn(State.FINISHED);

		// test the reverse operation when enactable ready

		tested.revertDistributionReproduction(scopeName);
		assertEquals(8, testInput.getVertexCount());
		assertEquals(7, testInput.getEdgeCount());
	}
}

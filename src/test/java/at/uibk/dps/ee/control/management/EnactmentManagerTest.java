package at.uibk.dps.ee.control.management;

import static org.junit.Assert.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.google.gson.JsonParser;

import at.uibk.dps.ee.control.command.Control;
import at.uibk.dps.ee.core.EnactmentState;
import at.uibk.dps.ee.core.enactable.Enactable.State;
import at.uibk.dps.ee.core.enactable.EnactableStateListener;
import at.uibk.dps.ee.core.exception.StopException;
import at.uibk.dps.ee.model.graph.EnactmentGraph;
import at.uibk.dps.ee.model.graph.EnactmentGraphProvider;
import at.uibk.dps.ee.model.properties.PropertyServiceData;
import at.uibk.dps.ee.model.properties.PropertyServiceData.DataType;
import net.sf.opendse.model.Communication;
import net.sf.opendse.model.Task;

public class EnactmentManagerTest {

	@Test
	public void reactToStateChangePausedRunning() {
		Set<EnactableStateListener> stateListeners = new HashSet<>();
		EnactmentGraph graph = new EnactmentGraph();
		EnactmentGraphProvider graphProvider = mock(EnactmentGraphProvider.class);
		when(graphProvider.getEnactmentGraph()).thenReturn(graph);
		Control control = mock(Control.class);
		EnactmentManager tested = new EnactmentManager(stateListeners, graphProvider, control);
		EnactmentManager spy = spy(tested);

		try {
			spy.reactToStateChange(EnactmentState.PAUSED, EnactmentState.RUNNING);
		} catch (StopException e) {
			fail();
		}

		assertEquals(State.RUNNING, spy.getState());
	}
	
	@Test
	public void reactToStateChangeRunningPaused() {
		Set<EnactableStateListener> stateListeners = new HashSet<>();
		EnactmentGraph graph = new EnactmentGraph();
		EnactmentGraphProvider graphProvider = mock(EnactmentGraphProvider.class);
		when(graphProvider.getEnactmentGraph()).thenReturn(graph);
		Control control = mock(Control.class);
		EnactmentManager tested = new EnactmentManager(stateListeners, graphProvider, control);
		EnactmentManager spy = spy(tested);

		try {
			spy.reactToStateChange(EnactmentState.RUNNING, EnactmentState.PAUSED);
		} catch (StopException e) {
			fail();
		}

		verify(spy).pause();
		assertEquals(State.PAUSED, spy.getState());
	}

	@Test
	public void testWfFinished() {
		Set<EnactableStateListener> stateListeners = new HashSet<>();

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

		Control control = mock(Control.class);

		EnactmentManager tested = new EnactmentManager(stateListeners, graphProvider, control);

		assertFalse(tested.wfFinished());
		PropertyServiceData.setContent(data2, JsonParser.parseString("5.0"));
		assertTrue(tested.wfFinished());
	}
}

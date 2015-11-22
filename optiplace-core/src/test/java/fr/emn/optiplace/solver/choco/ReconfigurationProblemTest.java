package fr.emn.optiplace.solver.choco;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.view.access.CoreView;

public class ReconfigurationProblemTest {

	@Test
	public void testWaiting() {
		SimpleConfiguration sc = new SimpleConfiguration();
		VM vm = sc.addVM("vm", null);
		ReconfigurationProblem p = new ReconfigurationProblem(sc);
		Assert.assertTrue(p.findSolution());
		Assert.assertEquals(p.getState(vm).getValue(), CoreView.VM_WAITING);
	}

	@Test
	public void testRunning() {
		SimpleConfiguration sc = new SimpleConfiguration();
		Node n = sc.addOnline("node");
		VM vm = sc.addVM("vm", n);

		ReconfigurationProblem p = new ReconfigurationProblem(sc);
		Assert.assertTrue(p.findSolution());
		Assert.assertEquals(p.getState(vm).getValue(), CoreView.VM_RUNNING);
	}

	@Test
	public void testExterned() {
		SimpleConfiguration sc = new SimpleConfiguration();
		Extern e = sc.addExtern("node");
		VM vm = sc.addVM("vm", e);

		ReconfigurationProblem p = new ReconfigurationProblem(sc);
		// p.getSolver().plugMonitor((IMonitorContradiction) cex ->
		// System.err.println(cex));
		Assert.assertTrue(p.findSolution());
		Assert.assertEquals(p.getState(vm).getValue(), CoreView.VM_EXTERNED);
	}

}

package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Solution;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.view.access.CoreView;

public class ReconfigurationProblemTest {

	@Test
	public void testWaiting() {
		Configuration sc = new Configuration();
		VM vm = sc.addVM("vm", null);
		ReconfigurationProblem p = new ReconfigurationProblem(sc);
		Solution sol = p.getSolver().findSolution();
		Assert.assertNotNull(sol);
		Assert.assertEquals(sol.getIntVal(p.getState(vm)), CoreView.VM_WAITING);
	}

	@Test
	public void testRunning() {
		Configuration sc = new Configuration();
		Node n = sc.addOnline("node");
		VM vm = sc.addVM("vm", n);

		ReconfigurationProblem p = new ReconfigurationProblem(sc);
		Solution sol = p.getSolver().findSolution();
		Assert.assertNotNull(sol);
		Assert.assertEquals(sol.getIntVal(p.getState(vm)), CoreView.VM_RUNNODE);
	}

	@Test
	public void testExterned() {
		Configuration sc = new Configuration();
		Extern e = sc.addExtern("node");
		VM vm = sc.addVM("vm", e);

		ReconfigurationProblem p = new ReconfigurationProblem(sc);
		// p.getSolver().plugMonitor((IMonitorContradiction) cex ->
		// System.err.println(cex));
		Solution sol = p.getSolver().findSolution();
		Assert.assertNotNull(sol);
		Assert.assertEquals(sol.getIntVal(p.getState(vm)), CoreView.VM_RUNEXT);
	}

}

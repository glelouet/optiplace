package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.access.CoreView;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class TestExternPlacement extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TestExternPlacement.class);

	/**
	 * 2 VM waiting. One is constrained externed, the other is constrained not
	 * waiting ; no node to host so they should be placed on the extern.
	 * 
	 * @throws ContradictionException
	 */
	@Test
	public void testIntantiateOneExtern() throws ContradictionException {
		SimpleConfiguration cfg = new SimpleConfiguration();
		VM vm1 = cfg.addVM("vm1", null);
		VM vm2 = cfg.addVM("vm2", null);
		Extern e = cfg.addExtern("extern");

		ReconfigurationProblem p = new ReconfigurationProblem(cfg);
		p.getSolver().plugMonitor((IMonitorContradiction) cex -> System.err.println(cex));
		p.getState(vm1).instantiateTo(CoreView.VM_EXTERNED, Cause.Null);
		p.getState(vm2).removeValue(CoreView.VM_WAITING, Cause.Null);

		Assert.assertTrue(p.findSolution());
		Assert.assertEquals(p.getExtern(vm1).getValue(), p.b().extern(e));
		Assert.assertEquals(p.getExtern(vm2).getValue(), p.b().extern(e));
	}

}

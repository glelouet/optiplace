/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.HashSet;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SpreadTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SpreadTest.class);

	@Test
	public void useCaseSpread() {
		Configuration cfg = new Configuration("mem");
		Node n0 = cfg.addOnline("n0", 64);
		Extern e = cfg.addExtern("e", 5);
		VM vm0 = cfg.addVM("vm0", null, 5);
		VM vm1 = cfg.addVM("vm1", null, 5);

		HAView ha = new HAView();
		ha.addRule(new Spread(vm0, vm1));

		Optiplace sp = new Optiplace(cfg);
		sp.views(ha);
		sp.solve();
		IConfiguration dest = sp.getTarget().getDestination();
		// System.err.println("" + dest);

		Assert.assertNotNull(dest);
		Assert.assertNotEquals(dest.getFutureLocation(vm0), dest.getFutureLocation(vm1),
				"vms are hosted on same hoster : " + dest.getFutureLocation(vm0));
		Assert.assertEquals(dest.nbHosted(n0), 1);
		Assert.assertEquals(dest.nbHosted(e), 1);
	}

	@Test
	public void testSimpleInject() {
		nbWaitings = 3;
		prepare();
		Spread c = new Spread(new HashSet<>(Arrays.asList(waitings)));
		IConfiguration d = solve(src, c).getDestination();
		HashSet<Node> hosters = new HashSet<>();
		for (VM v : waitings) {
			Assert.assertTrue(hosters.add(d.getNodeHost(v)));
		}
		Assert.assertEquals(hosters.size(), 3);
	}

	// FIXME
	/**
	 * a spread on 3 VMs, with 1 node and 2 extern. EAch VM should be placed on a
	 * different Node/extern.
	 */
	@Test
	public void testSpreadWithExtern() {
		nbWaitings = 0;
		nbVMPerNode = 3;
		nbNodes = 1;
		resources = null;
		prepare();
		Extern e1 = src.addExtern("ext1");
		Extern e2 = src.addExtern("ext2");

		IConfiguration d = solve(src, new Spread(this.placed[0])).getDestination();
		Assert.assertEquals(d.nbHosted(nodes[0]), 1, "dest is " + d);
		Assert.assertEquals(d.nbHosted(e1), 1, "dest is" + d);
		Assert.assertEquals(d.nbHosted(e2), 1, "dest is" + d);

	}

	@Test
	public void testParsing() {
		HashSet<VM> vms = new HashSet<>();
		vms.add(new VM("vm1"));
		vms.add(new VM("vm2"));
		Rule r = new Spread(vms);
		String s = r.toString();
		Spread parsed = Spread.parse(s);
		Assert.assertEquals(parsed, r);
	}

	/**
	 * 3 elements a b c to place, each is either in one of two bins of the 1-set
	 * (a1,b1,c1) or in the bin of the 2-set (a2,b2,c2) ; If the var is not in a
	 * set, its corresponding var is -1<br />
	 * eg if the element a is in the bin 0 of the 1-set, then a1=0 and a2=-1
	 * <br />
	 * if b is in the bin 0 of the 2-set, then b2=0 and b1=-1
	 */
	@Test
	public void testSimpleAllDiff() {
		Solver s = new Solver();
		IntVar a1 = VF.bounded("a1", -1, 1, s);
		IntVar a2 = VF.bounded("a2", -1, 0, s);
		LCF.ifThen(ICF.arithm(a1, "!=", -1), ICF.arithm(a2, "=", -1));
		LCF.ifThen(ICF.arithm(a2, "!=", -1), ICF.arithm(a1, "=", -1));

		IntVar b1 = VF.bounded("b1", -1, 1, s);
		IntVar b2 = VF.bounded("b2", -1, 0, s);
		LCF.ifThen(ICF.arithm(b1, "!=", -1), ICF.arithm(b2, "=", -1));
		LCF.ifThen(ICF.arithm(b2, "!=", -1), ICF.arithm(b1, "=", -1));

		IntVar c1 = VF.bounded("c1", -1, 1, s);
		IntVar c2 = VF.bounded("c2", -1, 0, s);
		LCF.ifThen(ICF.arithm(c1, "!=", -1), ICF.arithm(c2, "=", -1));
		LCF.ifThen(ICF.arithm(c2, "!=", -1), ICF.arithm(c1, "=", -1));

		s.post(ICF.alldifferent_conditionnal(new IntVar[] { a1, b1, c1 }, v -> v.getLB() > -1));
		s.post(ICF.alldifferent_conditionnal(new IntVar[] { a2, b2, c2 }, v -> v.getLB() > -1));
		Assert.assertTrue(s.findSolution());
	}
}

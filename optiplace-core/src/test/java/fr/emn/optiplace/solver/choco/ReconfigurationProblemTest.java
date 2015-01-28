/**
 *
 */
package fr.emn.optiplace.solver.choco;

import org.testng.Assert;
import org.testng.annotations.Test;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.center.configuration.VM;
import fr.emn.optiplace.configuration.SimpleConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class ReconfigurationProblemTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ReconfigurationProblemTest.class);

	Configuration src;
	Node n0, n1;
	VM vm0_0, vm0_1, vm1_0, vm1_1;
	ReconfigurationProblem pb;

	@org.testng.annotations.BeforeMethod
	public void prepare() {
		src = new SimpleConfiguration();
		n0 = src.addOnline("n0");
		n1 = src.addOnline("n1");
		vm0_0 = src.addVM("vm0_0", n0);
		vm0_1 = src.addVM("vm0_1", n0);
		vm1_0 = src.addVM("vm1_0", n1);
		vm1_1 = src.addVM("vm1_1", n1);
		pb = new ReconfigurationProblem(src);
	}

	@Test
	public void checkHosters() throws ContradictionException {
		pb.host(vm1_0).removeValue(pb.node(n0), Cause.Null);
		pb.host(vm1_1).removeValue(pb.node(n0), Cause.Null);
		Assert.assertTrue(pb.findSolution());
	}

	@Test(dependsOnMethods = "checkHosters")
	public void checkHosted() throws ContradictionException {
		pb.hosted(n0).removeFromEnvelope(pb.vm(vm1_0), Cause.Null);
		pb.hosted(n0).removeFromEnvelope(pb.vm(vm1_1), Cause.Null);
		Assert.assertTrue(pb.findSolution());
	}

	@Test(dependsOnMethods = "checkHosted")
	public void checknbVM() throws ContradictionException {
		pb.nbVM(n0).updateUpperBound(2, Cause.Null);
		Assert.assertTrue(pb.findSolution());
	}
}

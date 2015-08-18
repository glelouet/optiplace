/**
 *
 */

package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.view.access.CoreView;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class ReconfigurationProblemTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReconfigurationProblemTest.class);

	Configuration src;
	Node n0, n1;
	Node[] nodes;
	VM vm0_0, vm0_1, vm1_0, vm1_1;
	VM[] vms;
	ReconfigurationProblem pb;

	@BeforeMethod
	public void prepare() {
		src = new SimpleConfiguration();
		n0 = src.addOnline("n0");
		n1 = src.addOnline("n1");
		vm0_0 = src.addVM("vm0_0", n0);
		vm0_1 = src.addVM("vm0_1", n0);
		vm1_0 = src.addVM("vm1_0", n1);
		vm1_1 = src.addVM("vm1_1", n1);
		nodes = new Node[] {
		    n0, n1
		};
		vms = new VM[] {
		    vm0_0, vm0_1, vm1_0, vm1_1
		};
		pb = new ReconfigurationProblem(src);
	}

	@Test
	public void checkHosters() throws ContradictionException {
		pb.getNode(vm1_0).removeValue(pb.node(n0), Cause.Null);
		pb.getNode(vm1_1).removeValue(pb.node(n0), Cause.Null);
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

	@Test
	public void checkNoWaitingNoExternVMVariables() {
		for (VM vm : vms) {
			Assert.assertTrue(pb.getExtern(vm).isInstantiatedTo(-1), "" + vm + " extern is not -1 : " + pb.getExtern(vm));
			Assert.assertTrue(pb.getState(vm).isInstantiatedTo(CoreView.VM_RUNNING),
			    "" + vm + " state is not running : " + pb.getState(vm));
		}
	}
}

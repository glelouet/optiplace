
package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.view.access.CoreView;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class ReconfigurationProblemWaitingNoExternTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(ReconfigurationProblemWaitingNoExternTest.class);

	IConfiguration src;
	Node n0, n1;
	Node[] nodes;
	VM vm0, vm1, vm2;
	VM[] vms;
	ReconfigurationProblem pb;

	@BeforeMethod
	public void prepare() {
		src = new Configuration();
		n0 = src.addNode("n0");
		n1 = src.addNode("n1");
		vm0 = src.addVM("vm0", null);
		vm1 = src.addVM("vm1", null);
		vm2 = src.addVM("vm2", null);
		nodes = new Node[] {
				n0, n1
		};
		vms = new VM[] {
				vm0, vm1, vm2
		};
		pb = new ReconfigurationProblem(src);
	}

	/**
	 * check the construction of the variables
	 */
	@Test
	public void checkWaitingNoExternVMVariables() {
		for (VM vm : vms) {
			Assert.assertTrue(
					pb.getState(vm).contains(CoreView.VM_RUNNODE) && pb.getState(vm).contains(CoreView.VM_WAITING)
					&& !pb.getState(vm).contains(CoreView.VM_RUNEXT),
					"" + vm + " state is not running|waiting : " + pb.getState(vm));
		}
	}

	/**
	 * three propagation cases : we set the host to a value, we set the host to no
	 * host, we set the state to waiting
	 * <ol>
	 * <li>host = 0 so state = running</li>
	 * <li>host =-1 so state = waiting</li>
	 * <li>state = waiting so host =-1</li>
	 * </ol>
	 */
	@Test
	public void testStatePropagation() {
		try {
			pb.getVMLocation(vm0).instantiateTo(pb.b().waitIdx(), Cause.Null);
			pb.getVMLocation(vm1).instantiateTo(pb.b().location(n0), Cause.Null);
			pb.getState(vm2).instantiateTo(CoreView.VM_WAITING, Cause.Null);
			pb.getSolver().propagate();
			Assert.assertTrue(pb.getState(vm0).isInstantiatedTo(CoreView.VM_WAITING),
					"" + pb.getState(vm0) + " should be " + CoreView.VM_WAITING);
			Assert.assertTrue(pb.getState(vm1).isInstantiatedTo(CoreView.VM_RUNNODE),
					"" + pb.getState(vm1) + " should be " + CoreView.VM_RUNNODE);
			Assert.assertTrue(pb.getVMLocation(vm2).isInstantiatedTo(pb.b().waitIdx()),
					"" + pb.getVMLocation(vm2) + " should be 2");
		}
		catch (ContradictionException e) {
			e.printStackTrace(System.err);
			Assert.fail("whut ?", e);
		}
	}

}

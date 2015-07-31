
package fr.emn.optiplace.solver.choco;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.*;
import fr.emn.optiplace.view.access.CoreView;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class ReconfigurationProblemWaitingExternTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	    .getLogger(ReconfigurationProblemWaitingExternTest.class);

	Configuration src;
	Node n0, n1;
	Node[] nodes;
	VM vm0, vm1, vm2, vm3, vm4, vm5;
	VM[] vms;
	Extern e0, e1;
	Extern[] externs;
	ReconfigurationProblem pb;

	@BeforeMethod
	public void prepare() {
		src = new SimpleConfiguration();
		n0 = src.addOnline("n0");
		n1 = src.addOnline("n1");
		nodes = new Node[] {
		    n0, n1
		};
		vm0 = src.addVM("vm0", null);
		vm1 = src.addVM("vm1", null);
		vm2 = src.addVM("vm2", null);
		vm3 = src.addVM("vm3", null);
		vm4 = src.addVM("vm4", null);
		vm5 = src.addVM("vm5", n0);
		vms = new VM[] {
		    vm0, vm1, vm2, vm3, vm4, vm5
		};
		e0 = src.addExtern("e0");
		e1 = src.addExtern("e1");
		externs = new Extern[] {
		    e0, e1
		};
		pb = new ReconfigurationProblem(src);
	}

	/**
	 * <ol>
	 * <li>extern is set to 0 : state is externed, host=-1</li>
	 * <li>extern is set to -1 : state is not externed</li>
	 * <li>state is externed : host=-1</li>
	 * <li>host is set to 0 : extern is -1</li>
	 * <li>host is set to -1 :state is waiting|externed</li>
	 * <li>host is set to -1 and VM was on host :state is externed</li>
	 * </ol>
	 */
	@Test
	public void testPropagate() throws ContradictionException {
		pb.getExtern(vm0).instantiateTo(0, Cause.Null);
		// pb.getExtern(vm1).instantiateTo(-1, Cause.Null);
		// pb.getState(vm2).instantiateTo(CoreView.VM_EXTERNED, Cause.Null);
		// pb.getHost(vm3).instantiateTo(0, Cause.Null);
		// pb.getHost(vm4).instantiateTo(-1, Cause.Null);
		// pb.getHost(vm5).instantiateTo(-1, Cause.Null);
		System.err.println("propagate");
		pb.propagate();
		System.err.println("propagate done");
		Assert.assertTrue(pb.getState(vm0).isInstantiatedTo(CoreView.VM_EXTERNED) && pb.getHost(vm0).isInstantiatedTo(-1),
		    "" + pb.getState(vm0) + " ; " + pb.getHost(vm0));
	}
}

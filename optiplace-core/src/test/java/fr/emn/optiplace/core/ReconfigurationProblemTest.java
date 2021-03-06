/**
 *
 */

package fr.emn.optiplace.core;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.view.access.CoreView;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class ReconfigurationProblemTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ReconfigurationProblemTest.class);

	IConfiguration src;
	Computer n0, n1;
	Computer[] nodes;
	VM vm0_0, vm0_1, vm1_0, vm1_1;
	VM[] vms;
	ReconfigurationProblem pb;

	@BeforeMethod
	public void prepare() {
		src = new Configuration();
		n0 = src.addComputer("n0");
		n1 = src.addComputer("n1");
		vm0_0 = src.addVM("vm0_0", n0);
		vm0_1 = src.addVM("vm0_1", n0);
		vm1_0 = src.addVM("vm1_0", n1);
		vm1_1 = src.addVM("vm1_1", n1);
		nodes = new Computer[] {
				n0, n1
		};
		vms = new VM[] {
				vm0_0, vm0_1, vm1_0, vm1_1
		};
		pb = new ReconfigurationProblem(src);
	}

	@Test
	public void checkHosters() throws ContradictionException {
		pb.getVMLocation(vm1_0).removeValue(pb.b().location(n0), Cause.Null);
		pb.getVMLocation(vm1_1).removeValue(pb.b().location(n0), Cause.Null);
		Assert.assertNotNull(pb.getSolver().findSolution());
	}

	@Test(dependsOnMethods = "checkHosters")
	public void checkHosted() throws ContradictionException {
		pb.getHostedOn(n0).remove(pb.b().vm(vm1_0), Cause.Null);
		pb.getHostedOn(n0).remove(pb.b().vm(vm1_1), Cause.Null);
		Assert.assertNotNull(pb.getSolver().findSolution());
	}

	@Test(dependsOnMethods = "checkHosted")
	public void checknbVM() throws ContradictionException {
		pb.nbVMsOn(n0).updateUpperBound(2, Cause.Null);
		Assert.assertNotNull(pb.getSolver().findSolution());
	}

	@Test
	public void checkNoWaitingNoExternVMVariables() {
		for (VM vm : vms) {
			Assert.assertTrue(pb.getState(vm).isInstantiatedTo(CoreView.VM_RUNNODE),
					"" + vm + " state is not running : " + pb.getState(vm));
		}
	}

	@Test
	public void testNbVMs() throws ContradictionException {
		IConfiguration src = new Configuration("mem");
		Computer n = src.addComputer("n", 2);
		src.addVM("vm", n, 3);
		src.addExtern("e", 3);
		ReconfigurationProblem rp = new ReconfigurationProblem(src);
		rp.getHostedOn(n).instantiateTo(new int[] {}, Cause.Null);
		Assert.assertNotNull(rp.getSolver().findSolution());
	}

	@Test(dependsOnMethods = "testNbVMs")
	public void testIsHoster() throws ContradictionException {
		IConfiguration src = new Configuration("mem");
		Computer n = src.addComputer("n", 2);
		src.addVM("vm", n, 3);
		src.addExtern("e", 3);
		ReconfigurationProblem rp = new ReconfigurationProblem(src);
		rp.isHost(n).setToFalse(Cause.Null);
		Assert.assertNotNull(rp.getSolver().findSolution());
	}

	@Test
	public void testBugZeroResource() {
		Configuration c = new Configuration("mem");
		c.addComputer("n", 4);
		VM v = c.addVM("v", null, 2);

		c.resource("core").with(c.addExtern("e"), 1).with(v, 1);

		Optiplace test = new Optiplace(c);
		IConfiguration t = test.solve().getDestination();
		Assert.assertNotNull(t);
		Assert.assertTrue(t.isWaiting(v));
	}

	@Test
	public void testVMStateVariables() throws ContradictionException {
		Configuration c = new Configuration();
		Computer n = c.addComputer("n");
		Extern e = c.addExtern("e");
		VM vn0 = c.addVM("vn0", n);
		VM vn1 = c.addVM("vn1", n);
		VM ve0 = c.addVM("ve0", e);
		VM ve1 = c.addVM("ve1", e);
		VM vw0 = c.addVM("vw0", null);
		VM vw1 = c.addVM("vw1", null);
		VM vw2 = c.addVM("vw2", null);

		ReconfigurationProblem rp = new ReconfigurationProblem(c);
		rp.isRunComputer(vn0).setToTrue(Cause.Null);
		rp.isRunExt(vn1).setToTrue(Cause.Null);
		rp.isRunExt(ve0).setToTrue(Cause.Null);
		rp.isRunComputer(ve1).setToTrue(Cause.Null);
		rp.isWaiting(vw0).setToTrue(Cause.Null);
		rp.isRunComputer(vw1).setToTrue(Cause.Null);
		rp.isRunExt(vw2).setToTrue(Cause.Null);

		rp.getSolver().findSolution();
		IConfiguration dest = rp.extractConfiguration();

		Assert.assertEquals(dest.getLocation(vn0), n);
		Assert.assertEquals(dest.getLocation(vn1), e);
		Assert.assertEquals(dest.getLocation(ve0), e);
		Assert.assertEquals(dest.getLocation(ve1), n);
		Assert.assertEquals(dest.getLocation(vw0), null);
		Assert.assertEquals(dest.getLocation(vw1), n);
		Assert.assertEquals(dest.getLocation(vw2), e);
	}
}

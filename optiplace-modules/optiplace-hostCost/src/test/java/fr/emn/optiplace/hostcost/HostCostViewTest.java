package fr.emn.optiplace.hostcost;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;

public class HostCostViewTest {

	@Test
	public void testOnNode() {
		IConfiguration c = new Configuration("mem");
		Node n0 = c.addOnline("n0", 12);
		Extern e0 = c.addExtern("e0", 12);
		VM vm0 = c.addVM("vm0", n0, 4);
		VM vm1 = c.addVM("vm1", n0, 6);
		VM vm2 = c.addVM("vm2", n0, 4);
		VM vm3 = c.addVM("vm3", n0, 6);
		VM vm4 = c.addVM("vm4", n0, 4);

		HostCostView v = new HostCostView();
		v.getCostData().setHostCost(e0.getName(), 1);
		Optiplace o = new Optiplace(c);
		o.addView(v);
		o.getStrat().setGoalId("hostcost");

		o.solve();
		IConfiguration ret = o.getTarget().getDestination();
		Assert.assertEquals(ret.getLocation(vm0), n0);
		Assert.assertEquals(ret.getLocation(vm1), e0);
		Assert.assertEquals(ret.getLocation(vm2), n0);
		Assert.assertEquals(ret.getLocation(vm3), e0);
		Assert.assertEquals(ret.getLocation(vm4), n0);
	}

	@Test
	public void testWaitingCost() {
		Configuration c = new Configuration("mem");
		Node n0 = c.addOnline("n0", 2);
		Node n1 = c.addOnline("n1", 2);
		VM vm0 = c.addVM("vm0", n0, 1);
		VM vm1 = c.addVM("vm1", n1, 1);
		VM vm2 = c.addVM("vm2", null, 2);
		IConfiguration d1 = new Optiplace(c).solve().getDestination();
		Assert.assertEquals(d1.getLocation(vm0), d1.getLocation(vm1), "found cfg : " + d1);

		IOptiplace o = new Optiplace(c).with(new HostCostView()).withGoal("hostcost");
		IConfiguration d2 = o.solve().getDestination();
		Assert.assertEquals(d2.getLocation(vm0), d2.getLocation(vm1), "found cfg : " + d2);
		Assert.assertNotNull(d2.getLocation(vm2), "found cfg : " + d2);
	}

}

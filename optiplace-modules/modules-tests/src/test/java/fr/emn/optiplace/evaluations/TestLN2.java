package fr.emn.optiplace.evaluations;

import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Far;
import fr.emn.optiplace.hostcost.HostCostView;

public class TestLN2 {

	@Test
	public void testLN() {
		int locationMem = 2000;
		int vmmem = 1000;
		Configuration src = new Configuration("mem");

		Extern[] externs = new Extern[4];
		for (int i = 0; i < externs.length; i++) {
			externs[i] = src.addExtern("e" + i, locationMem);
			src.addSite("site" + i, externs[i]);
		}

		// 3 VMs
		VM[] vms = new VM[3];
		for (int i = 0; i < vms.length; i++) {
			vms[i] = src.addVM("v_" + i, null, vmmem);
		}

		HAView ha = new HAView();
		ha.addRule(new Far(vms[0], vms[1], vms[2]));

		HostCostView hc = new HostCostView();
		hc.getCostData().setDefaultCost(1);
		hc.getCostData().setSiteCost(src.addSite("site2"), 100);

		Optiplace opl = new Optiplace(src);
		opl.with(ha).with(hc).withGoal("hostcost");
		// opl.getStrat().setLogSolutions(true);
		DeducedTarget res = opl.solve();
		Assert.assertEquals(res.getDestination().getWaitings().count(), 0L,
				"source:\n" + opl.source() + "\nrules: " + opl.views().get(0).rulesStream().collect(Collectors.toList())
				+ "\n\ndest:" + res.getDestination() + "\n");
		Assert.assertEquals(res.getDestination().getHosted(externs[2]).count(), 0l,
				"source:\n" + opl.source() + "\nrules: " + opl.views().get(0).rulesStream().collect(Collectors.toList())
				+ "\n\ndest:" + res.getDestination() + "\n");
	}

}
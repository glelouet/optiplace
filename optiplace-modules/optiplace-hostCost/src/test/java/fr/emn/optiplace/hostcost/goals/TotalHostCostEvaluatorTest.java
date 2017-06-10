package fr.emn.optiplace.hostcost.goals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.hostcost.HostCostView;
import fr.emn.optiplace.hostcost.heuristics.PreventExpensiveHosts;

public class TotalHostCostEvaluatorTest {

	@Test
	public void testOrdering() {

		// 5 externs. cost is decreasing : e3=e4=e5> e1 > e2 > e0=0
		// since e3, e4, e5 have same cost we add vms too : 2 on e4, 1 on e5.

		Configuration cfg = new Configuration();
		Extern e0 = cfg.addExtern("e0");
		Extern e1 = cfg.addExtern("e1");
		Extern e2 = cfg.addExtern("e2");
		Extern e3 = cfg.addExtern("e3");
		Extern e4 = cfg.addExtern("e4");
		Extern e5 = cfg.addExtern("e5");

		cfg.addVM("v0", e4);
		cfg.addVM("v1", e4);
		cfg.addVM("v2", e5);
		ReconfigurationProblem rp = new ReconfigurationProblem(cfg);

		HostCostView v = new HostCostView();
		v.getCostData().setHostCost(e0, 0);
		v.getCostData().setHostCost(e1, 2);
		v.getCostData().setHostCost(e2, 1);
		v.getCostData().setHostCost(e3, 3);
		v.getCostData().setHostCost(e4, 3);
		v.getCostData().setHostCost(e5, 3);

		v.associate(rp);
		List<VMLocation> l = PreventExpensiveHosts.orderLocationsByCost(v).collect(Collectors.toList());
		Assert.assertEquals(l, Arrays.asList(e3, e5, e4, e1, e2));
	}

	public static void main(String[] args) {
		Configuration cfg = new Configuration();
		Extern e0 = cfg.addExtern("e0");
		Extern e1 = cfg.addExtern("e1");
		Extern e2 = cfg.addExtern("e2");
		Extern e3 = cfg.addExtern("e3");
		Extern e4 = cfg.addExtern("e4");
		Extern e5 = cfg.addExtern("e5");

		cfg.addVM("v0", e4);
		cfg.addVM("v1", e4);
		cfg.addVM("v2", e5);

		HostCostView v = new HostCostView();
		v.getCostData().setHostCost(e0, 0);
		v.getCostData().setHostCost(e1, 2);
		v.getCostData().setHostCost(e2, 1);
		v.getCostData().setHostCost(e3, 3);
		v.getCostData().setHostCost(e4, 3);
		v.getCostData().setHostCost(e5, 3);

		Optiplace op = new Optiplace(cfg);
		op.withGoal("hostcost");
		op.with(v);
		DeducedTarget dt = op.solve();
		System.err.println("" + dt.getSearchTime() + " " + dt.getSearchNodes());

	}

}

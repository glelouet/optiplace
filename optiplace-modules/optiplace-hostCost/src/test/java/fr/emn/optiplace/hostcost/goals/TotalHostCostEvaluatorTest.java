package fr.emn.optiplace.hostcost.goals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.hostcost.HostCostView;

public class TotalHostCostEvaluatorTest {

	@Test
	public void testOrdering() {
		Configuration cfg = new Configuration();
		Extern e0 = cfg.addExtern("e0");
		Extern e1 = cfg.addExtern("e1");
		Extern e2 = cfg.addExtern("e2");
		Extern e3 = cfg.addExtern("e3");
		ReconfigurationProblem rp = new ReconfigurationProblem(cfg);

		HostCostView v = new HostCostView();
		v.getCostData().setHostCost(e0, 0);
		v.getCostData().setHostCost(e1, 2);
		v.getCostData().setHostCost(e2, 1);
		v.getCostData().setHostCost(e3, 3);

		v.associate(rp);
		;
		List<VMLocation> l = new TotalHostCostEvaluator(v).orderLocationsByCost().collect(Collectors.toList());
		Assert.assertEquals(l, Arrays.asList(e3, e1, e2));
	}

}

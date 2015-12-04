package fr.emn.optiplace.hostcost;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.hostcost.CostData;

public class CostDataTest {

	@Test
	public void testSelection() {
		CostData test = new CostData();

		test.setDefaultCost(3);
		Assert.assertEquals(test.getCost("", null), 3);

		test.setHostCost("node1", 10);
		test.addHostFilter("node.*", 15);
		test.setSiteCost("siteOfDeath", 8);
		test.addSiteFilter("site.*", 20);
		Assert.assertEquals(test.getCost("node1", null), 10);
		Assert.assertEquals(test.getCost("node2", null), 15);
		Assert.assertEquals(test.getCost("n", "siteOfDeath"), 8);
		Assert.assertEquals(test.getCost("nodeJS", "siteOfDeath"), 15);
		Assert.assertEquals(test.getCost(null, "sitefficiency"), 20);
	}

	@Test
	public void testParsing() {
		CostData test = new CostData();
		test.readLine("host(node1)=4");
		test.readLine("hostLike(n.*)=5");
		test.readLine("site(s1)=6");
		test.readLine("siteLike(s.*)=7");
		Assert.assertEquals(test.getCost("node1", null), 4);
		Assert.assertEquals(test.getCost("n", null), 5);
		Assert.assertEquals(test.getCost("a", "s1"), 6);
		Assert.assertEquals(test.getCost("a", "s2"), 7);
	}

}

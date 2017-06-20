package fr.emn.optiplace.ha.rules;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.test.SolvingExample;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class SiteOffTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SiteOffTest.class);

	/** a VM must be moved to a specific node, on a different site */
	@Test
	public void testSiteOff() {
		nbComputers = 2;
		nbVMPerComputer = 1;
		nbWaitings = 0;
		prepare();
		Computer node = nodes[0];
		Site site = src.addSite("mysite", node);
		VM vm = placed[0][0];
		SiteOff test = new SiteOff(site, vm);
		// strat.setLogStats(true);
		// strat.setLogChoices(true);
		// strat.setLogHeuristicsSelection(true);
		IConfiguration d = solve(src, test).getDestination();
		Assert.assertTrue(test.isSatisfied(d), "rule is not satisfied : VM " + vm + " is on " + d.getLocation(vm)
				+ " which is site " + d.getSite(d.getComputerHost(vm)));
		Assert.assertNotEquals(d.getLocation(vm), null, "VM " + vm + " placed on " + d.getLocation(vm));
		Assert.assertNotEquals(d.getSite(d.getLocation(vm)), site);
	}
}

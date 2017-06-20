package fr.emn.optiplace.ha.rules;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.test.SolvingExample;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class SiteOnTest extends SolvingExample {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SiteOnTest.class);

	/** a VM must be moved to a specific node, on a different site */
	@Test
	public void testSiteOn() {
		nbWaitings = 0;
		prepare();

		Computer node = nodes[1];
		Site site = src.addSite("mysite", node);
		VM vm = placed[0][0];
		SiteOn test = new SiteOn(site, vm);

		IConfiguration d = solve(src, test).getDestination();
		Assert.assertEquals(d.getLocation(vm), node, "config solved : " + d);
		Assert.assertEquals(d.getSite(d.getLocation(vm)), site);
	}
}

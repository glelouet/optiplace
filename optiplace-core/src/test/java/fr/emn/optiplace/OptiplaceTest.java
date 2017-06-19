/**
 *
 */

package fr.emn.optiplace;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
@SuppressWarnings("unused")
public class OptiplaceTest {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OptiplaceTest.class);

	/**
	 * test with nothing
	 */
	@Test
	public void testEmpty() {
		Configuration c = new Configuration();
		Optiplace sp = new Optiplace(c);
		sp.solve();
		Assert.assertEquals(sp.getTarget().getDestination(), c);
	}

	/**
	 * test 1 vm already on the 1 node existing
	 */
	@Test
	public void testNoResource() {
		Configuration c = new Configuration();
		Node n1 = c.addNode("n1");
		c.addVM("v1", n1);
		Optiplace sp = new Optiplace(c);
		sp.getStrat().setLogContradictions(true);
		sp.solve();
		Assert.assertEquals(sp.getTarget().getDestination(), c);
	}

	/**
	 * test 1 vm already on the 1 node existing
	 */
	@Test
	public void testNoResourceSeveralElement() {
		Configuration c = new Configuration();
		Node n1 = c.addNode("n1");
		Node n2 = c.addNode("n2");
		c.addVM("v1", n1);
		c.addVM("v2", n2);
		c.addVM("v3", n2);
		Optiplace sp = new Optiplace(c);
		sp.withGoal(null);
		sp.getStrat().setLogChoices(true);
		sp.getStrat().setLogContradictions(true);
		sp.solve();
		Assert.assertEquals(sp.getTarget().getDestination(), c);
	}

	/**
	 * in this test, we have a correct source configuration : the solver should
	 * not make any action.
	 */
	@Test
	public void testExecutionNoObjective() {
		Configuration c = new Configuration("R");
		Node n1 = c.addNode("n1", 10);
		Node n2 = c.addNode("n2", 10);
		c.addVM("v1", n1, 1);
		c.addVM("v2", n2, 1);
		Optiplace sp = new Optiplace(c);
		sp.getStrat().setLogContradictions(true);
		sp.getStrat().setLogChoices(true);
		sp.solve();
		Assert.assertEquals(sp.getTarget().getDestination(), c);
	}

	@Test
	public void testBugSiteExternFull() {
		Configuration cfg = new Configuration();

		Extern a1 = cfg.addExtern("a1"), a1v2 = cfg.addExtern("a1.v2"), a2 = cfg.addExtern("a2");

		Site a1_site = cfg.addSite("a1_site", a1, a1v2);
		Site a2_site = cfg.addSite("a2_site", a2);

		VM n1 = cfg.addVM("n1", null), n2 = cfg.addVM("n2", null), n3 = cfg.addVM("n3", null);

		ResourceSpecification disk_size = cfg.resource("disk_size");
		ResourceSpecification mem_size = cfg.resource("mem_size");
		ResourceSpecification num_cpus = cfg.resource("num_cpus");
		disk_size.with(a1, 20000).with(a1v2, 10000).with(a2, 10000).with(n1, 20000).with(n2, 10000).with(n3, 10000);
		mem_size.with(a1, 10000).with(a1v2, 10000).with(a2, 20000).with(n1, 10000).with(n2, 10000).with(n3, 20000);
		num_cpus.with(a1, 1).with(a1v2, 2).with(a2, 1).with(n1, 1).with(n2, 2).with(n3, 1);

		Optiplace opl = new Optiplace(cfg);
		IConfiguration dest = opl.solve().getDestination();
		Assert.assertEquals(dest.getLocation(n1), a1);
		Assert.assertEquals(dest.getLocation(n2), a1v2);
		Assert.assertEquals(dest.getLocation(n3), a2);
	}

	@Test
	public void testBugSiteExtern() {
		Configuration cfg = new Configuration();

		Extern e1 = cfg.addExtern("e1"), e2 = cfg.addExtern("e2");

		Site e1_site = cfg.addSite("e1Site", e1);
		VM v1 = cfg.addVM("v1", null);
		VM v2 = cfg.addVM("v2", null);


		ResourceSpecification mem_size = cfg.resource("mem_size");
		mem_size.with(e1, 10000).with(e2, 20000).with(v1, 10000).with(v2, 20000);

		Optiplace opl = new Optiplace(cfg);
		IConfiguration dest = opl.solve().getDestination();
		Assert.assertEquals(dest.getLocation(v2), e2);
	}
}

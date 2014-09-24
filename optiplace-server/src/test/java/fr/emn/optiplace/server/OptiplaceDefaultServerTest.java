/**
 *
 */
package fr.emn.optiplace.server;

import org.testng.Assert;
import org.testng.annotations.Test;

import choco.kernel.common.logging.Verbosity;
import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.solver.ConfigStrat;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class OptiplaceDefaultServerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(OptiplaceDefaultServerTest.class);

	@Test
	public void testSimpleSolve() {
		OptiplaceDefaultServer test = new OptiplaceDefaultServer();
		ConfigStrat strat = test.addStrat();
		strat.setChocoVerbosity(Verbosity.FINEST);
		SimpleConfiguration cfg = new SimpleConfiguration("CPU");
		Node n1 = cfg.addOnline("n1", 1);
		Node n2 = cfg.addOnline("n2", 10);
		cfg.addVM("vm1", n1, 5);
		System.err.println("" + cfg);
		DeducedTarget res = test.solve(cfg);
		Assert.assertEquals(res.getSearchSolutions(), 1);
		Assert.assertEquals(res.getDestination().nbHosted(n2), 1);
		Assert.assertEquals(res.getDestination().nbHosted(n1), 0);
	}
}

package fr.emn.optiplace.solver;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration.VMSTATES;
import fr.emn.optiplace.configuration.Configuration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class SolverTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SolverTest.class);

	/**
	 * test if a VM set to waiting is put to execution on a node
	 */
	@Test
	public void testSolver() {
		Configuration c = new Configuration();
		c.addExtern("exter");
		c.addOnline("node");
		c.addVM("vm", null);
		Optiplace test = new Optiplace();
		test.source(c);
		test.solve();
		Assert.assertEquals(test.getTarget().getDestination().nbVMs(VMSTATES.WAITING), 0);
	}
}

/**
 *
 */

package fr.emn.optiplace;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SolvingProcessTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SolvingProcessTest.class);

	/**
	 * in this test, we have a correct source configuration : the solver should
	 * not make any action.
	 */
	@Test
	public void testExecutionNoObjective() {
		SimpleConfiguration c = new SimpleConfiguration("R");
		Node n1 = c.addOnline("n1", 10);
		Node n2 = c.addOnline("n2", 10);
		c.addVM("v1", n1, 1);
		c.addVM("v2", n2, 1);
		SolvingProcess sp = new SolvingProcess();
		sp.getStrat().setMoveMigratingVMs(true);
		sp.source(c);
		// sp.getStrat().setLogChoices(true);
		sp.solve();
		Assert.assertEquals(sp.getTarget().getDestination(), c);
	}
}

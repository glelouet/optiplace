/**
 *
 */

package fr.emn.optiplace;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class OptiplaceTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(OptiplaceTest.class);

	/**
	 * in this test, we have a correct source configuration : the solver should
	 * not make any action.
	 */
	@Test
	public void testExecutionNoObjective() {
		Configuration c = new Configuration("R");
		Node n1 = c.addOnline("n1", 10);
		Node n2 = c.addOnline("n2", 10);
		c.addVM("v1", n1, 1);
		c.addVM("v2", n2, 1);
		Optiplace sp = new Optiplace();
		sp.source(c);
		// sp.getStrat().setLogChoices(true);
		sp.solve();
		Assert.assertEquals(sp.getTarget().getDestination(), c);
	}
}

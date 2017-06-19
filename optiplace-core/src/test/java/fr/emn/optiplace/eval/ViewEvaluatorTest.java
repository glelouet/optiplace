package fr.emn.optiplace.eval;

import org.testng.Assert;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.eval.ViewEvaluator.ProblemEvaluatorIfWorse;

public class ViewEvaluatorTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ViewEvaluatorTest.class);

	public void testSameSearchTime() {
		Configuration c = new Configuration();
		int allowedpercent = 5;
		int nbTries = 20;
		int redo = 10000;
		c.addNode("n0");
		c.addNode("n1");
		c.addVM("vm0", null);
		c.addVM("vm1", null);
		c.addVM("vm2", null);
		c.addVM("vm3", null);
		Optiplace opl = new Optiplace(c);
		ProblemEvaluatorIfWorse test = ViewEvaluator.makeTimeEvaluator(redo);
		float maxTime = 0, minTime = Float.POSITIVE_INFINITY;
		for (int i = 0; i < nbTries; i++) {
			long time = test.evalBestIfWorse(opl, 0);
			maxTime = Math.max(maxTime, time);
			minTime = Math.min(minTime, time);
		}
		Assert.assertTrue(maxTime / minTime < 1 + 0.01 * allowedpercent,
				"two different searches give two different times : " + maxTime + " >>> " + minTime + ", factor is "
						+ maxTime / minTime);
	}
}

package fr.emn.optiplace.solver;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.VM;

public class BugNoExternResourceTest {

	public BugNoExternResourceTest() {
	}

	@SuppressWarnings("unused")
	@Test
	public void testNoNodeSolution() {
		SimpleConfiguration sc = new SimpleConfiguration("mem");
		Node n = sc.addOnline("node", 5);
		VM v = sc.addVM("v", null, 50);
		Extern e = sc.addExtern("e");

		SolvingProcess sp = new SolvingProcess();
		sp.source(sc);
		sp.solve();

		Configuration dest = sp.getTarget().getDestination();
		Assert.assertNotNull(dest);
	}

}

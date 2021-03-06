package fr.emn.optiplace.solver;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;

public class BugNoExternResourceTest {

	public BugNoExternResourceTest() {
	}

	@SuppressWarnings("unused")
	@Test
	public void testNoComputerSolution() {
		Configuration sc = new Configuration("mem");
		Computer n = sc.addComputer("node", 5);
		VM v = sc.addVM("v", null, 50);
		Extern e = sc.addExtern("e");

		Optiplace sp = new Optiplace();
		sp.source(sc);
		sp.solve();

		IConfiguration dest = sp.getTarget().getDestination();
		Assert.assertNotNull(dest);
	}

}

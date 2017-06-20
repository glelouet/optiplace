package fr.emn.optiplace.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;

public class ReconfigurationPoblemTestExternResources {

	@Test
	public void test() {
		Configuration cfg = new Configuration("mem");
		Computer n = cfg.addComputer("n", 10);
		Extern e = cfg.addExtern("e", 10);
		VM running = cfg.addVM("vm1", n, 5);
		VM waiting = cfg.addVM("w", null, 7);

		Optiplace sp = new Optiplace(cfg);
		IConfiguration out = sp.solve().getDestination();
		Assert.assertNotNull(out);
		Assert.assertEquals(out.getFutureLocation(running), n);
		Assert.assertEquals(out.getFutureLocation(waiting), e);

		cfg.resource("mem").capacity(e, 5);
		cfg.setHost(waiting, e);
		sp = new Optiplace(cfg);
		out = sp.solve().getDestination();
		Assert.assertNotNull(out);
		Assert.assertEquals(out.getFutureLocation(waiting), n);
		Assert.assertEquals(out.getFutureLocation(running), e);
	}

}

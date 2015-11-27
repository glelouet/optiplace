package fr.emn.optiplace.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.*;

public class ReconfigurationPoblemTestExternResources {

	@Test
	public void test() {
		Configuration cfg = new Configuration("mem");
		Node n = cfg.addOnline("n", 10);
		Extern e = cfg.addExtern("e", 10);
		VM running = cfg.addVM("vm1", n, 5);
		VM waiting = cfg.addVM("w", null, 7);

		Optiplace sp = new Optiplace();
		sp.source(cfg);
		sp.solve();
		IConfiguration out = sp.getTarget().getDestination();
		Assert.assertEquals(out.getFutureLocation(running), n);
		Assert.assertEquals(out.getFutureLocation(waiting), e);

		cfg.resource("mem").capacity(e, 5);
		cfg.setHost(waiting, e);
		sp = new Optiplace();
		sp.source(cfg);
		sp.solve();
		out = sp.getTarget().getDestination();
		Assert.assertEquals(out.getFutureLocation(waiting), n);
		Assert.assertEquals(out.getFutureLocation(running), e);
	}

}

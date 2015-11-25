package fr.emn.optiplace.core;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.SolvingProcess;
import fr.emn.optiplace.configuration.*;

public class ReconfigurationPoblemTestExternResources {

	@Test
	public void test() {
		SimpleConfiguration cfg = new SimpleConfiguration("mem");
		Node n = cfg.addOnline("n", 10);
		Extern e = cfg.addExtern("e", 10);
		VM running = cfg.addVM("vm1", n, 5);
		VM waiting = cfg.addVM("w", null, 7);

		SolvingProcess sp = new SolvingProcess();
		sp.source(cfg);
		sp.solve();
		Configuration out = sp.getTarget().getDestination();
		Assert.assertEquals(out.getFutureLocation(running), n);
		Assert.assertEquals(out.getFutureLocation(waiting), e);

		cfg.resource("mem").capacity(e, 5);
		cfg.setHost(waiting, e);
		sp = new SolvingProcess();
		sp.source(cfg);
		sp.solve();
		out = sp.getTarget().getDestination();
		Assert.assertEquals(out.getFutureLocation(waiting), n);
		Assert.assertEquals(out.getFutureLocation(running), e);
	}

}

package fr.emn.optiplace.evaluations;

import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Near;
import fr.emn.optiplace.ha.rules.Spread;

public class TestLN {

	@Test
	public void testLN() {
		int extMem = 2000;
		int ftpMem = 1000;
		Configuration src = new Configuration("mem");

		Extern[] externs = new Extern[2];
		externs[0] = src.addExtern("e" + 0, extMem);
		externs[1] = src.addExtern("e" + 1, extMem);
		src.addSite("site0", externs[0], externs[1]);
		// src.tagSite(site0, Near.SUPPORT_TAG);
		src.tagExtern(externs[0], Near.SUPPORT_TAG);
		src.tagExtern(externs[1], Near.SUPPORT_TAG);

		// nine VMs
		VM[] vms = new VM[2];
		vms[0] = src.addVM("ftp_" + 0, null, ftpMem);
		vms[1] = src.addVM("ftp_" + 1, null, ftpMem);

		HAView ha = new HAView();
		ha.addRule(new Spread(vms[0], vms[1]));
		ha.addRule(new Near(vms[0], vms[1]));


		Optiplace opl = new Optiplace(src);
		opl.with(ha);
		DeducedTarget res = opl.solve();
		Assert.assertEquals(res.getDestination().getWaitings().count(), 0L,
				"source:\n" + opl.source() + "\nrules: " + opl.views().get(0).rulesStream().collect(Collectors.toList())
				+ "\n\ndest:" + res.getDestination() + "\n");
	}

}
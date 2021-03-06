package fr.emn.optiplace.solver.choco;

import java.util.stream.Stream;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;

public class BridgeTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BridgeTest.class);

	@SuppressWarnings("unchecked")
	@Test
	public void testDefaultvalue() {
		IConfiguration cfg = Mockito.mock(IConfiguration.class);
		Mockito.when(cfg.getVMs()).thenReturn(Stream.empty());
		Mockito.when(cfg.getComputers()).thenReturn(Stream.empty(), Stream.empty());
		Mockito.when(cfg.getExterns()).thenReturn(Stream.empty(), Stream.empty());
		Mockito.when(cfg.getSites()).thenReturn(Stream.empty());
		Bridge test = new Bridge(cfg);
		Assert.assertEquals(test.location(new Computer("n0")), -1);
		Assert.assertEquals(test.vm(new VM("v0")), -1);
		Assert.assertEquals(test.site(new Site("s0")), 0);
		Assert.assertEquals(test.location(new Extern("e0")), -1);
		Assert.assertEquals(test.vm(0), null);
		Assert.assertEquals(test.location(0), null);
		Assert.assertEquals(test.site(0), null);
		Assert.assertEquals(test.location(0), null);
		Assert.assertEquals(test.vm(-1), null);
		Assert.assertEquals(test.location(-1), null);
		Assert.assertEquals(test.site(-1), null);
		Assert.assertEquals(test.location(-1), null);
	}

}

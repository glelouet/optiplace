
package fr.emn.optiplace.configuration.parser;

import java.io.File;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.resources.MappedResourceSpecification;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class ConfigurationFilerTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationFilerTest.class);

	@Test
	public void testParseEmptyConfig() {
		Configuration test = new Configuration();
		String[] lines = test.toString().split("\\n");
		ConfigurationFiler f = new ConfigurationFiler(null);
		for (String l : lines) {
			f.readLine(l);
		}
		Assert.assertEquals(f.getCfg(), test);

		test.resources().put("CPU", new MappedResourceSpecification("CPU"));
		lines = test.toString().split("\\n");
		f = new ConfigurationFiler(null);
		for (String l : lines) {
			f.readLine(l);
		}
		Assert.assertEquals(f.getCfg(), test);

		test.addNode("n0", 5);
		lines = test.toString().split("\\n");
		f = new ConfigurationFiler(null);
		for (String l : lines) {
			f.readLine(l);
		}
		Assert.assertEquals(f.getCfg(), test);
	}

	@Test
	public void testParsUnParse() {
		Configuration test = new Configuration("CPU", "MEM");
		Node n0 = test.addNode("n0", 10, 10);
		test.addVM("vm0", n0, 1, 1);
		test.addVM("vm1", null, 2, 3);
		String[] lines = test.toString().split("\\n");
		ConfigurationFiler f = new ConfigurationFiler(null);
		for (String l : lines) {
			f.readLine(l);
		}
		Assert.assertEquals(f.getCfg(), test);
	}

	@Test
	public void testParsingExterns() throws IOException {
		Configuration c1 = new Configuration("MEM");
		Extern e = c1.addExtern("e1", 20);
		c1.addVM("vm1", e, 5);

		ConfigurationFiler test = new ConfigurationFiler(File.createTempFile("nope", null));
		test.withConfiguration(c1).write();
		test.withConfiguration(new Configuration()).read();
		IConfiguration c2 = test.getCfg();

		Assert.assertEquals(c2, c1);
	}

}

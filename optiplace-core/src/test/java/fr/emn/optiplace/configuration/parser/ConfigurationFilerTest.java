package fr.emn.optiplace.configuration.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
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
	SimpleConfiguration test = new SimpleConfiguration();
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

	test.addOnline("n0", 5);
	lines = test.toString().split("\\n");
	f = new ConfigurationFiler(null);
	for (String l : lines) {
	    f.readLine(l);
	}
	Assert.assertEquals(f.getCfg(), test);
    }

    @Test
    public void testParsUnParse() {
	SimpleConfiguration test = new SimpleConfiguration("CPU", "MEM");
	Node n0 = test.addOnline("n0", 10, 10);
	test.addOffline("n1", 50, 20);
	test.addVM("vm0", n0, 1, 1);
	test.addVM("vm1", null, 2, 3);
	String[] lines = test.toString().split("\\n");
	ConfigurationFiler f = new ConfigurationFiler(null);
	for (String l : lines) {
	    f.readLine(l);
	}
	Assert.assertEquals(f.getCfg(), test);
    }

}

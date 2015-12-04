/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class BanTest extends SolvingExample {

  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(BanTest.class);

  @Test
  public void testSimpleInject() {
    prepare();
    HashSet<VM> vms = new HashSet<>();
    vms.addAll(src.getHosted(nodes[0]).collect(Collectors.toList()));
		Ban b = new Ban(vms, nodes[0].getName());
    IConfiguration d = solve(src, b).getDestination();
    for (VM v : vms) {
      Assert.assertNotEquals(nodes[0], d.getLocation(v));
    }
  }

  @Test
  public void testParsing() {
		HashSet<String> nodes = new HashSet<>(Arrays.asList("n1", "n2"));
    HashSet<VM> vms = new HashSet<>();
    vms.add(new VM("vm1"));
    vms.add(new VM("vm2"));
		Rule r = new Ban(vms, nodes);
		String s = r.toString();
    Ban parsed = Ban.parse(s);
		Assert.assertEquals(parsed, r);
  }
}

/**
 *
 */
package fr.emn.optiplace.ha.rules;

import java.util.Collections;
import java.util.HashSet;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class CapacityTest extends SolvingExample {

  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(CapacityTest.class);

  @Test
  public void testSimpleInject() {
    prepare();
    Capacity c = new Capacity(Collections.singleton(nodes[0]), 2);
    IConfiguration d = solve(src, c).getDestination();
    Assert.assertEquals(d.nbHosted(nodes[0]), 2);
  }

  @Test(dependsOnMethods = "testSimpleInject")
  public void testWithMove() {
    nbWaitings = 0;
    prepare();
    Capacity c = new Capacity(Collections.singleton(nodes[0]), 0);
    IConfiguration d = solve(src, c).getDestination();
    Assert.assertEquals(d.nbHosted(nodes[0]), 0);
    Assert.assertEquals(d.nbHosted(nodes[1]), 4);
  }

  @Test
  public void testParsing() {
    HashSet<Node> nodes = new HashSet<>();
    nodes.add(new Node("n1"));
    nodes.add(new Node("n2"));
		Rule r = new Capacity(nodes, 5);
		String s = r.toString();
    Capacity parsed = Capacity.parse(s);
		Assert.assertEquals(parsed, r);
  }
}

/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package entropy.vjob.builder.plasma;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.SimpleNode;

/** Unit tests for {@code SetsDifference}.
 * @author Fabien Hermenier */
@Test(groups = { "unit" })
public class TestSetsDifference {

  public void testExpand() {
    SimpleManagedElementSet<SimpleNode> ns1 = new SimpleManagedElementSet<SimpleNode>();
    SimpleManagedElementSet<SimpleNode> ns2 = new SimpleManagedElementSet<SimpleNode>();
    SimpleManagedElementSet<SimpleNode> res = new SimpleManagedElementSet<SimpleNode>();
    for (int i = 1; i <= 10; i++) {
      SimpleNode n = new SimpleNode("N" + i, 1, 1, 1);
      if (i <= 7) {
        ns1.add(n);
      }
      if (i >= 5) {
        ns2.add(n);
      }
      if (i < 5) {
        res.add(n);
      }
    }
    VJobSet d = new SetsDifference<SimpleNode>(
        new ExplodedSet<SimpleNode>(ns1), new ExplodedSet<SimpleNode>(ns2));
    Assert.assertEquals(d.flatten(), res);
  }

  public void testWithoutLabel() {
    VJobSet u = new SetsUnion<SimpleNode>(new ExplodedSet<SimpleNode>("$T2"),
        new RangeOfElements<SimpleNode>("VM[1..5]"));
    Assert.assertEquals(u.definition(), "$T2 + VM[1..5]");
    Assert.assertEquals(u.pretty(), "$T2 + VM[1..5]");
  }

  public void testWithLabel() {
    VJobSet u = new SetsUnion<SimpleNode>("$T1", new ExplodedSet<SimpleNode>(
        "$T2"), new RangeOfElements<SimpleNode>("VM[1..5]"));
    Assert.assertEquals(u.definition(), "$T2 + VM[1..5]");
    Assert.assertEquals(u.pretty(), "$T1");
  }

  public void testWithInsideLabels() {
    VJobSet u = new SetsUnion<SimpleNode>(new ExplodedSet<SimpleNode>("$T2"),
        new RangeOfElements<SimpleNode>("VM[1..5]", "$T3"));
    Assert.assertEquals(u.definition(), "$T2 + $T3");
  }
}

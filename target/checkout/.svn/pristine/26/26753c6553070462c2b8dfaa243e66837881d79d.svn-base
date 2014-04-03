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

import entropy.configuration.SimpleNode;

/** Unit tests for {@code MultiSetsDifference}
 * @author Fabien Hermenier */
@Test(groups = { "unit" })
public class TestMultiSetsDifference {

  /* public void testExpandAndAllElements() { ExplodedMultiSet<SimpleNode> ms1 =
   * new ExplodedMultiSet<SimpleNode>(); ExplodedMultiSet<SimpleNode> ms2 = new
   * ExplodedMultiSet<SimpleNode>(); List<ExplodedSet<SimpleNode>> ns = new
   * LinkedList<ExplodedSet<SimpleNode>>(); ExplodedSet<SimpleNode> all = new
   * ExplodedSet<SimpleNode>(); ExplodedSet<SimpleNode> s = new
   * ExplodedSet<SimpleNode>(); ExplodedSet<SimpleNode> e = new
   * ExplodedSet<SimpleNode>(); ExplodedMultiSet<SimpleNode> ms = ms1; for (int
   * i = 1; i <= 20; i++) { SimpleNode n = new SimpleNode("N" + i, 1, 1, 1);
   * all.add(n); s.add(n); e.add(n); if (i % 5 == 0) { ms.add(s); s = new
   * ExplodedSet<SimpleNode>(); ns.add(e); e = new ExplodedSet<SimpleNode>(); }
   * if (i == 10) { ms = ms2; } } MultiSetsDifference<SimpleNode> u = new
   * MultiSetsDifference<SimpleNode>(ms1, ms2); Assert.assertEquals(u.expand(),
   * ns); Assert.assertEquals(u.getElements(), all); } */

  public void testPrettyAndDefinition() {
    ExplodedMultiSet<SimpleNode> ms1 = new ExplodedMultiSet<SimpleNode>();
    ExplodedMultiSet<SimpleNode> ms2 = new ExplodedMultiSet<SimpleNode>();

    ms1.add(new RangeOfElements<SimpleNode>("N[1..5]"));
    ms1.add(new RangeOfElements<SimpleNode>("N[6..10]", "$T2"));
    ms2.add(new RangeOfElements<SimpleNode>("N[11..15]"));
    ms2.add(new RangeOfElements<SimpleNode>("N[16..20]", "$T4"));
    MultiSetsDifference<SimpleNode> u = new MultiSetsDifference<SimpleNode>(
        ms1, ms2);
    String def = "{N[1..5], $T2} - {N[11..15], $T4}";
    Assert.assertEquals(u.definition(), def);
    Assert.assertEquals(u.pretty(), def);

    u.setLabel("$small");
    Assert.assertEquals(u.definition(), def);
    Assert.assertEquals(u.pretty(), "$small");

  }
}

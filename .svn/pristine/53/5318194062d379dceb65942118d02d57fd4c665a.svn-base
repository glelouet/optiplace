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

import java.util.HashSet;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.SimpleNode;

/** Unit tests for {@code ExplodedMultiSet}
 * @author Fabien Hermenier */
@Test(groups = { "unit" })
public class TestExplodedMultiSet {

  public void testExpandAndAllElements() {
    ExplodedMultiSet<SimpleNode> ms = new ExplodedMultiSet<SimpleNode>();
    SimpleManagedElementSet<SimpleNode> all = new SimpleManagedElementSet<SimpleNode>();
    Set<ExplodedSet<SimpleNode>> expanded = new HashSet<ExplodedSet<SimpleNode>>();

    for (int i = 0; i < 3; i++) {
      ExplodedSet<SimpleNode> s = new ExplodedSet<SimpleNode>();
      ExplodedSet<SimpleNode> ns = new ExplodedSet<SimpleNode>();
      for (int j = 0; j < 2; j++) {
        SimpleNode n = new SimpleNode("N" + i + j, 1, 1, 1);
        s.add(n);
        all.add(n);
        ns.add(n);
      }
      ms.add(s);
      expanded.add(ns);
    }
    Assert.assertEquals(ms.getElements(), all);
    Assert.assertEquals(ms.expand(), expanded);

  }

  public void testPrettyAndDefinition() {
    ExplodedMultiSet<SimpleNode> ms = new ExplodedMultiSet<SimpleNode>();
    ms.add(new ExplodedSet<SimpleNode>("$T1"));
    ms.add(new RangeOfElements<SimpleNode>("N[2..5]"));
    ms.add(new RangeOfElements<SimpleNode>("N[20..50]", "$T3"));
    ms.add(new ExplodedSet<SimpleNode>());
    String def = "{$T1, N[2..5], $T3, {}}";
    Assert.assertEquals(ms.pretty(), def);
    Assert.assertEquals(ms.definition(), def);

    ms.setLabel("$small");
    Assert.assertEquals(ms.pretty(), "$small");
    Assert.assertEquals(ms.definition(), def);
  }
}

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

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.vjob.Ban;

/** Unit tests for Ban.
 * @author Fabien Hermenier */
@Test(groups = { "unit" })
public class TestBanBuilder {

  public void testGoodBuild() {
    ExplodedSet<SimpleVirtualMachine> vms = new ExplodedSet<SimpleVirtualMachine>();
    vms.add(new SimpleVirtualMachine("VM1", 1, 1, 1));
    ExplodedSet<SimpleNode> ns = new ExplodedSet<SimpleNode>();
    ns.add(new SimpleNode("N1", 1, 1, 1));
    BanBuilder b = new BanBuilder();

    List<VJobElement> params = new LinkedList<VJobElement>();
    params.add(vms);
    params.add(ns);
    try {
      Ban c = b.buildConstraint(params);
      Assert.assertEquals(c.getVirtualMachines(), vms);
      Assert.assertEquals(c.getNodes(), ns);
    }
    catch (ConstraintBuilderException e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test(expectedExceptions = { ConstraintBuilderException.class })
  public void testWithBadParamsNumber() throws ConstraintBuilderException {
    ExplodedSet<SimpleVirtualMachine> vms = new ExplodedSet<SimpleVirtualMachine>();
    vms.add(new SimpleVirtualMachine("VM1", 1, 1, 1));
    ExplodedSet<SimpleNode> ns = new ExplodedSet<SimpleNode>();
    ns.add(new SimpleNode("N1", 1, 1, 1));
    BanBuilder b = new BanBuilder();

    List<VJobElement> params = new LinkedList<VJobElement>();
    params.add(vms);
    b.buildConstraint(params);
  }

  @Test(expectedExceptions = { ConstraintBuilderException.class })
  public void testWithEmptyVMSet() throws ConstraintBuilderException {
    ExplodedSet<SimpleVirtualMachine> vms = new ExplodedSet<SimpleVirtualMachine>();
    ExplodedSet<SimpleNode> ns = new ExplodedSet<SimpleNode>();
    ns.add(new SimpleNode("N1", 1, 1, 1));
    BanBuilder b = new BanBuilder();

    List<VJobElement> params = new LinkedList<VJobElement>();
    params.add(vms);
    params.add(ns);
    b.buildConstraint(params);
  }

  @Test(expectedExceptions = { ConstraintBuilderException.class })
  public void testWithEmptyNodeset() throws ConstraintBuilderException {
    ExplodedSet<SimpleVirtualMachine> vms = new ExplodedSet<SimpleVirtualMachine>();
    ExplodedSet<SimpleNode> ns = new ExplodedSet<SimpleNode>();
    BanBuilder b = new BanBuilder();
    vms.add(new SimpleVirtualMachine("VM1", 1, 1, 1));
    List<VJobElement> params = new LinkedList<VJobElement>();
    params.add(vms);
    params.add(ns);
    b.buildConstraint(params);
  }
}

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

import entropy.configuration.ManagedElement;
import entropy.configuration.SimpleVirtualMachine;
import entropy.vjob.ContinuousSpread;

/** Unit tests for ContinuousSpreadBuilder.
 * @author Fabien Hermenier */
@Test(groups = { "unit" })
public class TestContinuousSpreadBuilder {

  /** Test cSpread({vm1,vm2,vm3}) */
  public void testValid() {
    ContinuousSpreadBuilder mb = new ContinuousSpreadBuilder();
    List<VJobElement> params = new LinkedList<VJobElement>();
    ExplodedSet<SimpleVirtualMachine> s1 = new ExplodedSet<SimpleVirtualMachine>();
    s1.add(new SimpleVirtualMachine("vm1", 1, 1, 1));
    s1.add(new SimpleVirtualMachine("vm2", 1, 1, 1));
    s1.add(new SimpleVirtualMachine("vm3", 1, 1, 1));
    params.add(s1);
    try {
      ContinuousSpread sc = mb.buildConstraint(params);
      Assert.assertEquals(sc.getAllVirtualMachines().size(), 3);
      Assert.assertEquals(sc.getVirtualMachines(), s1);
    }
    catch (ConstraintBuilderException e) {
      Assert.fail(e.getMessage(), e);
    }
  }

  /** Test spread({vm1}, {vm2}). must fail: 2 params */
  @Test(expectedExceptions = { ConstraintBuilderException.class })
  public void testWithBadParamsNumber() throws ConstraintBuilderException {
    ContinuousSpreadBuilder mb = new ContinuousSpreadBuilder();
    List<VJobElement> params = new LinkedList<VJobElement>();
    ExplodedSet<SimpleVirtualMachine> s1 = new ExplodedSet<SimpleVirtualMachine>();
    s1.add(new SimpleVirtualMachine("vm1", 1, 1, 1));
    ExplodedSet<SimpleVirtualMachine> s2 = new ExplodedSet<SimpleVirtualMachine>();
    s2.add(new SimpleVirtualMachine("vm2", 1, 1, 1));
    params.add(s1);
    params.add(s2);
    mb.buildConstraint(params);
  }

  /** Test spread({}). must fail (empty set) */
  @Test(expectedExceptions = { ConstraintBuilderException.class })
  public void testWithEmptySet() throws ConstraintBuilderException {
    ContinuousSpreadBuilder mb = new ContinuousSpreadBuilder();
    List<VJobElement> params = new LinkedList<VJobElement>();
    ExplodedSet<ManagedElement> s1 = new ExplodedSet<ManagedElement>();
    params.add(s1);
    mb.buildConstraint(params);
  }
}

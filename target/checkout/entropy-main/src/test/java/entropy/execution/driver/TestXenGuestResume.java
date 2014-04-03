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
package entropy.execution.driver;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.TestHelper;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.plan.action.Resume;

/** Unit tests for XenGuestResume.
 * @author Fabien Hermenier */
@Test(groups = { "unit" })
public class TestXenGuestResume {

  /** Test the customization with the default properties. */
  public void testDefaultProperties() {
    try {
      XenGuestResume m = new XenGuestResume(new Resume(
          new SimpleVirtualMachine("vm1", 0, 0, 0), new SimpleNode("n1", 0, 0,
              0), new SimpleNode("n2", 0, 0, 0)),
          TestHelper.readDefaultEntropyProperties());
      Assert.assertEquals(m.getStateFilesLocation(), "/snapshots");
    }
    catch (Exception e) {
      Assert.fail(e.getMessage(), e);
    }
  }
}

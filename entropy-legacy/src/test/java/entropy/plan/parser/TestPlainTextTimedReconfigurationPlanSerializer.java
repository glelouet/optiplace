/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.plan.parser;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.configuration.*;
import entropy.plan.Defaultentropy.plan.ReconfigurationResult;
import entropy.plan.entropy.plan.ReconfigurationResult;
import entropy.plan.action.*;
import entropy.plan.action.Shutdown;

/** @author Fabien Hermenier */
@Test(groups = { "unit" })
public class TestPlainTextTimedReconfigurationPlanSerializer {

  /** Write a plan and read it. Original and readed plan must be identical */
  public void loopTest() {
    Configuration c = new SimpleConfiguration();
    Node[] nodes = new SimpleNode[10];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = new SimpleNode("N" + (i + 1), 1, 1, 1);
      if (i < 8) {
        c.addOnline(nodes[i]);
      } else {
        c.addOffline(nodes[i]);
      }
    }
    VirtualMachine[] vms = new SimpleVirtualMachine[10];
    for (int i = 0; i < c.nbVMs(); i++) {
      vms[i] = new SimpleVirtualMachine("VM" + (i + 1), 1, 1, 1);
    }
    c.addWaiting(vms[0]);
    c.addWaiting(vms[1]);
    c.setRunOn(vms[2], nodes[0]);
    c.setRunOn(vms[3], nodes[1]);
    c.setRunOn(vms[6], nodes[1]);
    c.setSleepOn(vms[4], nodes[2]);

    System.out.println(c);
    entropy.plan.ReconfigurationResult p = new Defaultentropy.plan.ReconfigurationResult(c);
    p.add(new Startup(nodes[8], 0, 5));
    p.add(new Startup(nodes[9], 0, 5));
    p.add(new Shutdown(nodes[6], 0, 5));
    p.add(new Migration(vms[2], nodes[0], nodes[3], 5, 10));
    p.add(new Suspend(vms[3], nodes[1], nodes[1], 5, 10));
    p.add(new Resume(vms[4], nodes[2], nodes[5], 5, 10));
    p.add(new Run(vms[0], nodes[0], 2, 5));
    p.add(new Stop(vms[6], nodes[0], 2, 5));
    System.out.println(c);
    File f = null;
    try {
      f = File.createTempFile(this.getClass().getCanonicalName(), null);
      f.deleteOnExit();
      // f = new File("/Users/fhermeni/tmp.txt");//
      // File.createTempFile("tmp",
      // "tmp");
      PlainTextTimedReconfigurationPlanSerializer.getInstance().write(p,
          f.getAbsolutePath());
      entropy.plan.ReconfigurationResult plan2 = PlainTextTimedReconfigurationPlanSerializer
          .getInstance().read(f.getAbsolutePath());
      Assert.assertTrue(p.equals(plan2), p.getActions().size()
          + " actions vs. " + plan2.getActions().size());
      // f.deleteOnExit();
    }
    catch (Exception e) {
      Assert.fail(e.getMessage(), e);
    }
    finally {
      /* if (f != null) { f.delete(); } */
    }

  }
}

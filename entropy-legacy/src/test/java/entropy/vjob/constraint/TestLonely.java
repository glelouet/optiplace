/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.vjob.constraint;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import common.logging.ChocoLogging;
import entropy.configuration.*;
import entropy.plan.entropy.plan.ReconfigurationResult;
import entropy.plan.choco.ChocoCustomRP;
import entropy.plan.durationEvaluator.MockDurationEvaluator;
import entropy.template.MockVirtualMachineTemplateFactory;
import entropy.vjob.DefaultVJob;
import entropy.vjob.Lonely;
import entropy.vjob.VJob;
import entropy.vjob.builder.DefaultVJobElementBuilder;
import entropy.vjob.builder.protobuf.DefaultPBConstraintsCatalog;
import entropy.vjob.builder.protobuf.ProtobufVJobBuilder;
import entropy.vjob.builder.protobuf.ProtobufVJobSerializer;
import entropy.vjob.builder.xml.DefaultXMLConstraintsCatalog;
import entropy.vjob.builder.xml.LonelyBuilder;
import entropy.vjob.builder.xml.XMLVJobBuilder;
import entropy.vjob.builder.xml.XmlVJobSerializer;

/** Unit tests for (@link Lonely}.
 * @author Fabien Hermenier */
@Test(groups = { "unit" })
public class TestLonely {

  public void testBasics() {
    ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
    vms.add(new SimpleVirtualMachine("VM1", 1, 2, 3));
    vms.add(new SimpleVirtualMachine("VM", 1, 2, 3));
    Lonely l = new Lonely(vms);
    Assert.assertFalse(l.toString().contains("null"));
    Assert.assertEquals(l.getNodes().size(), 0);
    Assert.assertEquals(l.getAllVirtualMachines(), vms);

    Lonely l2 = new Lonely(vms);
    Assert.assertEquals(l, l2);
    Assert.assertEquals(l.hashCode(), l2.hashCode());
    ManagedElementSet<VirtualMachine> vms2 = vms.clone();
    vms2.remove(vms2.get("VM"));
    l2 = new Lonely(vms2);
    Assert.assertNotEquals(l, l2);
    Assert.assertNotEquals(l.hashCode(), l2.hashCode());
  }

  public void testInstantiation() {
    ManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
    vms.add(new SimpleVirtualMachine("VM1", 1, 1, 1));
    vms.add(new SimpleVirtualMachine("VM", 1, 1, 1));
    vms.add(new SimpleVirtualMachine("VM3", 1, 1, 1));
    Lonely l = new Lonely(vms);
    Assert.assertEquals(l.getAllVirtualMachines(), vms);
    Assert.assertNotNull(l.toString());
  }

  public void testIsSatisfied() {
    Configuration cfg = new SimpleConfiguration();
    for (int i = 0; i < 10; i++) {
      Node n = new SimpleNode("N" + i, 5, 5, 5);
      cfg.addOnline(n);
    }

    for (int i = 0; i < 20; i++) {
      VirtualMachine vm = new SimpleVirtualMachine("VM" + i, 1, 1, 1);
      cfg.setRunOn(vm, cfg.getAllNodes().get(i % cfg.getAllNodes().size()));
    }

    ManagedElementSet<VirtualMachine> s1 = new SimpleManagedElementSet<VirtualMachine>();
    s1.add(cfg.getRunnings().get("VM0"));
    s1.add(cfg.getRunnings().get("VM1"));
    s1.add(cfg.getRunnings().get("VM10"));
    s1.add(cfg.getRunnings().get("VM11"));
    Lonely l1 = new Lonely(s1);
    Assert.assertTrue(l1.isSatisfied(cfg));

    ManagedElementSet<VirtualMachine> s2 = new SimpleManagedElementSet<VirtualMachine>();
    s2.add(cfg.getRunnings().get("VM0"));
    s2.add(cfg.getRunnings().get("VM1"));
    s2.add(cfg.getRunnings().get("VM"));
    s2.add(cfg.getRunnings().get("VM7"));
    Lonely l2 = new Lonely(s2);
    Assert.assertFalse(l2.isSatisfied(cfg));
  }

  public void testGetMisplaced() {
    Configuration cfg = new SimpleConfiguration();
    for (int i = 0; i < 10; i++) {
      Node n = new SimpleNode("N" + i, 5, 5, 5);
      cfg.addOnline(n);
    }

    for (int i = 0; i < 20; i++) {
      VirtualMachine vm = new SimpleVirtualMachine("VM" + i, 1, 1, 1);
      cfg.setRunOn(vm, cfg.getAllNodes().get(i % cfg.getAllNodes().size()));
    }

    ManagedElementSet<VirtualMachine> s1 = new SimpleManagedElementSet<VirtualMachine>();
    s1.add(cfg.getRunnings().get("VM0"));
    s1.add(cfg.getRunnings().get("VM1"));
    s1.add(cfg.getRunnings().get("VM10"));
    s1.add(cfg.getRunnings().get("VM11"));
    Lonely l1 = new Lonely(s1);
    Assert.assertEquals(l1.getMisPlaced(cfg).size(), 0); // Cause it is
    // satisfied

    ManagedElementSet<VirtualMachine> s2 = new SimpleManagedElementSet<VirtualMachine>();
    s2.add(cfg.getRunnings().get("VM0"));
    s2.add(cfg.getRunnings().get("VM1"));
    s2.add(cfg.getRunnings().get("VM10"));
    s2.add(cfg.getRunnings().get("VM11"));
    s2.add(cfg.getRunnings().get("VM"));
    s2.add(cfg.getRunnings().get("VM7"));
    Lonely l2 = new Lonely(s2);
    ManagedElementSet<VirtualMachine> bads = new SimpleManagedElementSet<VirtualMachine>();
    bads.add(cfg.getRunnings().get("VM"));
    bads.add(cfg.getRunnings().get("VM7"));
    Assert.assertEquals(l2.getMisPlaced(cfg), bads);
  }

  public void test1() {
    Configuration cfg = new SimpleConfiguration();
    for (int i = 0; i < 5; i++) {
      Node n = new SimpleNode("N" + i, 5, 5, 5);
      cfg.addOnline(n);
    }

    for (int i = 0; i < 10; i++) {
      VirtualMachine vm = new SimpleVirtualMachine("VM" + i, 1, 1, 1);
      cfg.setRunOn(vm, cfg.getAllNodes().get(i % cfg.getAllNodes().size()));
    }

    ManagedElementSet<VirtualMachine> s2 = new SimpleManagedElementSet<VirtualMachine>();
    s2.add(cfg.getRunnings().get("VM0"));
    s2.add(cfg.getRunnings().get("VM1"));
    Lonely l2 = new Lonely(s2);

    ChocoCustomRP plan = new ChocoCustomRP(new MockDurationEvaluator(9, 3, 2,
        3, 4, 5, 6, 7, 8));
    plan.setRepairMode(false);
    List<VJob> vjobs = new ArrayList<VJob>();
    try {
      VJob v = new DefaultVJob("v1");
      v.addVirtualMachines(cfg.getAllVirtualMachines());
      v.addConstraint(l2);
      vjobs.add(v);
      // plan.setTimeLimit(10);

      entropy.plan.ReconfigurationResult p = plan.compute(cfg,
          cfg.getAllVirtualMachines(), cfg.getWaitings(), cfg.getSleepings(),
          new SimpleManagedElementSet<VirtualMachine>(), cfg.getOnlines(),
          cfg.getOfflines(), vjobs);
      System.err.println(p);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage(), e);
    }
    finally {
      System.err.flush();
      ChocoLogging.flushLogs();
    }
  }

  /** Test if protobuf serialization if fine by doing a cycle
   * serialization/deserialization */
  public void testProtobufSerialization() {

    Configuration cfg = new SimpleConfiguration();
    Node n1 = new SimpleNode("N1", 1, 1, 1);
    Node n2 = new SimpleNode("N2", 1, 1, 1);
    VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 1);
    VirtualMachine vm2 = new SimpleVirtualMachine("VM", 1, 1, 1);
    VirtualMachine vm3 = new SimpleVirtualMachine("VM3", 1, 1, 1);

    cfg.addOnline(n1);
    cfg.addOnline(n2);
    cfg.setRunOn(vm1, n1);
    cfg.setRunOn(vm2, n1);
    cfg.setSleepOn(vm3, n2);

    Lonely s = new Lonely(cfg.getAllVirtualMachines());
    VJob v = new DefaultVJob("V1");
    v.addConstraint(s);
    try {
      File out = File.createTempFile("vjob", "pbd");
      out.deleteOnExit();
      ProtobufVJobSerializer.getInstance().write(v, out.getPath());
      ProtobufVJobBuilder vb = new ProtobufVJobBuilder(
          new DefaultVJobElementBuilder(
              new MockVirtualMachineTemplateFactory(), null));
      vb.getElementBuilder().useConfiguration(cfg);
      DefaultPBConstraintsCatalog cat = new DefaultPBConstraintsCatalog();
      cat.add(new entropy.vjob.builder.protobuf.LonelyBuilder());
      vb.setConstraintCatalog(cat);
      VJob v2 = vb.build(out);
      Lonely s2 = (Lonely) v2.getConstraints().iterator().next();
      Assert.assertEquals(s, s2);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage(), e);
    }
  }

  /** Test if xml serialization if fine by doing a cycle
   * serialization/deserialization */
  public void testXMLSerialization() {
    Configuration cfg = new SimpleConfiguration();
    Node n1 = new SimpleNode("N1", 1, 1, 1);
    Node n2 = new SimpleNode("N2", 1, 1, 1);
    VirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 1, 1);
    VirtualMachine vm2 = new SimpleVirtualMachine("VM", 1, 1, 1);
    VirtualMachine vm3 = new SimpleVirtualMachine("VM3", 1, 1, 1);

    cfg.addOnline(n1);
    cfg.addOnline(n2);
    cfg.setRunOn(vm1, n1);
    cfg.setRunOn(vm2, n1);
    cfg.setSleepOn(vm3, n2);

    Lonely r = new Lonely(cfg.getAllVirtualMachines());
    VJob v = new DefaultVJob("V1");
    v.addConstraint(r);
    try {
      File out = File.createTempFile("vjob", "pbd");
      out.deleteOnExit();
      DefaultXMLConstraintsCatalog cat = new DefaultXMLConstraintsCatalog();
      cat.add(new LonelyBuilder());
      XmlVJobSerializer.getInstance().write(v, out.getPath());
      XMLVJobBuilder vb = new XMLVJobBuilder(new DefaultVJobElementBuilder(
          new MockVirtualMachineTemplateFactory(), null), cat);
      vb.getElementBuilder().useConfiguration(cfg);
      VJob v2 = vb.build(out);
      Lonely s2 = (Lonely) v2.getConstraints().iterator().next();
      Assert.assertEquals(r, s2);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage(), e);
    }
  }
}

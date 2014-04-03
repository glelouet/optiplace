/* Copyright (c) 2009 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.configuration;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.configuration.SimpleVirtualMachine;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * Unit tests for Configuration.
 * 
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestConfiguration {

	/**
	 * Make a Simple configuration for test purpose. VM0 to VM9 are affected to
	 * node N0 to N9. Node N10 to N14 are unused VirtualMachine VM10 to VM14 are
	 * unaffected
	 * 
	 * @return the configuration
	 */
	private static SimpleConfiguration makeSimpleConfiguration() {
		SimpleConfiguration c = new SimpleConfiguration();
		for (int i = 0; i < 10; i++) {
			SimpleVirtualMachine vm = new SimpleVirtualMachine("VM" + i, 1,
					1024, 2);
			SimpleNode n = new SimpleNode("N" + i, 2, 100, 4096);
			c.addOnline(n);
			c.setRunOn(vm, n);
		}
		for (int i = 0; i < 5; i++) {
			c.addOnline(new SimpleNode("N1" + i, 1, 100, 4096));
		}

		for (int i = 0; i < 5; i++) {
			c.addOffline(new SimpleNode("-N1" + i, 1, 100, 4096));
		}

		for (int i = 10; i < 15; i++) {
			c.addWaiting(new SimpleVirtualMachine("VM" + i, 1, 0, 1024));
		}
		for (int i = 15; i < 20; i++) {
			c.setSleepOn(new SimpleVirtualMachine("VM" + i, 1, 0, 1024), c
					.getOnlines().get("N" + (i - 10)));
		}
		return c;
	}

	/** Test getNodes(). */
	public void testGetOnlines() {
		Configuration c = new SimpleConfiguration();
		Assert.assertEquals(c.getOnlines().size(), 0);
		c = TestConfiguration.makeSimpleConfiguration();
		Assert.assertEquals(c.getOnlines().size(), 15);
	}

	/** Test getRunnings(). */
	public void testGetRunnings() {
		Configuration c = TestConfiguration.makeSimpleConfiguration();
		Assert.assertEquals(c.getRunnings().size(), 10);
		for (int i = 0; i < 10; i++) {
			Node n = c.getOnlines().get("N" + i);
			VirtualMachine vm = c.getRunnings().get("VM" + i);

			Assert.assertEquals(c.getRunnings(n).size(), 1);
			Assert.assertTrue(c.getRunnings(n).contains(vm));
		}
	}

	/** Test getSleepings(). */
	public void testGetSleepings() {
		SimpleConfiguration c = TestConfiguration.makeSimpleConfiguration();
		Assert.assertEquals(c.getSleepings().size(), 5);
		Assert.assertEquals(c.getSleepings(c.getOnlines().get("N5")).size(), 1);
	}

	/** Tets getWaitings(). */
	public void testGetWaitings() {
		SimpleConfiguration c = TestConfiguration.makeSimpleConfiguration();
		Assert.assertEquals(c.getWaitings().size(), 5);
	}

	/** Test equals() when the configurations are the same. */
	public void testEquals() {
		SimpleConfiguration c = TestConfiguration.makeSimpleConfiguration();
		SimpleConfiguration c2 = TestConfiguration.makeSimpleConfiguration();
		Assert.assertEquals(c2, c);
	}

	/**
	 * Test equals() when the configuration are not the same, for several
	 * reasons.
	 */
	public void testNotEquals() {
		SimpleConfiguration c = TestConfiguration.makeSimpleConfiguration();
		SimpleConfiguration c2 = TestConfiguration.makeSimpleConfiguration();

		// c2 has an additional VM
		c2.addWaiting(new SimpleVirtualMachine("new", 1, 2, 3));
		Assert.assertNotSame(c2, c);

		// Affectations are not the same
		c2 = TestConfiguration.makeSimpleConfiguration();
		c2.setRunOn(c2.getRunnings().get(0), c2.getOnlines().get(0));
		Assert.assertNotSame(c2, c);

		// c2 has not the same nodes
		c2 = TestConfiguration.makeSimpleConfiguration();
		c2.addOffline(new SimpleNode("NX", 1, 2, 3));
		Assert.assertNotSame(c2, c);
	}

	/** Test addOnline in several conditions. */
	public void testAddOnline() {
		Configuration c = TestConfiguration.makeSimpleConfiguration();
		Node n = new SimpleNode("NA", 1, 2, 3);
		c.addOnline(n);
		Assert.assertEquals(c.getOnlines().size(), 16);
		Assert.assertTrue(c.getOnlines().contains(n));

		// A node that is offline.
		n = new SimpleNode("-N0", 1, 2, 3);
		c.addOnline(n);
		Assert.assertEquals(c.getOnlines().size(), 17);
		Assert.assertTrue(c.getOnlines().contains(n));
		Assert.assertFalse(c.getOfflines().contains(n));
	}

	/** Test addOffline in several conditions. */
	public void testAddOffline() {
		SimpleConfiguration c = TestConfiguration.makeSimpleConfiguration();
		SimpleNode n = new SimpleNode("-N30", 1, 2, 3);
		Assert.assertEquals(c.getOfflines().size(), 5);
		c.addOffline(n);
		Assert.assertEquals(c.getOfflines().size(), 6);
		Assert.assertTrue(c.getOfflines().contains(n));

		// A node that is online.
		n = new SimpleNode("N12", 1, 2, 3);
		Assert.assertTrue(c.addOffline(n));
		Assert.assertFalse(c.getOnlines().contains(n));
		Assert.assertTrue(c.getOfflines().contains(n));

		// A node that contains virtual machines. So should not be allowed
		n = new SimpleNode("N3", 1, 2, 3);
		Assert.assertFalse(c.addOffline(n));
		Assert.assertFalse(c.getOfflines().contains(n));
	}

	/** Test getAffected(). */
	public void testGetPosition() {
		Configuration c = TestConfiguration.makeSimpleConfiguration();
		for (int i = 0; i < 10; i++) {
			Node n = c.getOnlines().get("N" + i);
			VirtualMachine vm = c.getRunnings().get("VM" + i);
			Assert.assertEquals(c.getLocation(vm), n);
		}
	}

	/** Dummy test for toString, to avoid NullPointerException. */
	public void testToString() {
		Assert.assertNotNull(TestConfiguration.makeSimpleConfiguration()
				.toString());
	}

	/** Tests for setRunOn() in several conditions. */
	public void testSetRunOn() {
		Configuration c = makeSimpleConfiguration();
		VirtualMachine vm = new SimpleVirtualMachine("toto", 1, 2, 3);
		Node n = c.getOnlines().get(0);
		c.setRunOn(vm, n);
		Assert.assertTrue(c.getRunnings().contains(vm));
		Assert.assertTrue(c.getRunnings(n).contains(vm));

		// Test with a VM that was waiting
		vm = c.getWaitings().get("VM10");
		c.setRunOn(vm, n);
		Assert.assertFalse(c.getWaitings().contains(vm));
		Assert.assertTrue(c.getRunnings().contains(vm));
		Assert.assertTrue(c.getRunnings(n).contains(vm));

		// Test with a VM that was sleeping
		vm = c.getSleepings().get("VM15");
		Node oldNode = c.getLocation(vm);
		c.setRunOn(vm, n);
		Assert.assertFalse(c.getSleepings().contains(vm));
		Assert.assertFalse(c.getSleepings(oldNode).contains(vm));
		Assert.assertTrue(c.getRunnings(n).contains(vm));

		// Test on a offline node
		vm = new SimpleVirtualMachine("op", 1, 2, 3);
		n = c.getOfflines().get(0);
		c.setRunOn(vm, n);
		Assert.assertFalse(c.getRunnings().contains(vm));
		Assert.assertFalse(c.getRunnings(n).contains(vm));
		Assert.assertNotSame(c.getLocation(vm), n);

		// Relocation of a VM
		c.setRunOn(vm, c.getOnlines().get(c.getOnlines().size() - 1));
		Assert.assertEquals(c.getLocation(vm),
				c.getOnlines().get(c.getOnlines().size() - 1));
		Assert.assertFalse(c.getRunnings(c.getOnlines().get(0)).contains(vm));
		Assert.assertTrue(c.getRunnings(
				c.getOnlines().get(c.getOnlines().size() - 1)).contains(vm));
	}

	/** Tests for setRunOn() in several conditions. */
	public void testSetSleepOn() {
		Configuration c = makeSimpleConfiguration();
		VirtualMachine vm = new SimpleVirtualMachine("toto", 1, 2, 3);
		Node n = c.getOnlines().get(0);
		c.setSleepOn(vm, n);
		Assert.assertTrue(c.getSleepings().contains(vm));
		Assert.assertTrue(c.getSleepings(n).contains(vm));
		Assert.assertEquals(c.getLocation(vm), n);

		// Test with a VM that was waiting
		vm = c.getWaitings().get("VM10");
		c.setSleepOn(vm, n);
		Assert.assertFalse(c.getWaitings().contains(vm));
		Assert.assertTrue(c.getSleepings().contains(vm));
		Assert.assertTrue(c.getSleepings(n).contains(vm));
		Assert.assertEquals(c.getLocation(vm), n);

		// Test with a VM that was running
		vm = c.getRunnings().get("VM1");
		Node oldNode = c.getLocation(vm);
		c.setSleepOn(vm, n);
		Assert.assertFalse(c.getRunnings().contains(vm));
		Assert.assertFalse(c.getRunnings(oldNode).contains(vm));
		Assert.assertTrue(c.getSleepings(n).contains(vm));
		Assert.assertEquals(c.getLocation(vm), n);

		// Test on a offline node
		vm = new SimpleVirtualMachine("op", 1, 2, 3);
		n = c.getOfflines().get(0);
		c.setSleepOn(vm, n);
		Assert.assertFalse(c.getSleepings().contains(vm));
		Assert.assertFalse(c.getSleepings(n).contains(vm));
		Assert.assertNotSame(c.getLocation(vm), n);

		// Test on a unknown node
	}

	/** Test addWaiting() in several situations. */
	public void testAddWaiting() {
		SimpleConfiguration c = makeSimpleConfiguration();
		VirtualMachine vm = new SimpleVirtualMachine("hop", 1, 2, 3);
		c.addWaiting(vm);
		Assert.assertTrue(c.getWaitings().contains(vm));

		// Test with a VM that was running
		vm = c.getRunnings().get("VM1");
		Node oldNode = c.getLocation(vm);
		c.addWaiting(vm);
		Assert.assertFalse(c.getRunnings().contains(vm));
		Assert.assertFalse(c.getRunnings(oldNode).contains(vm));
		Assert.assertTrue(c.getWaitings().contains(vm));

		// Test with a VM that was sleeping
		vm = c.getSleepings().get("VM15");
		oldNode = c.getSleepingLocation(vm);
		c.addWaiting(vm);
		Assert.assertFalse(c.getSleepings().contains(vm));
		Assert.assertFalse(c.getSleepings(oldNode).contains(vm));
		Assert.assertTrue(c.getWaitings().contains(vm));

	}

	/** Test getRunningLocation(). */
	public void testGetRunningLocation() {
		SimpleConfiguration c = makeSimpleConfiguration();
		Assert.assertEquals(
				c.getRunningLocation(new SimpleVirtualMachine("VM0", 1, 2, 3)),
				c.getOnlines().get("N0"));
		Assert.assertNull(c.getRunningLocation(new SimpleVirtualMachine("zob",
				1, 2, 3)));
	}

	/** Test getSleepingLocation(). */
	public void testGetSleepingLocation() {
		SimpleConfiguration c = makeSimpleConfiguration();
		Assert.assertEquals(c.getSleepingLocation(new SimpleVirtualMachine(
				"VM15", 1, 2, 3)), c.getOnlines().get("N5"));
		Assert.assertNull(c.getSleepingLocation(new SimpleVirtualMachine("zob",
				1, 2, 3)));
	}

	/** Test remove(). */
	public void testRemove() {
		SimpleConfiguration c = makeSimpleConfiguration();
		SimpleNode n1 = new SimpleNode("n1", 1, 2, 3);
		SimpleVirtualMachine vm1 = new SimpleVirtualMachine("VM1", 1, 2, 3);
		c.addOnline(n1);
		c.setRunOn(vm1, n1);
		c.remove(vm1);
		Assert.assertFalse(c.getRunnings().contains(vm1));
		Assert.assertNull(c.getRunningLocation(vm1));
	}
}

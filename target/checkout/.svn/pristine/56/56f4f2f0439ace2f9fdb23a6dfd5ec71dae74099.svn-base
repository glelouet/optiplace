/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.configuration;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Unit tests related to ManagedElementSet.
 * 
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestSimpleManagedElementSet {

	/**
	 * Make a default set of elements.
	 * 
	 * @return a proper set
	 */
	private static ManagedElementSet<MockManagedElement> makeDefaultSet() {
		ManagedElementSet<MockManagedElement> set = new SimpleManagedElementSet<MockManagedElement>();
		MockManagedElement n = new MockManagedElement("N0");
		set.add(n);

		n = new MockManagedElement("N1");
		set.add(n);

		n = new MockManagedElement("N2");
		set.add(n);

		n = new MockManagedElement("N3");
		set.add(n);

		return set;
	}

	@Test
	public void testSingleton() {
		MockManagedElement n = new MockManagedElement("N0");
		ManagedElementSet<MockManagedElement> set = new SimpleManagedElementSet<MockManagedElement>(
				n);
		Assert.assertTrue(set.contains(n));
		Assert.assertEquals(set.size(), 1);
	}

	@Test
	public void testGetFromName() {
		ManagedElementSet<MockManagedElement> orig = TestSimpleManagedElementSet
				.makeDefaultSet();
		Assert.assertEquals(orig.get("N0"), new MockManagedElement("N0"));
		Assert.assertNull(orig.get("N7"));
	}

	/** Check that the copy constructor makes a deep copy. */
	@Test
	public void testCopyConstructor() {
		ManagedElementSet<MockManagedElement> orig = TestSimpleManagedElementSet
				.makeDefaultSet();
		ManagedElementSet<MockManagedElement> copy = orig.clone();

		// Not the same reference
		Assert.assertFalse(orig == copy,
				"The copy should not have the same reference");

		// But the same content
		Assert.assertEquals(copy, orig);
	}

	/** Test the non-possibility of having 2 elements with the same name. */
	@Test
	public void testAdd() {
		ManagedElementSet<MockManagedElement> orig = TestSimpleManagedElementSet
				.makeDefaultSet();
		Assert.assertFalse(orig.add(new MockManagedElement("N0")));
		Assert.assertEquals(orig.size(), 4);
	}

	@Test
	public void testRemove() {
		ManagedElementSet<MockManagedElement> orig = TestSimpleManagedElementSet
				.makeDefaultSet();
		MockManagedElement m = orig.get("N1");
		Assert.assertTrue(orig.remove(m));
		Assert.assertFalse(orig.contains(m));
		MockManagedElement m2 = new MockManagedElement("hop");
		Assert.assertFalse(orig.remove(m2));
	}

	@Test
	public void testClear() {
		ManagedElementSet<MockManagedElement> orig = TestSimpleManagedElementSet
				.makeDefaultSet();
		ManagedElementSet<MockManagedElement> orig2 = orig.clone();
		orig2.clear();
		Assert.assertEquals(orig2.size(), 0);
		for (MockManagedElement e : orig) {
			Assert.assertFalse(orig2.contains(e));
		}

	}

	/** Test the equals() method. */
	@Test
	public void testEquals() {
		ManagedElementSet<MockManagedElement> orig = TestSimpleManagedElementSet
				.makeDefaultSet();
		ManagedElementSet<MockManagedElement> clone = TestSimpleManagedElementSet
				.makeDefaultSet();
		Assert.assertEquals(clone, orig);
		clone.remove(clone.size() - 1);
		Assert.assertNotSame(clone, orig);

		clone = TestSimpleManagedElementSet.makeDefaultSet();
		orig.remove(clone.size() - 1);
		Assert.assertNotSame(clone, orig);

		Assert.assertFalse(orig.equals(new Object()));
	}

	/** Test addAll(). */
	@Test
	public void testAddAll() {
		ManagedElementSet<MockManagedElement> set = makeDefaultSet();
		ManagedElementSet<MockManagedElement> s2 = new SimpleManagedElementSet<MockManagedElement>();
		Assert.assertFalse(set.addAll(s2));
		s2.add(new MockManagedElement("N0"));
		Assert.assertFalse(set.addAll(s2));
		s2.add(new MockManagedElement("aaaa"));
		Assert.assertTrue(set.addAll(s2));
	}

	@Test
	public void testRetainAll() {
		ManagedElementSet<MockManagedElement> set = makeDefaultSet();
		ManagedElementSet<MockManagedElement> toKeep = new SimpleManagedElementSet<MockManagedElement>();
		MockManagedElement n1 = set.get("N1");
		MockManagedElement n2 = set.get("N2");
		MockManagedElement n3 = set.get("N3");
		MockManagedElement n7 = new MockManagedElement("N7");
		toKeep.add(n1);
		toKeep.add(n2);
		toKeep.add(n7);
		Assert.assertTrue(set.retainAll(toKeep));
		Assert.assertTrue(set.contains(n1));
		Assert.assertTrue(set.contains(n2));
		Assert.assertFalse(set.contains(n3));
		Assert.assertNull(set.get("N0"));
	}

	@Test
	public void testToString() {
		ManagedElementSet<MockManagedElement> set = makeDefaultSet();
		Assert.assertEquals(set.toString(), "{N0, N1, N2, N3}");
	}

	@Test
	public void testSet() {
		ManagedElementSet<MockManagedElement> set = makeDefaultSet();
		MockManagedElement n0 = set.get(0);
		MockManagedElement n3 = set.get(3);
		set.set(3, n0);
		set.set(0, n3);
		Assert.assertEquals(set.toString(), "{N3, N1, N2, N0}");
		Assert.assertTrue(set.contains(n0));
		Assert.assertTrue(set.contains(n3));
		Assert.assertEquals(set.get("N3"), n3);
		Assert.assertEquals(set.get("N0"), n0);
	}

	@Test
	public void testSelfContains() {
		SimpleManagedElementSet<VirtualMachine> test = new SimpleManagedElementSet<VirtualMachine>();
		ArrayList<VirtualMachine> list = new ArrayList<VirtualMachine>();
		list.add(new SimpleVirtualMachine("vm01"));
		list.add(new SimpleVirtualMachine("vm02"));
		Assert.assertTrue(test.addAll(list));
		Assert.assertTrue(test.contains(list.get(0)), "does not contain "
				+ list.get(0));
		Assert.assertTrue(test.contains(list.get(1)), "does not contain "
				+ list.get(1));
		Assert.assertTrue(test.containsAll(test));
	}

}

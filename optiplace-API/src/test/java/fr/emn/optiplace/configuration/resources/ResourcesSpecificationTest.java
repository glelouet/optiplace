/**
 *
 */

package fr.emn.optiplace.configuration.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2015
 *
 */
public class ResourcesSpecificationTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ResourcesSpecificationTest.class);

	@Test
	public void testComparatorNodeSort() {
		Node[] nodes = new Node[10];
		Map<VMHoster, Integer> capacities = new HashMap<>();
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new Node("n" + i);
			capacities.put(nodes[i], i + i % 2 * 10);
		}
		ResourceSpecification test = new ResourceSpecification() {

			@Override
			public void readLine(String line) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getType() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int getCapacity(VMHoster n) {
				return capacities.get(n);
			}

			@Override
			public Stream<VMHoster> findHosters(IConfiguration c, Predicate<Integer> filter) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void use(VM v, int use) {
				throw new UnsupportedOperationException("implement this !");
			}

			@Override
			public int getUse(VM vm) {
				throw new UnsupportedOperationException("implement this !");
			}

			@Override
			public void capacity(VMHoster h, int capacity) {
				capacities.put(h, capacity);
			}

			@Override
			public void remove(ManagedElement e) {
			}

			@Override
			public ResourceSpecification clone() {
				throw new UnsupportedOperationException();
			}
		};
		ArrayList<Node> list = new ArrayList<>(Arrays.asList(nodes));

		List<Node> expected = Arrays.asList(nodes[0], nodes[2], nodes[4], nodes[6], nodes[8], nodes[1], nodes[3], nodes[5],
				nodes[7], nodes[9]);
		Comparator<Node> c = test.makeNodeComparator(true);
		Collections.sort(list, c);
		Assert.assertEquals(list, expected);

		Collections.reverse(expected);
		c = test.makeNodeComparator(false);
		Collections.sort(list, c);
		Assert.assertEquals(list, expected);
	}

	@Test
	public void testComparatorVMSort() {
		VM[] vms = new VM[10];
		Map<VM, Integer> uses = new HashMap<>();
		for (int i = 0; i < vms.length; i++) {
			vms[i] = new VM("n" + i);
			uses.put(vms[i], i + i % 2 * 10);
		}
		ResourceSpecification test = new ResourceSpecification() {

			@Override
			public void readLine(String line) {
				throw new UnsupportedOperationException();
			}

			@Override
			public String getType() {
				throw new UnsupportedOperationException();
			}

			@Override
			public int getCapacity(VMHoster n) {
				throw new UnsupportedOperationException();
			}

			@Override
			public Stream<VMHoster> findHosters(IConfiguration c, Predicate<Integer> val) {
				throw new UnsupportedOperationException();
			}

			@Override
			public void use(VM v, int use) {
				uses.put(v, use);
			}

			@Override
			public int getUse(VM vm) {
				Integer ret = uses.get(vm);
				return ret != null ? ret : 0;
			}

			@Override
			public void capacity(VMHoster h, int capacity) {
				throw new UnsupportedOperationException("implement this !");
			}

			@Override
			public void remove(ManagedElement e) {
			}

			@Override
			public ResourceSpecification clone() {
				throw new UnsupportedOperationException();
			}
		};
		ArrayList<VM> list = new ArrayList<>(Arrays.asList(vms));

		List<VM> expected = Arrays.asList(vms[0], vms[2], vms[4], vms[6], vms[8], vms[1], vms[3], vms[5], vms[7], vms[9]);
		Comparator<VM> c = test.makeVMComparator(true);
		Collections.sort(list, c);
		Assert.assertEquals(list, expected);

		Collections.reverse(expected);
		c = test.makeVMComparator(false);
		Collections.sort(list, c);
		Assert.assertEquals(list, expected);
	}
}

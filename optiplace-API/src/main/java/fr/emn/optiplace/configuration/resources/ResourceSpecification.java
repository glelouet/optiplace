package fr.emn.optiplace.configuration.resources;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.*;
import fr.emn.optiplace.view.ProvidedDataReader;

/**
 * specifies, for one resource known as its {@link #getType() type} the
 * capacities of the nodes and the consumptions of the VMs for that resource.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface ResourceSpecification extends ProvidedDataReader {

	/** @return the type of the resources, used as an ID. */
	String getType();

	int getLoad(VM vm);

	/**
	 * @param vms
	 *          the vms to get the usages. Should not contain null values.
	 * @return the array of usages of this resource by the vms, such as
	 *         ret[i]:=usage(vms[i])
	 */
	default int[] getUses(VM... vms) {
		int[] ret = new int[vms.length];
		for (int i = 0; i < vms.length; i++) {
			ret[i] = getLoad(vms[i]);
		}
		return ret;
	}

	int getCapacity(VMHoster h);

	/**
	 * @param nodes
	 *          the nodes to get the capacities. Should not contain null values.
	 * @return the array of capacities of this resource by the nodes, such as
	 *         ret[i]:=capacity(nodes[i])
	 */
	default int[] getCapacities(Node... nodes) {
		int[] ret = new int[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			ret[i] = getCapacity(nodes[i]);
		}
		return ret;
	}

	/**
	 * @return a map of the usages of the vms. Should return a value even if a vm
	 *         is not present.
	 */
	Map<VM, Integer> toUses();

	/**
	 * set the use of a VM
	 * 
	 * @param v
	 *          the VM
	 * @param use
	 *          the use value of this VM
	 */
	default void use(VM v, int use) {
		toUses().put(v, use);
	}

	/**
	 * @return a map of the capacities of the nodes. should return a value even if
	 *         a node is not present.
	 */
	Map<VMHoster, Integer> toCapacities();

	/**
	 * set the capacity of the hoster
	 * 
	 * @param h
	 *          the hoster
	 * @param capacity
	 *          the capacity value of the hoster
	 */
	default void capacity(VMHoster h, int capacity) {
		toCapacities().put(h, capacity);
	}

	/**
	 * get the sum of the uses of vms
	 *
	 * @param vms
	 *          the virtualMachines hosted on the given node
	 * @return the sum of the use of the vms
	 */
	default int getUse(VM... vms) {
		int sum = 0;
		if (vms != null) {
			for (VM vm : vms) {
				sum += getLoad(vm);
			}
		}
		return sum;
	}

	/**
	 * get the use of a node in a given configuration
	 *
	 * @param cfg
	 *          the configuration
	 * @param n
	 *          the node
	 * @return the use of the node
	 */
	default int getUse(Configuration cfg, Node n) {
		return cfg.getHosted(n).collect(Collectors.summingInt(this::getLoad));
	}

	/** get the total use of the VMs running in the center */
	default int getUse(Configuration cfg) {
		return cfg.getVMs().mapToInt(this::getLoad).sum();
	}

	/** get the total capacity of the nodes which are online */
	default int getCapacity(Configuration cfg) {
		return cfg.getOnlines().mapToInt(this::getCapacity).sum();
	}

	/**
	 * check wether a vm can be hosted on given node.
	 *
	 * @param n
	 *          the node
	 * @param vm
	 *          the vm
	 * @return true if there is enough resource on n to host vm.
	 */
	default boolean canHost(Configuration cfg, VMHoster n, VM vm) {
		if (n instanceof Node) {
			return getUse(cfg, (Node) n) + getLoad(vm) <= getCapacity(n);
		} else if (n instanceof Extern) {
			return getLoad(vm) <= getCapacity(n);
		}
		throw new UnsupportedOperationException("can't handle the class " + n.getClass());
	}

	/**
	 * make a comparator on VMs, based on this resource's uses. this comparator
	 * can then be used to sort collections according to the VMs' resource use
	 *
	 * @param increasing
	 *          true to make Collections.sort() sort with increasing order, false
	 *          to make decreasing order
	 * @return a new comparator, backing to this.
	 */
	default Comparator<VM> makeVMComparator(boolean increasing) {
		if (increasing) {
			return new Comparator<VM>() {
				@Override
				public int compare(VM o1, VM o2) {
					return getLoad(o1) - getLoad(o2);
				}

				@Override
				public String toString() {
					return "VMCOMP:" + getType() + "-" + (increasing ? "inc" : "dec");
				};
			};
		} else {
			return new Comparator<VM>() {
				@Override
				public int compare(VM o1, VM o2) {
					return getLoad(o2) - getLoad(o1);
				}

				@Override
				public String toString() {
					return "VMCOMP:" + getType() + "-" + (increasing ? "inc" : "dec");
				};
			};
		}
	}

	default Comparator<Node> makeNodeComparator(boolean increasing) {
		return new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				return (increasing ? 1 : -1) * (getCapacity(o1) - getCapacity(o2));
			}

			@Override
			public String toString() {
				return "NodeCOMP:" + getType() + "-" + (increasing ? "inc" : "dec");
			}

			@Override
			public boolean equals(Object obj) {
				return obj != null && obj instanceof Comparator<?> && toString().equals(obj.toString());
			}

			@Override
			public int hashCode() {
				return toString().hashCode();
			}
		};
	}

	/**
	 * add resource specifications to a (potential null) map
	 *
	 * @param map
	 *          the map to add data inside, or null to create one
	 * @param res
	 *          the list of resource specifications. can be null or empty
	 * @return map if not null or a new map, into which specifications have been
	 *         added
	 */
	public static HashMap<String, ResourceSpecification> toMap(HashMap<String, ResourceSpecification> map,
			ResourceSpecification... res) {
		HashMap<String, ResourceSpecification> ret = map == null ? new HashMap<String, ResourceSpecification>() : map;
		if (res != null) {
			for (ResourceSpecification r : res) {
				ret.put(r.getType(), r);
			}
		}
		return ret;
	}

	/**
	 * find all the hosters with strictly less capacity than given value
	 *
	 * @param val
	 *          the value to compare the hosters capacities to
	 * @return a new stream of the Hosters
	 */
	public Stream<VMHoster> findHostersWithLess(int val);

}

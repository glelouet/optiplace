package fr.emn.optiplace.configuration.resources;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * specifies, for one resource known as its {@link #getType() type} the
 * capacities of the nodes and the consumptions of the VMs for that resource.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface ResourceSpecification {

	/** @return the type of the resources, used as an ID. */
	String getType();

	int getUse(VirtualMachine vm);

	/**
	 * @param vms
	 * the vms to get the usages. Should not contain null values.
	 * @return the array of usages of this resource by the vms, such as
	 * ret[i]:=usage(vms[i])
	 */
	default int[] getUses(VirtualMachine... vms) {
		int[] ret = new int[vms.length];
		for (int i = 0; i < vms.length; i++) {
			ret[i] = getUse(vms[i]);
		}
		return ret;
	}

	int getCapacity(Node n);

	/**
	 * @param nodes
	 * the nodes to get the capacities. Should not contain null values.
	 * @return the array of capacities of this resource by the nodes, such as
	 * ret[i]:=capacity(nodes[i])
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
	 * is not present.
	 */
	Map<VirtualMachine, Integer> toUses();

	/**
	 * @return a map of the capacities of the nodes. should return a value even if
	 * a node is not present.
	 */
	Map<Node, Integer> toCapacities();

	/**
	 * get the sum of the uses of vms
	 *
	 * @param vms
	 * the virtualMachines hosted on the given node
	 * @return the sum of the use of the vms
	 */
	default int getUse(VirtualMachine... vms) {
		int sum = 0;
		if (vms != null) {
			for (VirtualMachine vm : vms) {
				sum += getUse(vm);
			}
		}
		return sum;
	}

	/**
	 * get the use of a node in a given configuration
	 *
	 * @param cfg
	 * the configuration
	 * @param n
	 * the node
	 * @return the use of the node
	 */
	default int getUse(Configuration cfg, Node n) {
		return cfg.getHosted(n).collect(Collectors.summingInt(this::getUse));
	}

	/** get the total use of the VMs running in the center */
	default int getUse(Configuration cfg) {
		return cfg.getRunnings().mapToInt(this::getUse).sum();
	}

	/** get the total capacity of the nodes which are online */
	default int getCapacity(Configuration cfg) {
		return cfg.getOnlines().mapToInt(this::getCapacity).sum();
	}

	/**
	 * check wether a vm can be hosted on given node.
	 *
	 * @param n
	 * the node
	 * @param vm
	 * the vm
	 * @return true if there is enough resource on n to host vm.
	 */
	default boolean canHost(Configuration cfg, Node n, VirtualMachine vm) {
		return getUse(cfg, n) + getUse(vm) <= getCapacity(n);
	}

	/**
	 * make a comparator on VMs, based on this resource's uses. this comparator
	 * can then be used to sort collections according to the VMs' resource use
	 *
	 * @param increasing
	 * true to make Collections.sort() sort with increasing order, false to make
	 * decreasing order
	 * @return a new comparator, backing to this.
	 */
	default Comparator<VirtualMachine> makeVMComparator(boolean increasing) {
		if (increasing) {
			return new Comparator<VirtualMachine>() {
				@Override
				public int compare(VirtualMachine o1, VirtualMachine o2) {
					return getUse(o1) - getUse(o2);
				}
			};
		} else {
			return new Comparator<VirtualMachine>() {
				@Override
				public int compare(VirtualMachine o1, VirtualMachine o2) {
					return getUse(o2) - getUse(o1);
				}
			};
		}
	}

	/**
	 * add resource specifications to a (potential null) map
	 * 
	 * @param map
	 * the map to add data inside, or null to create one
	 * @param res
	 * the list of resource specifications. can be null or empty
	 * @return map if not null or a new map, into which specifications have been
	 * added
	 */
	public static HashMap<String, ResourceSpecification> toMap(
			HashMap<String, ResourceSpecification> map, ResourceSpecification... res) {
		HashMap<String, ResourceSpecification> ret = map == null ? new HashMap<String, ResourceSpecification>()
				: map;
		if (res != null) {
			for (ResourceSpecification r : res) {
				ret.put(r.getType(), r);
			}
		}
		return ret;
	}

}

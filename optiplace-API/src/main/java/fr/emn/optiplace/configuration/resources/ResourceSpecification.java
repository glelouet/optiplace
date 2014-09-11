package fr.emn.optiplace.configuration.resources;

import java.util.Comparator;
import java.util.Map;

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

	/**
	 * @param vms
	 * the vms to get the usages. Should not contain null values.
	 * @return the array of usages of this resource by the vms, such as
	 * ret[i]:=usage(vms[i])
	 */
	int[] getUses(VirtualMachine... vms);

	/**
	 * @param nodes
	 * the nodes to get the capacities. Should not contain null values.
	 * @return the array of capacities of this resource by the nodes, such as
	 * ret[i]:=capacity(nodes[i])
	 */
	int[] getCapacities(Node... nodes);

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
	int getUse(VirtualMachine... vms);

	/**
	 * @param n
	 * @return
	 */
	int getCapacity(Node n);

	/**
	 * get the load, as vms consumption / nodes capacity, of the center relative
	 * to this resource
	 *
	 * @param cfg
	 * the center
	 * @return sum of vms consumptions / sum of nodes capacities
	 */
	double getLoad(Configuration cfg);

	/**
	 * get the use of a node in a given configuration
	 *
	 * @param cfg
	 * the configuration
	 * @param n
	 * the node
	 * @return the use of the node
	 */
	double getUse(Configuration cfg, Node n);

	/**
	 * check wether a vm can be hosted on given node.
	 *
	 * @param n
	 * the node
	 * @param vm
	 * the vm
	 * @return true if there is enough resource on n to host vm.
	 */
	boolean canHost(Configuration cfg, Node n, VirtualMachine vm);

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

}

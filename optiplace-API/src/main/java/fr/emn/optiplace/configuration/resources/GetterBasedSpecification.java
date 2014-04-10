package fr.emn.optiplace.configuration.resources;

import java.util.HashMap;
import java.util.Map;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * resource specification based on the presence of {@link #getCapacity(Node)}
 * and {@link #getUse(VirtualMachine)} methods
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public abstract class GetterBasedSpecification implements ResourceSpecification {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(GetterBasedSpecification.class);

	/**
	 * add resource specifications to a (potential null) map
	 * 
	 * @param map
	 *            the map to add data inside, or null to create one
	 * @param res
	 *            the list of resource specifications. can be null or empty
	 * @return map if not null or a new map, into which specifications have been
	 *         added
	 */
	public static HashMap<String, ResourceSpecification> toMap(
			HashMap<String, ResourceSpecification> map,
			ResourceSpecification... res) {
		HashMap<String, ResourceSpecification> ret = map == null
				? new HashMap<String, ResourceSpecification>()
				: map;
		if (res != null) {
			for (ResourceSpecification r : res) {
				ret.put(r.getType(), r);
			}
		}
		return ret;
	}

	/**
	 * @param vm
	 *            a vm to get the usage
	 * @return the usage of the VM
	 */
	public abstract int getUse(VirtualMachine vm);

	/**
	 * @param n
	 *            the node to get the capacity
	 * @return the capacity of the node
	 */
	@Override
	public abstract int getCapacity(Node n);

	@Override
	public int[] getUses(VirtualMachine[] vms) {
		int[] ret = new int[vms.length];
		for (int i = 0; i < vms.length; i++) {
			ret[i] = getUse(vms[i]);
		}
		return ret;
	}

	@Override
	public int[] getCapacities(Node... nodes) {
		int[] ret = new int[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			ret[i] = getCapacity(nodes[i]);
		}
		return ret;
	}

	private final Map<VirtualMachine, Integer> usages = new HashMap<VirtualMachine, Integer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public Integer get(Object key) {
			return getUse((VirtualMachine) key);
		}

	};

	@Override
	public Map<VirtualMachine, Integer> toUses() {
		return usages;
	}

	private final Map<Node, Integer> capacities = new HashMap<Node, Integer>() {

		private static final long serialVersionUID = 1L;

		@Override
		public Integer get(Object key) {
			return getCapacity((Node) key);
		}

	};

	@Override
	public Map<Node, Integer> toCapacities() {
		return capacities;
	}

	@Override
	public int getUse(VirtualMachine... vms) {
		int ret = 0;
		for (VirtualMachine vm : vms) {
			ret += getUse(vm);
		}
		return ret;
	}

	@Override
	public double getLoad(Configuration cfg) {
		double vmsUse = 0, nodesCapa = 0;
		for (Node n : cfg.getAllNodes()) {
			nodesCapa += getCapacity(n);
		}
		for (VirtualMachine vm : cfg.getAllVirtualMachines()) {
			vmsUse += getUse(vm);
		}
		return vmsUse / nodesCapa;
	}

	@Override
	public double getUse(Configuration cfg, Node n) {
		double use = 0;
		for (VirtualMachine vm : cfg.getRunnings(n)) {
			use += getUse(vm);
		}
		return use;
	}
}

/**
 *
 */
package fr.emn.optiplace.configuration.resources;

import java.util.HashMap;
import java.util.Map;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * Store the map of nodes capacities and vms uses in an internal structure.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class MappedResourceSpecification extends GetterBasedSpecification
		implements
			ResourceSpecification {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MappedResourceSpecification.class);

	private String type = null;

	@Override
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
   *
   */
	public MappedResourceSpecification(String type) {
		this.type = type;
	}

	private HashMap<Node, Integer> nodesCapacities = new HashMap<Node, Integer>();

	@Override
	public Map<Node, Integer> toCapacities() {
		return nodesCapacities;
	}

	/**
	 * @param nodesCapacities
	 *            the nodesCapacities to set
	 */
	public void setNodesCapacities(HashMap<Node, Integer> nodesCapacities) {
		this.nodesCapacities = nodesCapacities;
	}

	private HashMap<VirtualMachine, Integer> vmsUses = new HashMap<VirtualMachine, Integer>();

	@Override
	public Map<VirtualMachine, Integer> toUses() {
		return vmsUses;
	}

	/**
	 * @param vmsUses
	 *            the vmsUses to set
	 */
	public void setVmsUses(HashMap<VirtualMachine, Integer> vmsUses) {
		this.vmsUses = vmsUses;
	}

	@Override
	public int getUse(VirtualMachine vm) {
		return vmsUses.get(vm);
	}

	@Override
	public int getCapacity(Node n) {
		return nodesCapacities.get(n);
	}
}

/**
 *
 */
package fr.emn.optiplace.configuration.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.configuration.SimpleVirtualMachine;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.view.ViewConfigurationReader;

/**
 * Store the map of nodes capacities and vms uses in an internal structure.
 * <p>
 * implements the {@link ViewConfigurationReader} so that an view can require a
 * specific resource
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class MappedResourceSpecification implements ResourceSpecification,
		ViewConfigurationReader {

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
	 * the type to set
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

	@Override
	public void readLine(String line) {
		if (line.startsWith(START_NODE_CAPA)) {
			String[] para = line.substring(START_NODE_CAPA.length()).split(" = ");
			nodesCapacities.put(new SimpleNode(para[0]), Integer.parseInt(para[1]));
		} else if (line.startsWith(START_VM_USE)) {
			String[] para = line.substring(START_VM_USE.length()).split(" = ");
			vmsUses.put(new SimpleVirtualMachine (para[0]), Integer.parseInt(para[1]));
		}
	}

	public Stream<String> lines() {
		Stream<String> nodes = nodesCapacities.entrySet().stream().map(e -> {
			return START_NODE_CAPA + e.getKey().getName() + " = " + e.getValue();
		});
		Stream<String> vms = vmsUses.entrySet().stream().map(e -> {
			return START_VM_USE + e.getKey().getName() + " = " + e.getValue();
		});
		return Stream.concat(nodes, vms);
	}

	private static final String START_NODE_CAPA = "capa ";
	private static final String START_VM_USE = "cons ";
}

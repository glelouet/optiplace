/**
 *
 */

package fr.emn.optiplace.configuration.resources;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.view.ProvidedDataReader;


/**
 * Store the map of nodes capacities and vms uses in an internal structure.
 * <p>
 * implements the {@link ProvidedDataReader} so that an view can require a
 * specific resource
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class MappedResourceSpecification implements ResourceSpecification {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MappedResourceSpecification.class);

	private String type = null;

	@Override
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *          the type to set
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

	private HashMap<VMHoster, Integer> hostersCapacities = new HashMap<>();

	@Override
	public Map<VMHoster, Integer> toCapacities() {
		return hostersCapacities;
	}

	/**
	 * @param nodesCapacities
	 *          the nodesCapacities to set
	 */
	public void setNodesCapacities(HashMap<VMHoster, Integer> nodesCapacities) {
		hostersCapacities = nodesCapacities;
	}

	private HashMap<VM, Integer> vmsUses = new HashMap<VM, Integer>();

	@Override
	public Map<VM, Integer> toUses() {
		return vmsUses;
	}

	/**
	 * @param vmsUses
	 *          the vmsUses to set
	 */
	public void setVmsUses(HashMap<VM, Integer> vmsUses) {
		this.vmsUses = vmsUses;
	}

	@Override
	public int getLoad(VM vm) {
		Integer ret = vmsUses.get(vm);
		return ret == null ? 0 : ret;
	}

	@Override
	public int getCapacity(VMHoster h) {
		Integer ret = hostersCapacities.get(h);
		return ret == null ? 0 : ret;
	}

	@Override
	public void readLine(String line) {
		if (line.startsWith(START_NODE_CAPA)) {
			String[] para = line.substring(START_NODE_CAPA.length()).split(" = ");
			hostersCapacities.put(new Node(para[0]), Integer.parseInt(para[1]));
		} else if (line.startsWith(START_VM_USE)) {
			String[] para = line.substring(START_VM_USE.length()).split(" = ");
			vmsUses.put(new VM(para[0]), Integer.parseInt(para[1]));
		}
	}

	public Stream<String> lines() {
		Stream<String> nodes = hostersCapacities.entrySet().stream().map(e -> {
			return START_NODE_CAPA + e.getKey().getName() + " = " + e.getValue();
		});
		Stream<String> vms = vmsUses.entrySet().stream().map(e -> {
			return START_VM_USE + e.getKey().getName() + " = " + e.getValue();
		});
		return Stream.concat(nodes, vms);
	}

	public static final String START_NODE_CAPA = "capa ";
	public static final String START_VM_USE = "cons ";

	@Override
	public String toString() {
		return type + hostersCapacities + vmsUses;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj == null || obj.getClass() != MappedResourceSpecification.class) {
			return false;
		}
		MappedResourceSpecification o = (MappedResourceSpecification) obj;
		if (!type.equals(o.type)) {
			return false;
		}
		if (!hostersCapacities.equals(o.hostersCapacities)) {
			return false;
		}
		if (!vmsUses.equals(o.vmsUses)) {
			return false;
		}
		return true;
	}

	@Override
	public Stream<VMHoster> findHostersWithLess(int val) {
		return hostersCapacities.entrySet().stream().filter(e -> e.getValue() < val).map(Entry::getKey);
	}
}

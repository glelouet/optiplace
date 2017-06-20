/**
 *
 */

package fr.emn.optiplace.configuration.resources;

import java.util.HashMap;
import java.util.stream.Stream;

import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
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

	@Override
	public ResourceSpecification clone() {
		MappedResourceSpecification ret = new MappedResourceSpecification(type);
		ret.vmsUses = new HashMap<>(vmsUses);
		ret.hostersCapacities = new HashMap<>(hostersCapacities);
		return ret;
	}

	private HashMap<VMLocation, Integer> hostersCapacities = new HashMap<>();

	/**
	 * @param nodesCapacities
	 *          the nodesCapacities to set
	 */
	public void setNodesCapacities(HashMap<VMLocation, Integer> nodesCapacities) {
		hostersCapacities = nodesCapacities;
	}

	private HashMap<VM, Integer> vmsUses = new HashMap<>();

	/**
	 * @param vmsUses
	 *          the vmsUses to set
	 */
	public void setVmsUses(HashMap<VM, Integer> vmsUses) {
		this.vmsUses = vmsUses;
	}

	@Override
	public int getUse(VM vm) {
		Integer ret = vmsUses.get(vm);
		return ret != null ? ret : 0;
	}

	@Override
	public int getCapacity(VMLocation h) {
		Integer ret = hostersCapacities.get(h);
		return ret == null ? 0 : ret;
	}

	@Override
	public void readLine(String line) {
		if (line.startsWith(START_NODE_CAPA)) {
			String[] para = line.substring(START_NODE_CAPA.length()).split(" = ");
			hostersCapacities.put(new Computer(para[0]), Integer.parseInt(para[1]));
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
	public void use(VM v, int use) {
		if (use == 0) {
			vmsUses.remove(v);
		} else {
			vmsUses.put(v, use);
		}
	}

	@Override
	public void capacity(VMLocation h, int capacity) {
		if (capacity == 0) {
			hostersCapacities.remove(h);
		} else {
			hostersCapacities.put(h, capacity);
		}
	}

	@Override
	public void remove(ManagedElement e) {
		vmsUses.remove(e);
		hostersCapacities.remove(e);
	}
}

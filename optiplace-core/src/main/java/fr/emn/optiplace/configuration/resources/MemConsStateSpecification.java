package fr.emn.optiplace.configuration.resources;

import fr.emn.optiplace.configuration.CenterStates;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * Memory specification based on the states of the elements.<br />
 * An idle node has no memory available, while a waiting vm's demand is its max
 * mem demand.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
public class MemConsStateSpecification extends GetterBasedSpecification {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MemConsStateSpecification.class);

	private final CenterStates cfg;

	public MemConsStateSpecification(CenterStates cfg) {
		this.cfg = cfg;
	}

	@Override
	public String getType() {
		return "MEM_cons";
	}

	@Override
	public int getUse(VirtualMachine vm) {
		if (cfg.isWaiting(vm)) {
			return vm.getMemoryDemand();
		} else {
			return vm.getMemoryConsumption();
		}
	}

	@Override
	public int getCapacity(Node n) {
		if (cfg.isOnline(n)) {
			return n.getMemoryCapacity();
		} else {
			return 0;
		}
	}
}

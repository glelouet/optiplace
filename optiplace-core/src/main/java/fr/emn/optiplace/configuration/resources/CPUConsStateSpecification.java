package fr.emn.optiplace.configuration.resources;

import fr.emn.optiplace.configuration.CenterStates;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * specification of CPU consumption of the vms and capacities of nodes based on
 * their states.<br />
 * A node <b>offline</b> has no CPU capacity, a vm <b>sleeping</b> or
 * <b>waiting</b> has no CPU consumption.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
public class CPUConsStateSpecification extends GetterBasedSpecification {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(CPUConsStateSpecification.class);

	private final CenterStates cfg;

	public CPUConsStateSpecification(CenterStates cfg) {
		this.cfg = cfg;
	}

	@Override
	public String getType() {
		return "CPU_cons";
	}

	@Override
	public int getUse(VirtualMachine vm) {
		if (cfg.isRunning(vm)) {
			return vm.getCPUConsumption();
		} else {
			return 0;
		}
	}

	@Override
	public int getCapacity(Node n) {
		if (cfg.isOnline(n)) {
			return n.getCPUCapacity();
		} else {
			return 0;
		}
	}
}

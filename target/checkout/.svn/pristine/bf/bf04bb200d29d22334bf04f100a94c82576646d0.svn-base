package entropy.configuration;

import java.util.Map;

import entropy.configuration.resources.ResourceSpecification;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 * 
 */
public class ConfigurationTools {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ConfigurationTools.class);

	/**
	 * 
	 * @param cfg
	 * @return the load (base 1) of the configuration CPU-wise
	 */
	public static double getCPULoad(Configuration cfg) {
		double sumNode = 0, sumVMs = 0;
		for (Node n : cfg.getOnlines()) {
			sumNode += n.getCPUCapacity();
		}
		for (VirtualMachine vm : cfg.getAllVirtualMachines()) {
			sumVMs += vm.getCPUConsumption();
		}
		return sumVMs / sumNode;
	}

	/**
	 * 
	 * @param cfg
	 * @return the load (base 1) of the configuration memory-wise
	 */
	public static double getMemLoad(Configuration cfg) {
		double sumNode = 0, sumVMs = 0;
		for (Node n : cfg.getOnlines()) {
			sumNode += n.getMemoryCapacity();
		}
		for (VirtualMachine vm : cfg.getAllVirtualMachines()) {
			sumVMs += vm.getMemoryConsumption();
		}
		return sumVMs / sumNode;
	}

	/**
	 * 
	 * @param cfg
	 * @return the load (base 1) of the configuration for a given dimension
	 */
	public static double getLoad(Configuration cfg, ResourceSpecification specs) {
		double capa = 0, used = 0;
		for (Node n : cfg.getOnlines()) {
			capa += specs.toCapacities().get(n);
		}
		for (VirtualMachine vm : cfg.getAllVirtualMachines()) {
			used += specs.toUses().get(vm);
		}
		return used / capa;
	}

	/**
	 * @param specs
	 * @param n
	 * @param vm
	 * @return the maximum number of vms the node can host
	 */
	public static double getMaxVMs(Map<String, ResourceSpecification> specs,
			Node n, VirtualMachine vm) {
		double maxVMs = Double.POSITIVE_INFINITY;
		for (ResourceSpecification s : specs.values()) {
			double resLimit = s.toCapacities().get(n) / s.toUses().get(vm);
			maxVMs = Math.max(maxVMs, resLimit);
		}
		return maxVMs;
	}
}

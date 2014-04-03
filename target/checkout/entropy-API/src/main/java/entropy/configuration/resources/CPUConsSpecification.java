package entropy.configuration.resources;

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * CPU resource based on {@link VirtualMachine.#getCPUConsumption()} and {@link
 * Node.#getCPUCapacity()}. Does not need to be parametered so a singleton is
 * available.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class CPUConsSpecification extends GetterBasedSpecification {

	public static final CPUConsSpecification INSTANCE = new CPUConsSpecification();

	public static final String TYPE = "CPU";

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(CPUConsSpecification.class);

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	int getUse(VirtualMachine vm) {
		return vm.getCPUConsumption();
	}

	@Override
	public int getCapacity(Node n) {
		return n.getCPUCapacity();
	}
}

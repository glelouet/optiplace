package entropy.configuration.resources;

import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/**
 * CPU resource based on {@link VirtualMachine.#getMemoryConsumption()} and
 * {@link Node.#getMemoryCapacity()}. Does not need to be parametered so a
 * singleton is available.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class MemConsSpecification extends GetterBasedSpecification {

	public static final MemConsSpecification INSTANCE = new MemConsSpecification();

	public static final String TYPE = "MEM";

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MemConsSpecification.class);

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	int getUse(VirtualMachine vm) {
		return vm.getMemoryConsumption();
	}

	@Override
	public int getCapacity(Node n) {
		return n.getMemoryCapacity();
	}
}

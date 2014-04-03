package fr.emn.optiplace.configuration.resources;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * CPU resource, using only the number of nodes
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class CPUCoresSpecification extends GetterBasedSpecification {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(CPUCoresSpecification.class);

	public static final CPUCoresSpecification INSTANCE = new CPUCoresSpecification();

	@Override
	public String getType() {
		return CPUConsSpecification.TYPE;
	}

	@Override
	public int getUse(VirtualMachine vm) {
		return vm.getNbOfCPUs();
	}

	@Override
	public int getCapacity(Node n) {
		return n.getNbOfCores();
	}
}

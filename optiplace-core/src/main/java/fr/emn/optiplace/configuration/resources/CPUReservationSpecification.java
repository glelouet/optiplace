package fr.emn.optiplace.configuration.resources;

import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 * 
 */
public class CPUReservationSpecification extends CPUConsSpecification {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(CPUReservationSpecification.class);

	public static final CPUReservationSpecification INSTANCE = new CPUReservationSpecification();

	@Override
	public int getUse(VirtualMachine vm) {
		return vm.getCPUMax();
	}
}

package entropy.configuration.resources;

import entropy.configuration.VirtualMachine;

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
	int getUse(VirtualMachine vm) {
		return vm.getCPUMax();
	}
}

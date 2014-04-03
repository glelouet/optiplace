package entropy.configuration.resources;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 * 
 */
public class MemReservationSpecification extends MemConsSpecification {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MemReservationSpecification.class);

	public static final MemReservationSpecification INSTANCE = new MemReservationSpecification();

	@Override
	int getUse(entropy.configuration.VirtualMachine vm) {
		return vm.getMemoryDemand();
	}
}

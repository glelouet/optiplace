package entropy.configuration;

import java.util.Comparator;

import entropy.configuration.VirtualMachine;

/**
 * comparator to sort vms per memory : desc or asc. static final objects are
 * created as {@link #DESC} and {@link ASC}
 * 
 * @author guigolum
 * 
 */
public class VMsPerMem implements Comparator<VirtualMachine> {

	@SuppressWarnings("unused")
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(VMsPerMem.class);

	private boolean desc = true;

	public VMsPerMem(boolean desc) {
		this.desc = desc;
	}

	@Override
	public int compare(VirtualMachine o1, VirtualMachine o2) {
		return (desc ? -1 : 1)
				* (o1.getMemoryConsumption() - o2.getMemoryConsumption());
	}

	public static final VMsPerMem DESC = new VMsPerMem(true);

	public static final VMsPerMem ASC = new VMsPerMem(false);

}

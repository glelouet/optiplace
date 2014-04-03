package entropy.configuration;

import java.util.Comparator;

/**
 * compare two vms by their CPU consumption
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class VMsPerCPU implements Comparator<VirtualMachine> {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(VMsPerCPU.class);

	private boolean desc = true;

	private VMsPerCPU(boolean desc) {
		this.desc = desc;
	}

	@Override
	public int compare(VirtualMachine o1, VirtualMachine o2) {
		return (desc ? -1 : 1)
				* (o1.getCPUConsumption() - o2.getCPUConsumption());
	}

	public static final VMsPerCPU DESC = new VMsPerCPU(true);

	public static final VMsPerCPU ASC = new VMsPerCPU(false);
}

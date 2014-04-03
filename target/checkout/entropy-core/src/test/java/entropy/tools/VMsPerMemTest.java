package entropy.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.testng.annotations.Test;

import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VMsPerMem;
import entropy.configuration.VirtualMachine;

public class VMsPerMemTest {

	@SuppressWarnings("unused")
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(VMsPerMemTest.class);

	@Test
	public void testSimpleORdering() {
		VirtualMachine[] vms = new VirtualMachine[]{
				new SimpleVirtualMachine("vm1", 1, 1000, 1000),
				new SimpleVirtualMachine("vm2", 1, 1000, 300),
				new SimpleVirtualMachine("vm3", 1, 1000, 2000)};
		List<VirtualMachine> sorted = new ArrayList<VirtualMachine>(
				Arrays.asList(vms));
		Collections.sort(sorted, VMsPerMem.DESC);
		Assert.assertEquals(
				Arrays.asList(new VirtualMachine[]{vms[2], vms[0], vms[1]}),
				sorted);
	}
}

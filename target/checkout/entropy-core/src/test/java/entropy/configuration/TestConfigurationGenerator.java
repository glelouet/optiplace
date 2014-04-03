package entropy.configuration;

import java.util.ArrayList;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestConfigurationGenerator {

	@SuppressWarnings("unused")
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(TestConfigurationGenerator.class);

	@Test
	public void simpleTest() {
		Configuration cfg = new ConfigurationGenerator()
				.addNodes(2, 1000, 1024).addRunnings(4, 100, 200).getTarget();
		Assert.assertEquals(cfg.getAllNodes().size(), 2);
		Assert.assertEquals(cfg.getAllVirtualMachines().size(), 8);
	}

	@Test
	public void testSimpleCluster() {
		ConfigurationGenerator gen = new ConfigurationGenerator();
		gen.addCluster(
				new Node[]{new SimpleNode(null, 2, 1000, 1024)},
				new String[][]{{"node1", "node2"}},
				new VirtualMachine[]{new SimpleVirtualMachine(null, 1, 100, 100)},
				new String[][][][]{{{{"vm_n1_1", "vm_n1_2", "vm_n1_3"}},
						{{"vm_n2_1", "vm_n2_2"}}}});
		Configuration cfg = gen.getTarget();
		Assert.assertEquals(cfg.getAllNodes().size(), 2);
		Assert.assertEquals(cfg.getAllVirtualMachines().size(), 5);
	}

	@Test
	public void testBunchOfVMs() {
		int size = 90;
		double dst = 0.5;
		ArrayList<VirtualMachine> test = ConfigurationGenerator.bunchOfVMs(
				"vm", size, 1024, 1000, dst);
		Assert.assertEquals(test.size(), size);
		Assert.assertEquals(test.get(0).getCPUDemand(), 500);
		Assert.assertEquals(test.get(size - 1).getCPUDemand(), 1500);
		for (VirtualMachine vm : test) {
			Assert.assertEquals(vm.getMemoryConsumption(), 1024);
			Assert.assertEquals(vm.getName().length(), test.get(0).getName()
					.length());
			// System.err.println(vm);
		}
	}
}

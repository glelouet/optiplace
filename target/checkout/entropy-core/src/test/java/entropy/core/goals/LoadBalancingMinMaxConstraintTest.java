package entropy.core.goals;

import org.testng.Assert;
import org.testng.annotations.DataProvider;

import choco.kernel.common.logging.Verbosity;
import entropy.SolvingProcess;
import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.SimpleConfiguration;
import entropy.configuration.SimpleNode;
import entropy.configuration.SimpleVirtualMachine;
import entropy.configuration.VirtualMachine;
import entropy.solver.PlanException;

public class LoadBalancingMinMaxConstraintTest {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LoadBalancingMinMaxConstraintTest.class);

	public static int NODECPUCAPA = 1000;
	public static int NODERAMCAPA = 1000;

	/**
	 * generate a simple config of nodes and waiting VMs.
	 * 
	 * @param nbNodes
	 *            number of nodes
	 * @param nbVMPerNode
	 *            number of VMs to generate, per node
	 * @param vmPcCPU
	 *            percentage of CPU used on a node, per VM
	 * @param vmPcRAM
	 *            percentage of RAM used on a node, per VM
	 * @return an homogeneous config fulfilling those requirements.<br />
	 *         The nodes have {@value #NODECPUCAPA} CPU capacity and
	 *         {@value #NODECPUCAPA} ram capacity, 1 CPU core.
	 */
	public static SimpleConfiguration makeSimpleConfiguration(int nbNodes,
			int nbVMPerNode, int vmPcCPU, int vmPcRAM) {
		SimpleConfiguration ret = new SimpleConfiguration();
		for (int i = 0; i < nbNodes; i++) {
			SimpleNode n = new SimpleNode("n" + i, 1, NODECPUCAPA, NODERAMCAPA);
			ret.addOnline(n);
		}
		int vmLoadCPU = vmPcCPU * 10;
		int vmLoadRAM = vmPcRAM * 10;
		for (int i = 0; i < nbVMPerNode * nbNodes; i++) {
			SimpleVirtualMachine svm = new SimpleVirtualMachine("VM" + i, 1,
					vmLoadCPU, vmLoadRAM);
			ret.addWaiting(svm);
		}
		return ret;
	}

	// test is too long
	// @Test(groups = "unit", dataProvider = "simpleExecData")
	public void simpleExec(int nbNodes, int nbVmsPerNode, int pcCPUPerVM,
			int pcRAMPerVM) throws PlanException {
		Configuration cfg = makeSimpleConfiguration(nbNodes, nbVmsPerNode,
				pcCPUPerVM, pcRAMPerVM);

		SolvingProcess sp = generateClassSolver();
		sp.getCenter().setSource(cfg);
		Configuration dest = sp.getTarget().getDestination();
		// each node should have pcCPUPerVM*nbVmsPerNode % load
		logger.debug("objective load is " + nbVmsPerNode * pcCPUPerVM * 10
				+ "out of " + NODECPUCAPA);
		for (Node n : dest.getAllNodes()) {
			int load = 0;
			for (VirtualMachine vm : dest.getRunnings(n)) {
				load += vm.getCPUDemand();
			}
			Assert.assertEquals(load, nbVmsPerNode * pcCPUPerVM * 10,
					"end configuration : " + dest);
		}
	}

	/**
	 * @return a list of [nodes number, VMs per node number, percentage CPU per
	 *         VM, percentage RAM per VM]
	 */
	@DataProvider(name = "simpleExecData")
	public Object[][] simpleExecData() {
		return new Object[][]{{2, 2, 10, 10}, {10, 3, 20, 10}};
	}

	/** generate a correct solver for the tests methods */
	public static SolvingProcess generateClassSolver() {
		SolvingProcess ret = new SolvingProcess();
		ret.getStrat().setChocoVerbosity(Verbosity.SOLUTION);
		ret.getStrat().setGoal(LoadBalancingMinMaxCost.INSTANCE);
		return ret;
	}

}

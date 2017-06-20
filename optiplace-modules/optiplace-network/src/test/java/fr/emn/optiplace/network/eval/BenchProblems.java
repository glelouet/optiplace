package fr.emn.optiplace.network.eval;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.VM;

public class BenchProblems {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BenchProblems.class);


	public static void main(String[] args) {
		int nbredo = 20;
		int nbnodes = 50;
		int nbVmPerComputer = 10;
		Configuration c = new Configuration("mem");
		Computer[] nodes = new Computer[nbnodes];
		VM[][] vms = new VM[nodes.length][];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = c.addComputer("n_" + i, nbVmPerComputer * 2 + i);
			vms[i] = new VM[nbVmPerComputer];
			for (int j = 0; j < vms[i].length; j++) {
				vms[i][j] = c.addVM("vm_" + i + "_" + j, null, 2);
			}
		}
		System.err.println("best time evaluation with " + c.nbComputers() + " nodes and " + c.nbVMs() + "VMs");
		// long timeNude = getMinTime(new Optiplace(c), nbredo);
		// System.err.println("nude time is " + timeNude);
		NetworkViewStreamer nvs = new NetworkViewStreamer(c, 100, 600, 2, 4, 2, 5);
		// Chatterbox.showDecisions(nvs);
		// Chatterbox.showContradiction(nvs);
		nvs.stream().forEach(nv -> {
			System.err.println("eval of view " + nv);
			IOptiplace opl = new Optiplace(c).with(nv);
			System.err.println(" view time is " + getMinTime(opl, nbredo));
		});
	}

	public static long getMinTime(IOptiplace pb, int redo) {
		System.err.println(" besttime(ms)\t#iterations");
		long ret = Long.MAX_VALUE;
		int iterations = 0;
		for (int remaining = redo; remaining > 0; remaining--) {
			DeducedTarget target = pb.solve();
			if (target.getDestination() == null) {
				return -1;
			}
			long time = target.getTotalTime();
			if (time < ret) {
				System.err.println(" " + time / 1000000 + "\t" + iterations);
				ret = time;
				remaining = redo + iterations / 4;
			}
			iterations++;
		}
		return ret;
	}
}

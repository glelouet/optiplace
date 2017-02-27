package fr.emn.optiplace.evaluations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Near;
import fr.emn.optiplace.ha.rules.SiteOn;
import fr.emn.optiplace.hostcost.HostCostView;

public class EvalSites {

	public static void main(String[] args) {
		// first create the physical infrastructure. it does not change through
		// tests.
		int nbNodes = 5;
		Configuration physical = new Configuration("mem");
		// 4 externs, each on its site
		Extern[] externs = new Extern[4];
		for (int ie = 0; ie < externs.length; ie++) {
			externs[ie] = physical.addExtern("e" + ie, 2000);
			physical.addSite("site" + ie, externs[ie]);
		}
		// 5 nodes, each on the "local" site
		int nodeMem = 5000;
		Node[] nodes = new Node[nbNodes];
		for (int in = 0; in < nodes.length; in++) {
			nodes[in] = physical.addOnline("n" + in, nodeMem);
			physical.addSite("local", nodes[in]);
		}
		/////////////////

		int ftpMem = 1000, dispatcherMem = 500, webserviceMem = 1000;
		int maxSize = Math.floorDiv(nodeMem * nbNodes, dispatcherMem * 2 + webserviceMem);
		// for each size of problem, we launch several tests we collect the results
		// of those tests in this list.
		@SuppressWarnings("unchecked")
		List<DeducedTarget>[] evals = new List[maxSize];
		Arrays.setAll(evals, i -> new ArrayList<>());
		// we redo the same benches 33 times
		int nbiterations = 33;

		for (int iteration = 0; iteration < nbiterations; iteration++) {
			for (int size = 1; size <= maxSize; size++) {
				Configuration src = physical.clone();
				HAView ha = new HAView();
				HostCostView hc = new HostCostView();
				hc.getCostData().setHostCost("site4", 3);
				hc.getCostData().addHostFilter(".*", 1);
				VM[][] clusters = new VM[size][];
				// for each cluster (1 to size) of webapp+trusty app
				for (int clusteri = 1; clusteri <= size; clusteri++) {
					VM[] vms = new VM[9];
					clusters[clusteri - 1] = vms;
					vms[0] = src.addVM("dispatcher1_" + clusteri, null, dispatcherMem);
					vms[1] = src.addVM("dispatcher2_" + clusteri, null, dispatcherMem);
					// ha.addRule(new Spread(vms[0], vms[1]));
					vms[2] = src.addVM("webservice_" + clusteri, null, webserviceMem);
					ha.addRule(new SiteOn(src.addSite("local", (VMHoster[]) null), vms[0], vms[1], vms[2]));
					for (int i = 0; i < 3; i++) {
						vms[3 + i * 2] = src.addVM("ftp_" + clusteri + "_" + i + "a", null, ftpMem);
						vms[3 + i * 2 + 1] = src.addVM("ftp_" + clusteri + "_" + i + "b", null, ftpMem);
						ha.addRule(new Near(vms[3 + i * 2], vms[4 + i * 2]));
					}
					// ha.addRule(new Far(vms[3], vms[5], vms[7]));
				}
				DeducedTarget res = new Optiplace(src).with(ha)
						// .with(hc).withGoal("hostcost")
						.solve();
				if(res.getDestination()!=null) {
					evals[size - 1].add(res);
				} else {
					System.err.println("error : can't solve");
					System.err.println(src);
					System.err.println(ha.getInternalRules());
					System.exit(42);
				}

			}
			System.err.println("iteration " + iteration + " performed");
		}

		for (int size = 1; size <= maxSize; size++) {
			List<DeducedTarget> list = evals[size - 1];
			boolean hasSolution = list.stream().filter(dt -> dt.getDestination() != null).findAny().isPresent();
			long lowestSolutionTime = hasSolution
					? list.stream().filter(dt -> dt.getDestination() != null).mapToLong(DeducedTarget::getTotalTime).min()
							.getAsLong()
							: 0;
							System.err.println("" + size + "\t" + hasSolution + "\t" + lowestSolutionTime);
		}
	}

}

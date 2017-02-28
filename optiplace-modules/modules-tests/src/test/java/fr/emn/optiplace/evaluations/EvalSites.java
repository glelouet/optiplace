package fr.emn.optiplace.evaluations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.ha.rules.Far;
import fr.emn.optiplace.ha.rules.Near;
import fr.emn.optiplace.ha.rules.SiteOn;
import fr.emn.optiplace.ha.rules.Spread;
import fr.emn.optiplace.hostcost.HostCostView;
import fr.emn.optiplace.view.View;

public class EvalSites {

	///////////////////////////////////////////////////////////////
	// Eval parameters

	/**
	 * number of physical nodes in the local site.
	 *
	 */
	static int nbNodes = 5;

	/** mem of local nodes */
	static int nodeMem = 5000;

	/**
	 * mem limit of externs
	 */
	static int extMem = 2000;

	/** mem of ftp VM */
	static int ftpMem = 1000;
	/**
	 * mem of dispatcher vm
	 */
	static int dispatcherMem = 500;
	/**
	 * mem of webservice VM
	 */
	static int webserviceMem = 1000;
	/**
	 * maximum size of the problem, with regard to memory constraints
	 */
	static int maxSize = Math.floorDiv(nodeMem * nbNodes, dispatcherMem * 2 + webserviceMem);

	///////////////////////////////////////////////////////////////////

	/**
	 * the physical infrastructure, only contains nodes and externs. It is cloned
	 * for each test.
	 */
	static Configuration physical;

	/**
	 * create the physical infrastructure. it does not change through tests.
	 */
	static void doPhysical() {
		physical = new Configuration("mem");
		// 4 externs, each on its site
		Extern[] externs = new Extern[4];
		for (int ie = 0; ie < externs.length; ie++) {
			externs[ie] = physical.addExtern("e" + ie, extMem);
			physical.addSite("site" + ie, externs[ie]);
		}
		// 5 nodes, each on the "local" site
		Node[] nodes = new Node[nbNodes];
		for (int in = 0; in < nodes.length; in++) {
			nodes[in] = physical.addOnline("n" + in, nodeMem);
			physical.addSite("local", nodes[in]);
		}
	}

	static Optiplace makeProblem(int size, boolean spread, boolean far, boolean near, boolean hostcost) {
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
			if (spread) {
				ha.addRule(new Spread(vms[0], vms[1]));
			}
			vms[2] = src.addVM("webservice_" + clusteri, null, webserviceMem);
			ha.addRule(new SiteOn(src.addSite("local", (VMHoster[]) null), vms[0], vms[1], vms[2]));
			for (int i = 0; i < 3; i++) {
				vms[3 + i * 2] = src.addVM("ftp_" + clusteri + "_" + i + "a", null, ftpMem);
				vms[3 + i * 2 + 1] = src.addVM("ftp_" + clusteri + "_" + i + "b", null, ftpMem);
				if (near) {
					ha.addRule(new Near(vms[3 + i * 2], vms[4 + i * 2]));
				}
			}
			if (far) {
				ha.addRule(new Far(vms[3], vms[5], vms[7]));
			}
		}
		Optiplace ret = new Optiplace(src);
		if (spread || far || near) {
			ret.with(ha);
		}
		if (hostcost) {
			ret.with(hc).withGoal("hostcost");
		}
		return ret;
	}

	public static final ToLongFunction<Stream<DeducedTarget>> minTotalTime = new ToLongFunction<Stream<DeducedTarget>>() {
		@Override
		public long applyAsLong(Stream<DeducedTarget> value) {
			return value.mapToLong(DeducedTarget::getTotalTime).min().getAsLong();
		}

		@Override
		public String toString() {
			return "minTotalTime";
		}
	};

	public static final ToLongFunction<Stream<DeducedTarget>> medianTotalTime = new ToLongFunction<Stream<DeducedTarget>>() {

		@Override
		public long applyAsLong(Stream<DeducedTarget> value) {
			long[] arr = value.mapToLong(DeducedTarget::getTotalTime).sorted().toArray();
			return arr[arr.length / 2];
		}

		@Override
		public String toString() {
			return "medianTotalTime";
		}
	};

	public static final ToLongFunction<Stream<DeducedTarget>> minBactracks = new ToLongFunction<Stream<DeducedTarget>>() {

		@Override
		public long applyAsLong(Stream<DeducedTarget> value) {
			return value.mapToLong(DeducedTarget::getSearchBacktracks).min().getAsLong();
		}

		@Override
		public String toString() {
			return "minBacktracks";
		}
	};

	@SuppressWarnings("unchecked")
	static ToLongFunction<Stream<DeducedTarget>>[] evalMetrics = new ToLongFunction[] { minTotalTime, medianTotalTime,
			minBactracks };

	public static void main(String[] args) {
		doPhysical();
		// 4 externs, each on its site
		Extern[] externs = new Extern[4];
		for (int ie = 0; ie < externs.length; ie++) {
			externs[ie] = physical.addExtern("e" + ie, 2000);
			physical.addSite("site" + ie, externs[ie]);
		}
		// 5 nodes, each on the "local" site
		Node[] nodes = new Node[nbNodes];
		for (int in = 0; in < nodes.length; in++) {
			nodes[in] = physical.addOnline("n" + in, nodeMem);
			physical.addSite("local", nodes[in]);
		}
		/////////////////
		// for each size of problem, we launch several tests we collect the results
		// of those tests in this list.
		@SuppressWarnings("unchecked")
		List<DeducedTarget>[] evals = new List[maxSize];
		Arrays.setAll(evals, i -> new ArrayList<>());
		// we redo the same benches 33 times
		int nbiterations = 33;

		for (int iteration = 0; iteration < nbiterations; iteration++) {
			for (int size = 1; size <= maxSize; size++) {
				Optiplace opl = makeProblem(size, true, false, true, false);
				DeducedTarget res = opl.solve();
				if (res.getDestination() != null) {
					evals[size - 1].add(res);
				} else {
					System.err.println("error : can't solve");
					System.err.println(opl.source());
					for (View v : opl.views()) {
						System.err.println(v.rulesStream());
					}
					System.exit(42);
				}

			}
			System.err.println("iteration " + iteration + " performed");
		}

		System.err.print("size");
		for (ToLongFunction<Stream<DeducedTarget>> e : evalMetrics) {
			System.err.print("\t" + e);
		}
		System.err.println();
		for (int size = 1; size <= maxSize; size++) {

			System.err.print(size);
			List<DeducedTarget> list = evals[size - 1];
			for (ToLongFunction<Stream<DeducedTarget>> e : evalMetrics) {
				System.err.print("\t" + e.applyAsLong(list.stream()));
			}
			System.err.println();
		}
	}

}

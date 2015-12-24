package fr.emn.optiplace.network.benches;

import java.util.function.Supplier;
import java.util.stream.Stream;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.network.NetworkData;
import fr.emn.optiplace.network.NetworkView;

public class BenchSmallProblems {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(BenchSmallProblems.class);

	/**
	 * class containing the data of a bench : the configuration, the network data,
	 * 
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
	 *
	 */
	public static class Bench {

		Configuration cfg = null;
		NetworkData data = null;
		long findNoView = Long.MAX_VALUE;
		long findWithView = Long.MAX_VALUE;

		public Bench() {
		}

		public Bench(Configuration cfg, NetworkData data) {
			this.cfg = cfg;
			this.data = data;
		}

		@Override
		public String toString() {
			return "" + cfg + "\n" + data + "\n noview=" + findNoView + " withview=" + findWithView;
		}

		public void run(int nbIterations) {
			findNoView = Long.MAX_VALUE;
			findWithView = Long.MAX_VALUE;
			NetworkView v = null;
			if (data != null) {
				v = new NetworkView();
				v.setData(data);
			}
			for (int i = 0; i < nbIterations; i++) {
				for (boolean view : new boolean[] { true, false }) {
					Optiplace o = new Optiplace(cfg);
					if (view)
						o.addView(v);
					long time = o.solve().getSearchTime();
					if (view && data != null)
						findWithView = Math.min(findWithView, time);
					else
						findNoView = Math.min(findNoView, time);
				}
			}
		}
	}

	/**
	 * performs a solve ten time and store the minimum time to find a solution
	 * with or without a network view
	 * 
	 * @param cfg
	 *          the configuration to find a solution
	 * @param data
	 *          the network data to consider
	 * @return the smallest time (in ms) to find the first solution
	 */
	public static Bench bench(Configuration cfg, NetworkData data) {
		Bench ret = new Bench();
		ret.data = data;
		ret.cfg = cfg;
		NetworkView v = new NetworkView();
		v.setData(data);
		for (int i = 0; i < 10; i++) {
			for (boolean view : new boolean[] { true, false }) {
				Optiplace o = new Optiplace(cfg);
				if (view)
					o.addView(v);
				long time = o.solve().getSearchTime();
				if (view)
					ret.findWithView = Math.min(ret.findWithView, time);
				else
					ret.findNoView = Math.min(ret.findNoView, time);
			}
		}
		return ret;
	}

	public static void main(String[] args) {
		Configuration cfg = new Configuration("mem");
		Node n0 = cfg.addOnline("n0", 100);
		Node n1 = cfg.addOnline("n1", 100);
		Node n2 = cfg.addOnline("n2", 200);
		VM v0 = cfg.addVM("v0", n0, 50);
		Stream.generate(new Supplier<String>() {

			@Override
			public String get() {
				return null;
			}
		});
	}
}

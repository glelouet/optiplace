
package fr.emn.optiplace.eval;

import java.util.stream.Stream;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.view.View;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class ViewEvaluator implements IViewEvaluator {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ViewEvaluator.class);

	public static <T extends View> Stream<ViewImpact<T>> eval(Stream<? extends IConfiguration> cex, ViewStreamer<T> vex,
	    int redo) {
		return cex.filter(Optiplace::hasSolution).map(c -> {
			ViewImpact<T> impact = new ViewImpact<>(c);
			for (int i = 0; i < redo; i++) {
				impact.addNudeTime(new Optiplace(c).solve().getSearchTime());
			}
			vex.explore(c).forEach(v -> {
				if (v == null) {
					throw new NullPointerException();
				}
				long viewtime = Long.MAX_VALUE;
				for (int i = 0; i < redo; i++) {
					DeducedTarget t = new Optiplace(c).with(v).solve();
					if (t.getDestination() == null) {
						if (i != 0) {
							logger.error("error : sequential calls to solve() return non-null then null results for config " + c
		              + " and view " + v);
						}
						break;
					}
					viewtime = Math.min(viewtime, t.getSearchTime());
					if (viewtime < impact.worseViewTime_ns) {
						break;
					}
				}
				impact.addView(viewtime, v);
			});
			return impact;
		}).filter(vi -> vi.worseView != null && vi.configuration != null);
	}

	/**
	 *
	 * @param redo
	 *          number of times we solve a problem to have a good minimum solving
	 *          time.
	 * @param maxCfg
	 *          number of different configuration we allow.
	 */
	public ViewEvaluator(int redo, int maxCfg) {
		this.redo = redo;
		this.maxCfg = maxCfg;
	}

	public int maxCfg = -1;
	public int redo = 5;
	boolean burnt = false;

	@Override
	public <T extends View> void eval(Stream<IConfiguration> cex, ViewStreamer<T> vex, ViewImpactAggregator<T> aggreg) {
		burnt = false;
		Stream<? extends IConfiguration> configs = cex.filter(Optiplace::hasSolution);
		if (maxCfg > -1) {
			configs = configs.limit(maxCfg);
		}
		configs.forEach(c -> {
			ViewImpact<T> impact = new ViewImpact<>(c);
			long startTime = System.nanoTime();
			do {
				for (int i = 0; i < (burnt ? redo : redo * 100); i++) {
					if (impact.addNudeTime(new Optiplace(c).solve().getSearchTime())) {
						i--;
					}
				}
			}
			while (System.nanoTime() - startTime < 10000000000l);
			// we can't accept a configuration if another with same weight has already
		  // got more percent increase.
		  // because we want the worst %increase for a given configuration weight
			long mintime_ns = (aggreg.increase(c) + 100) * impact.nudeTime_ns / 100;

			System.err.println("nude time is " + impact.nudeTime_ns + ", minTime is " + mintime_ns);
			vex.explore(c).forEach(v -> {
				long viewtime = Long.MAX_VALUE;
				for (int i = 0; i < redo; i++) {
					DeducedTarget t = new Optiplace(c).with(v).solve();
					if (t.getDestination() == null) {
						if (i != 0) {
							logger.error("error : sequential calls to solve() return non-null then null results for config " + c
		              + " and view " + v);
						}
						viewtime = 0;
						break;
					}
					long searchTime = t.getSearchTime();
					if (searchTime < mintime_ns) {
						break;
					}
					viewtime = Math.min(viewtime, searchTime);
				}
				if (viewtime > mintime_ns) {
					impact.addView(viewtime, v);
				}
			});
			if (impact.worseView != null) {
				System.err.println("adding view");
				aggreg.add(impact);
			}
			burnt = true;
		});
	}
}

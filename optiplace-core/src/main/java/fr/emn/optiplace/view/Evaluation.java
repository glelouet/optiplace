package fr.emn.optiplace.view;

import java.util.stream.Stream;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class Evaluation {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Evaluation.class);

	public static class ViewImpact<T extends View> {

		public ViewImpact(IConfiguration cfg) {
			configuration = cfg;
		}

		public final IConfiguration configuration;
		public long minTimeNude = Long.MAX_VALUE;
		public T worseView = null;
		public long worseViewTime = Long.MAX_VALUE;

		public void addNudeTime(long time) {
			minTimeNude = Math.min(minTimeNude, time);
		}

		public void addView(long time, T view) {
			if (worseView == null || time < worseViewTime) {
				worseViewTime = time;
				worseView = view;
			}
		}
	}

	public static <T extends View> Stream<ViewImpact<T>> eval(Stream<? extends IConfiguration> cex, ViewStreamer<T> vex) {
		return cex.filter(Optiplace::hasSolution).map(c -> {
			ViewImpact<T> impact = new ViewImpact<>(c);
			for (int i = 0; i < 10; i++) {
				impact.addNudeTime(new Optiplace(c).solve().getFirstSolTime());
			}
			vex.explore(c).forEach(v->{
				long viewtime = Long.MAX_VALUE;
				for (int i = 0; i < 10; i++) {
					DeducedTarget t = new Optiplace(c).with(v).solve();
					if (t.getDestination() == null) {
						if (i != 0)
							logger.error("error : sequential calls to solve() return non-null then null results for config " + c
									+ " and view " + v);
						break;
					}
					viewtime = Math.min(viewtime, t.getFirstSolTime());
				}
				impact.addView(viewtime, v);
			});
			return impact;
		});
	}
}

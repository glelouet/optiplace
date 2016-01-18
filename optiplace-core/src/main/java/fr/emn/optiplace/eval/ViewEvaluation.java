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
public class ViewEvaluation {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ViewEvaluation.class);

	public static <T extends View> Stream<ViewImpact<T>> eval(Stream<? extends IConfiguration> cex, ViewStreamer<T> vex,
	    int redo) {
		return cex.filter(Optiplace::hasSolution).map(c -> {
			ViewImpact<T> impact = new ViewImpact<>(c);
			for (int i = 0; i < redo; i++) {
				impact.addNudeTime(new Optiplace(c).solve().getSearchTime());
			}
			vex.explore(c).forEach(v->{
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
					if (viewtime < impact.worseViewTime) {
						break;
					}
				}
				impact.addView(viewtime, v);
			});
			return impact;
		}).filter(vi -> vi.worseView != null && vi.configuration != null);
	}
}

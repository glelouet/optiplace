
package fr.emn.optiplace.eval;

import java.util.stream.Stream;

import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.view.View;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class ViewEvaluator {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ViewEvaluator.class);

	/**
	 * functional interface to evaluate a problem if this problem's best
	 * evaluation is worse than any already evaluated, given an already known
	 * worst solution for this class of problem
	 */
	public static interface ProblemEvaluatorIfWorse {
		/**
		 * 
		 * @param target
		 *          the problem to evaluate
		 * @param minValue
		 *          the worse value already found for this kind of problem; we
		 *          return 0 if the best evaluation of this problem is worse than
		 *          this value
		 * @return -1 if this problem can't be solved, 0 if the problem's best
		 *         solution is better than minValue, or an evaluation of the problem
		 *         otherwise.
		 */
		long evalBestIfWorse(IOptiplace target, long minValue);

	}

	public static <T extends View> void evaluate(Stream<IConfiguration> cex, ViewStreamer<T> vex,
			ProblemEvaluatorIfWorse eval, ViewImpactAggregator<T> aggreg) {
		final long timeInit = System.currentTimeMillis();
		// we can't pass non-final elements to a closure so we use an array of int.
		// [ nb cfg tested, last print time, last nb config]
		final long[] nbCfg = new long[] { 0, timeInit, 0 };
		cex.forEach(c -> {
			ViewCfgEval<T> impact = new ViewCfgEval<>(c);
			impact.nudeEval = eval.evalBestIfWorse(new Optiplace(c), 0);
			// we can't accept a configuration if another with same weight has already
			// got more percent increase.
			// because we want the worst %increase for a given configuration weight
			impact.worseViewEval = aggreg.getPercent(c) * impact.nudeEval / 100;

			vex.explore(c).forEach(v -> {
				impact.nbViewsTested++;
				IOptiplace opl = new Optiplace(c).with(v);
				long viewEval = eval.evalBestIfWorse(opl, impact.worseViewEval);
				if (viewEval > 0)
					impact.addView(viewEval, v);
			});
			if (impact.worseView != null) {
				aggreg.add(impact);
			}
			nbCfg[0]++;
			long time = System.currentTimeMillis();
			long deltat = time - nbCfg[1];
			if (deltat > 10000) {
			long deltac = nbCfg[0] - nbCfg[2];
				System.err.println(" " + deltac + " cfg in " + deltat / 1000 + "s : cfg/s=" + (1000.0 * deltac / deltat)
						+ " ; total #cfg=" + nbCfg[0]);
				aggreg.dataOrdered(
						(i, w) -> System.err.println("weight=" + w + " pct=" + i.pctView() + " #views=" + i.nbViewsTested));
				nbCfg[1] = time;
				nbCfg[2] = nbCfg[0];
			}
		});
	}

	/**
	 * make a time evaluator on a problem
	 * 
	 * @param redo
	 *          the minimum number of times we find solutions worse, in a row,
	 *          than the best solution already found to stop searching
	 * @return the best time we took to build and solve the given problem.
	 */
	public static ProblemEvaluatorIfWorse makeTimeEvaluator(int redo) {
		return (o, min) -> {
			long minTime = Long.MAX_VALUE;
			int nbEval = 0;
			for (int remaining = redo; remaining > 0; remaining--) {
				nbEval++;
				long time = o.solve().getSearchTime();
				if (time < 1)
					return -1;
				if (time < minTime) {
					if (time < min)
						return 0;
					minTime = time;
					remaining = redo + nbEval;
				}
			}
			return minTime;
		};
	}

	public static final ProblemEvaluatorIfWorse OPEN_NODES = (o, v) -> {
		long ret = o.solve().getSearchNodes();
		if (ret == -1)
			return -1;
		return ret;
	};
}

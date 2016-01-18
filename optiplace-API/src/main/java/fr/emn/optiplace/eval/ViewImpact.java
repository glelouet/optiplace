package fr.emn.optiplace.eval;

import java.util.function.Function;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.view.View;

/**
 * contain a Configuration solve time, and keep the view with the worst solve
 * time
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 * @param <T>
 *          type of the view.
 */
public class ViewImpact<T extends View> {

	public ViewImpact(IConfiguration cfg) {
		configuration = cfg;
	}

	public final IConfiguration configuration;
	public long minTimeNude = Long.MAX_VALUE;
	public long viewsTested = 0;
	public T worseView = null;
	public long worseViewTime = 0;

	public void addNudeTime(long time) {
		minTimeNude = Math.min(minTimeNude, time);
	}

	public void addView(long time, T view) {
		if (worseView == null || time > worseViewTime) {
			worseViewTime = time;
			worseView = view;
		}
		viewsTested++;
	}

	@Override
	public String toString() {
		return "nude time is " + minTimeNude + " for config \n " + configuration + "\nand worse time is " + worseViewTime
		    + " with view\n " + worseView;
	}

	/**
	 *
	 * @param size
	 *          convert the configuration to its size
	 * @param separator
	 *          separates the size, and converted values.
	 * @param conversions
	 *          methods to extract deduced values from this.
	 * @return a new String built with the given specifications.
	 */
	@SuppressWarnings("unchecked")
	public String printValue(String separator,
	    Function<ViewImpact<T>, Object>... conversions) {
		StringBuilder sb = null;
		for (Function<ViewImpact<T>, Object> f : conversions) {
			if (sb != null) {
				sb.append(separator);
			} else {
				sb = new StringBuilder();
			}
			sb.append(f.apply(this));
		}
		return sb.toString();
	}
}
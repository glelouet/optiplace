package fr.emn.optiplace.eval;

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
	public long nudeTime_ns = Long.MAX_VALUE;
	public T worseView = null;
	public long worseViewTime_ns = 0;

	public boolean addNudeTime(long time) {
		boolean ret = time < nudeTime_ns;
		if (ret) {
			nudeTime_ns = time;
		}
		return ret;
	}

	public void addView(long time, T view) {
		if (worseView == null || time > worseViewTime_ns) {
			worseViewTime_ns = time;
			worseView = view;
			System.err.println("new worse view time " + time + " , nude time is " + nudeTime_ns);
		}
	}

	@Override
	public String toString() {
		return "nude time is " + nudeTime_ns + " for config \n " + configuration + "\nand worse time is " + worseViewTime_ns
		    + " with view\n " + worseView;
	}

	public int pctIncrease() {
		return (int) ((worseViewTime_ns - nudeTime_ns) * 100 / nudeTime_ns);
	}
}
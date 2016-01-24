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
public class WorseViewEvaluation<T extends View> {

	public WorseViewEvaluation(IConfiguration cfg) {
		configuration = cfg;
	}

	public final IConfiguration configuration;
	public long nudeEval = Long.MAX_VALUE;
	public T worseView = null;
	public long worseViewEval = 0;
	public long nbViewsTested = 0;

	public boolean addNudeEval(long eval) {
		boolean ret = eval < nudeEval;
		if (ret) {
			nudeEval = eval;
		}
		return ret;
	}

	public boolean addView(long viewBestEval, T view) {
		if (worseView == null || viewBestEval > worseViewEval) {
			worseViewEval = viewBestEval;
			worseView = view;
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "nude time is " + nudeEval + " for config \n " + configuration + "\nand worse time is " + worseViewEval
		    + " with view\n " + worseView;
	}

	public int pctIncrease() {
		return (int) ((worseViewEval - nudeEval) * 100 / nudeEval);
	}
}
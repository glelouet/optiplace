
package fr.emn.optiplace.network.eval;

import fr.emn.optiplace.eval.ConfigurationStreamer;
import fr.emn.optiplace.eval.ViewEvaluation;
import fr.emn.optiplace.eval.ViewImpact;
import fr.emn.optiplace.network.NetworkView;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class NetworkViewEvaluation {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkViewEvaluation.class);

	public static void main(String[] args) {
		System.err.println("nude_ns\tview_ns\ttests_#\tincr_%\telements_#\tplacement_#");
		ViewEvaluation
		    .eval(
		        ConfigurationStreamer.streamConfigurations(5, -1, "mem", c -> c.nbNodes() * 5, 50, c -> c.nbNodes() > 2,
		            c -> c.nbVMs() > 2),
		        c -> new SelfMadeStreamer(c, 2 * (c.nbNodes() + c.nbExterns()), 4 * c.nbNodes(), 3, 4, 4).stream(), 10)
		    .limit(5000).forEach(vi -> acceptViewImpact(vi));
	}

	public static void acceptViewImpact(ViewImpact<NetworkView> vi) {
		int nbHosts = vi.configuration.nbNodes() + vi.configuration.nbExterns();
		int nbVMs = vi.configuration.nbVMs();

		System.out.println(vi.minTimeNude + "\t" + vi.worseViewTime + "\t" + vi.viewsTested + "\t"
		    + (100 * vi.worseViewTime / vi.minTimeNude - 100) + "\t" + (nbHosts + nbVMs) + "\t" + Math.pow(nbHosts, nbVMs));
	}

}

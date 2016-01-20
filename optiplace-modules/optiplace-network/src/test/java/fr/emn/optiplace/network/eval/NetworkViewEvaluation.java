
package fr.emn.optiplace.network.eval;

import java.util.stream.Stream;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.eval.ConfigurationStreamer;
import fr.emn.optiplace.eval.ViewEvaluator;
import fr.emn.optiplace.eval.ViewImpact;
import fr.emn.optiplace.eval.ViewImpactAggregator;
import fr.emn.optiplace.eval.ViewStreamer;
import fr.emn.optiplace.network.NetworkView;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class NetworkViewEvaluation {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkViewEvaluation.class);

	public static void main2(String[] args) {
		System.err.println("nude_ns\tview_ns\tincr_%\telements_#\tplacement_#");
		ViewEvaluator
		    .eval(
						ConfigurationStreamer.streamConfigurations(5, -1, "mem", c -> c.nbNodes() * 5, 50, 5, c -> c.nbNodes() > 2,
		            c -> c.nbVMs() > 2),
		        c -> new SelfMadeStreamer(c, 2 * (c.nbNodes() + c.nbExterns()), 4 * c.nbNodes(), 3, 4, 4).stream(), 10)
		    .limit(5000).forEach(vi -> acceptViewImpact(vi));
	}

	public static void acceptViewImpact(ViewImpact<NetworkView> vi) {
		int nbHosts = vi.configuration.nbNodes() + vi.configuration.nbExterns();
		int nbVMs = vi.configuration.nbVMs();

		System.out
		    .println(vi.nudeTime_ns + "\t" + vi.worseViewTime_ns + "\t" 
		    + (100 * vi.worseViewTime_ns / vi.nudeTime_ns - 100) + "\t" + (nbHosts + nbVMs) + "\t" + Math.pow(nbHosts, nbVMs));
	}

	public static void main(String[] args) {
		ViewEvaluator v = new ViewEvaluator(10, 1000);
		ViewImpactAggregator<NetworkView> agg = new ViewImpactAggregator<NetworkView>()
		    .withConfigWeigther(IConfiguration::nbHosts);
		Stream<IConfiguration> cex = ConfigurationStreamer.streamConfigurations(5, -1, "mem", c -> c.nbNodes() * 5, 50, 5,
		    c -> c.nbNodes() > 2, c -> c.nbVMs() > 2);
		ViewStreamer<NetworkView> vex = c -> new SelfMadeStreamer(c, 2 * (c.nbNodes() + c.nbExterns()), 4 * c.nbNodes(), 3,
		    4, 4).stream();
		v.eval(cex, vex, agg);
		agg.dataOrdered((i, w) -> System.err.println("weight " + w + " pct " + i.pctIncrease()));
	}

}

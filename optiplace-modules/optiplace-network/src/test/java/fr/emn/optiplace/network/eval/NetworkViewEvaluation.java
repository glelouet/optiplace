
package fr.emn.optiplace.network.eval;

import java.util.stream.Stream;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.eval.ConfigurationStreamer;
import fr.emn.optiplace.eval.ViewEvaluator;
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

	public static void main(String[] args) {
		ViewImpactAggregator<NetworkView> agg = new ViewImpactAggregator<NetworkView>()
				.withConfigWeigther(IConfiguration::nbElems);
		Stream<IConfiguration> cex = ConfigurationStreamer
				.streamConfigurations(5, -1, "mem", c -> c.nbNodes() * 5, 50, 3, c -> c.nbNodes() > 2, c -> c.nbVMs() > 2)
				.filter(Optiplace::hasSolution).limit(1000);
		ViewStreamer<NetworkView> vex = c -> new NetworkViewStreamer(c, 2 * (c.nbNodes() + c.nbExterns()), 4 * c.nbNodes(),
				3, 4, 4, 4).stream();
		ViewEvaluator.evaluate(cex, vex, ViewEvaluator.OPEN_NODES, agg);
		agg.dataOrdered((i, w) -> System.err.println("weight " + w + " pct " + i.pctView()));
	}

}

package fr.emn.optiplace.eval;

import java.util.stream.Stream;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.view.View;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public interface IViewEvaluator {

	<T extends View> void eval(Stream<IConfiguration> cex, ViewStreamer<T> vex, ViewImpactAggregator<T> aggreg);


}

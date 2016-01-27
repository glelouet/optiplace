
package fr.emn.optiplace.eval;

import java.util.function.ObjLongConsumer;
import java.util.function.ToLongFunction;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.view.View;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;


/**
 * Aggregates View evaluation data. Also provides a return on the max time
 * percent increase to a similar configuration.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class ViewImpactAggregator<T extends View> {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ViewImpactAggregator.class);


	protected ToLongFunction<IConfiguration> configurationWeighter;

	public ViewImpactAggregator<T> withConfigWeigther(ToLongFunction<IConfiguration> weighter) {
		configurationWeighter = weighter;
		return this;
	}

	TLongObjectHashMap<ViewCfgEval<T>> weightToEval = new TLongObjectHashMap<>();
	TLongIntHashMap weightToPct = new TLongIntHashMap();

	public void add(ViewCfgEval<T> eval) {
		int pct = (int) (100 * (eval.worseViewEval) / eval.nudeEval);
		long weight = weight(eval.configuration);
		if(pct>percent(weight)) {
			weightToEval.put(weight, eval);
			weightToPct.put(weight, pct);
		}
	}

	public long weight(IConfiguration cfg) {
		long w = configurationWeighter.applyAsLong(cfg);
		return w;
	}

	/**
	 * get the worse value percent registered for a Configuration with same weight
	 * as the one given.
	 *
	 * @param cfg
	 *          a {@link IConfiguration} to get the weight.
	 * @return the existing worse percentage if existing, or 0 if no configuration
	 *         with same weight has been added yet.
	 */
	public int getPercent(IConfiguration cfg) {
		return percent(weight(cfg));
	}

	public int percent(long weight) {
		return weightToPct.get(weight);
	}

	/**
	 * apply a consumer on the couples (impact, weight) after ordering them by
	 * weight
	 * 
	 * @param consumer
	 *          a consumer to apply
	 */
	public void dataOrdered(ObjLongConsumer<ViewCfgEval<T>> consumer) {
		long[] keys = weightToEval.keys();
		java.util.Arrays.sort(keys);
		for (long l : keys) {
			consumer.accept(weightToEval.get(l), l);
		}
	}

}

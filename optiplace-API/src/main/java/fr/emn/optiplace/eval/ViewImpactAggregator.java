
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

	TLongObjectHashMap<WorseViewEvaluation<T>> weightToEval = new TLongObjectHashMap<>();
	TLongIntHashMap weightToIncrease = new TLongIntHashMap();

	public void add(WorseViewEvaluation<T> eval) {
		int pct = (int) (100 * (eval.worseViewEval - eval.nudeEval) / eval.nudeEval);
		long weight = weight(eval.configuration);
		if(pct>increase(weight)) {
			System.err.println("new worse pct " + pct + " for view size " + weight);
			weightToEval.put(weight, eval);
			weightToIncrease.put(weight, pct);
		}
	}

	public long weight(IConfiguration cfg) {
		long w = configurationWeighter.applyAsLong(cfg);
		return w;
	}

	/**
	 * get the worse value increase percent registered for a Configuration with
	 * same weight as the one given.
	 *
	 * @param cfg
	 *          a {@link IConfiguration} to get the weight.
	 * @return the existing worse percentage increase if existing, or 0 if no
	 *         configuration with same weight has been added yet.
	 */
	public int getIncrease(IConfiguration cfg) {
		return increase(weight(cfg));
	}

	public int increase(long weight) {
		return weightToIncrease.get(weight);
	}

	/**
	 * apply a consumer on the couples (impact, weight) after ordering them by
	 * weight
	 * 
	 * @param consumer
	 *          a consumer to apply
	 */
	public void dataOrdered(ObjLongConsumer<WorseViewEvaluation<T>> consumer) {
		long[] keys = weightToEval.keys();
		java.util.Arrays.sort(keys);
		for (long l : keys) {
			consumer.accept(weightToEval.get(l), l);
		}
	}

}

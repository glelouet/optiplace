
package fr.emn.optiplace.eval;

import java.util.function.ObjLongConsumer;
import java.util.function.ToLongFunction;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.view.View;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongObjectHashMap;


/**
 * Aggregates View impact data. Also provides a return on the max time percent
 * increase to a similar configuration.
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

	TLongObjectHashMap<ViewImpact<T>> weightToImpact = new TLongObjectHashMap<>();
	TLongIntHashMap weightToIncrease = new TLongIntHashMap();

	public void add(ViewImpact<T> impact) {
		int pct = (int) (100 * (impact.worseViewTime_ns - impact.nudeTime_ns) / impact.nudeTime_ns);
		long weight = weight(impact.configuration);
		if(pct>increase(weight)) {
			System.err.println("increased pct (" + pct + ") for weight " + weight);
			weightToImpact.put(weight, impact);
			weightToIncrease.put(weight, pct);
		}
	}

	public long weight(IConfiguration cfg) {
		long w = configurationWeighter.applyAsLong(cfg);
		System.err.println("weight " + w);
		return w;
	}

	/**
	 * get the worse time increase percent registered for a Configuration with
	 * same weigth as the one given.
	 *
	 * @param cfg
	 *          a {@link IConfiguration} to get the weight.
	 * @return the existing worse percentage increase if existing, or 0 if no
	 *         configuration with same weight has been added yet.
	 */
	public int increase(IConfiguration cfg) {
		return increase(weight(cfg));
	}

	public int increase(long weight) {
		return weightToIncrease.get(weight);
	}

	public void dataOrdered(ObjLongConsumer<ViewImpact<T>> consumer) {
		long[] keys = weightToImpact.keys();
		java.util.Arrays.sort(keys);
		for (long l : keys) {
			consumer.accept(weightToImpact.get(l), l);
		}
	}

}

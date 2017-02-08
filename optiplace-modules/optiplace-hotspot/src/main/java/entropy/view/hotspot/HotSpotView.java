
package entropy.view.hotspot;

import java.util.Map;
import java.util.stream.Stream;

import org.chocosolver.solver.variables.IntVar;

import entropy.view.hotspot.goals.ReduceHeatGoal;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;


/**
 * view of the rear temperature of the servers. The rear temperature of a server
 * is a linear function of the servers' consumption. An impact matrix is
 * required to model this rear temperature function.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
@ViewDesc
public class HotSpotView extends EmptyView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HotSpotView.class);


	@Parameter(confName = "hotspot")
	ImpactMatrix data = null;

	public HotSpotView() {
		this(new ImpactMatrix());
	}

	public HotSpotView(ImpactMatrix rearImpact) {
		setImpacts(rearImpact);
	}

	public void setImpacts(ImpactMatrix imp) {
		data = imp;
	}

	public ImpactMatrix getImpacts() {
		return data;
	}


	@Depends
	PowerView consumption = null;

	public PowerView getConsumption() {
		return consumption;
	}

	public void setConsumption(PowerView consumption) {
		this.consumption = consumption;
	}

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
		cachedRears = new IntVar[b.nodes().length];
	}


	private IntVar[] cachedRears = null;

	/**
	 * get the constrained {@link IntVar variable} corresponding to a node rear
	 * temperature.
	 *
	 * @param n
	 *          the node to get the consumption variable
	 * @return the variable internally constrained to the server consumption,
	 *         created ot retrieved if cached.
	 */
	public IntVar getRearTemp(Node n) {
		int nidx = b.node(n);
		if (nidx == -1) {
			return null;
		}
		IntVar ret = cachedRears[nidx];
		if (ret == null) {
			ret = makeRear(n);
			cachedRears[nidx] = ret;
		}
		return ret;
	}

	/**
	 * ensure we have cached all the rear temperatures, then return the cached
	 * array.
	 * 
	 * @return the internal array of all the cached rear temperatures.
	 */
	public IntVar[] getAllRearTemps() {
		for (Node n : b.nodes()) {
			getRearTemp(n);
		}
		return cachedRears;
	}

	/**
	 * as we use integers internally, we must multiply by a granularity to avoid
	 * multiplication approximations
	 */
	int granularity = 2 * 3 * 5 * 7 * 11 * 13;

	protected IntVar makeRear(Node n) {
		// make a scalar operation on nodes power and their impact.
		IntVar[] powers = consumption.getAllNodesPowers();
		Map<String, Double> impacters = data.getImpacters(n.getName());
		int[] mults = Stream.of(b.nodes())
				.mapToInt(impacter -> (int) (granularity * impacters.get(impacter.getName()))).toArray();
		return v.div(v.scalar(powers, mults), granularity);
	}

	@Goal
	public SearchGoal reduceHeat() {
		return new ReduceHeatGoal(this);
	}

	IntVar cachedMaxRear = null;

	public IntVar maxRearTemperature() {
		IntVar ret = cachedMaxRear;
		if (ret == null) {
			IntVar[] increases = new IntVar[b.nodes().length];
			for (int i = 0; i < increases.length; i++) {
				increases[i] = makeRear(b.node(i));
			}
			ret = v.max(increases);
			cachedMaxRear = ret;
		}
		return ret;
	}

	@Override
	public void clear() {
		super.clear();
		cachedMaxRear = null;
		cachedRears = null;
	}

}


package entropy.view.hotspot;

import java.util.HashMap;
import java.util.Map.Entry;

import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

import entropy.view.hotspot.goals.ReduceHeatGoal;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;


/**
 * view of the rear temperature of the servers. The rear temperature is function
 * of the servers consumption and their rear impacts.
 *
 * @author guillaume
 *
 */
@ViewDesc
public class HotSpotView extends EmptyView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HotSpotView.class);

	public HotSpotView() {
		this(new ImpactMatrix());
	}

	public HotSpotView(ImpactMatrix rearImpact) {
		data = rearImpact;
	}

	@Parameter(confName = "hotspot")
	ImpactMatrix data = null;

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

	private final HashMap<String, IntVar> cachedRears = new HashMap<String, IntVar>();

	/**
	 * get the constrained {@link IntVar variable} corresponding to a node
	 * consumption.
	 *
	 * @param n
	 *          the node to get the consumption variable
	 * @return the variable internally constrained to the server consumption,
	 *         created ot retrieved if cached.
	 */
	public IntVar getRearTemp(Node n) {
		IntVar ret = cachedRears.get(n.getName());
		if (ret == null) {
			ret = makeRear(n);
			cachedRears.put(n.getName(), ret);
			onNewVar(ret);
		}
		return ret;
	}

	/** as we use integers internally, we must */
	int granularity = 1000;

	protected IntVar makeRear(Node n) {
		IntVar ret = pb.v().createBoundIntVar(n.getName() + ".hotspot", 0, Integer.MAX_VALUE);
		IntVar val = pb.v().createIntegerConstant(0);
		for (Entry<String, Double> e : data.getImpacters(n.getName()).entrySet()) {
			Node from = new Node(e.getKey());
			val = v.plus(val, v.mult(consumption.getPower(from), (int) (granularity * e.getValue())));
		}
		pb.getSolver().post(ICF.arithm(ret, "=", v.div(val, granularity)));
		return ret;
	}

	@Goal
	public SearchGoal reduceHeat() {
		return new ReduceHeatGoal(this);
	}

	IntVar cachedMaxRear = null;
	public IntVar maxRearIncrease() {
		IntVar ret = cachedMaxRear;
		if (ret == null) {
			IntVar[] increases = new IntVar[pb.b().nodes().length];
			for (int i = 0; i < increases.length; i++) {
				increases[i] = makeRear(pb.b().node(i));
			}
			ret = pb.v().max(increases);
			cachedMaxRear = ret;
		}
		return ret;
	}

	@Override
	public void clear() {
		super.clear();
		cachedMaxRear = null;
		cachedRears.clear();
	}

}

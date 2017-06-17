package fr.emn.optiplace.power;

import java.util.ArrayList;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.power.goals.TotalPowerEvaluator;
import fr.emn.optiplace.power.powermodels.LinearCPUCons;
import fr.emn.optiplace.power.rules.LimitPower;
import fr.emn.optiplace.power.rules.LimitSumPower;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

/**
 * view of the consumption of the nodes in a Datacenter.<br />
 * based on a {@link PowerData} retrieved through {@link #getPowerData()}, it
 * gives the ability to associate energy consumption to nodes in its
 * {@link IReconfigurationProblem}, as the linear function of this node's CPU
 * usage.
 *
 * @author guillaume
 */
@ViewDesc
public class PowerView extends EmptyView {

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PowerView.class);

	public class Params {
		public boolean useOptimal = false;
		public boolean usePotential = true;

		/**
		 * the granularity to have correct model.<br />
		 * Since we use integer model, but the consumption can be in W, the linear
		 * model can dive integers by intergers and then lose precision. so we use a
		 * multiplier to reduce the precision loss<br />
		 * the higher, the longer to solve but the better the result.
		 */
		int requiredGranulartity = 10;
	}

	public Params params = new Params();

	@Parameter(confName = "power")
	private PowerData powerData = new PowerData();

	/**
	 * set the {@link PowerData data} of the servers' min and max consumption,
	 * used to produce the formulas
	 */
	public void setPowerData(PowerData data) {
		powerData = data;
	}

	/**
	 * @return the internal consumptionData
	 */
	public PowerData getPowerData() {
		return powerData;
	}

	public PowerView() {
		this(new PowerData());
	}

	public PowerView(PowerData powerData) {
		super();
		this.powerData = powerData;
	}

	/**
	 * limit the power of a set of Nodes .each power is limited independently
	 *
	 * @param value
	 *          the maximum power of each node
	 * @param nodes
	 *          the nodes to limit the power of, or null to select all
	 */
	public void limitPower(int value, Node... nodes) {
		addRule(new LimitPower(this, value, nodes));
	}

	/**
	 * limit the total power of a set of Nodes. Powers are summed
	 *
	 * @param value
	 *          the maximum total power
	 * @param nodes
	 *          the nodes to limit the power of, or null to select all
	 */
	public void limitSumPower(int value, Node... nodes) {
		addRule(new LimitSumPower(this, value, nodes));
	}

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
		cachedPowers = new IntVar[rp.b().nodes().length];
	}

	@Override
	public void clear() {
		super.clear();
		cachedPowers = null;
		cachedTotalPower = null;
		cachedMaxPowerDiff = null;
	}

	private IntVar[] cachedPowers = null;

	/**
	 * get the constrained {@link IntVar variable} corresponding to a node
	 * consumption.
	 *
	 * @param n
	 *          the node to get the consumption variable
	 * @return the variable internally constrained to the server consumption,
	 *         created ot retrieved if cached.
	 */
	public IntVar getPower(Node n) {
		int nidx = pb.b().location(n);
		if (nidx == -1) {
			return null;
		}
		IntVar ret = cachedPowers[nidx];
		if (ret == null) {
			ret = makePower(n);
			cachedPowers[nidx] = ret;
		}
		return ret;
	}

	/**
	 * what should we do when a model is missing ? default is throw exception, but
	 * e can also warn and return 0, or just return 0
	 */
	protected enum onMissingModel {
		EXCEPTION, ZEROANDWARN, ZERO
	}

	protected onMissingModel onmMissingModel = onMissingModel.EXCEPTION;

	/**
	 * @return the onmMissingModel
	 */
	public onMissingModel getOnmMissingModel() {
		return onmMissingModel;
	}

	/**
	 * @param onmMissingModel
	 *          the onmMissingModel to set
	 */
	public void setOnmMissingModel(onMissingModel onmMissingModel) {
		this.onmMissingModel = onmMissingModel;
	}

	/**
	 * create the consumption of a server's name, with no cache handling
	 *
	 * @param n
	 *          the node
	 * @return the {@link IntVar} corresponding to the server consumption
	 */
	protected IntVar makePower(Node n) {
		PowerModel cm = powerData.get(n);
		if (cm == null) {
			System.err.println("no data for node " + n + ", models are  :" + powerData);
			switch (onmMissingModel) {
			case EXCEPTION:
				throw new UnsupportedOperationException("no consumption model for node " + n);
			case ZEROANDWARN:
				logger.warn("no consumption model for node " + n);
				return v.createIntegerConstant(0);
				// no break, we go to zero then
			case ZERO:
				return v.createIntegerConstant(0);
			default:
				throw new UnsupportedOperationException("case " + onmMissingModel + " not supported here");
			}
		}
		IntVar ret = cm.makePower(n, this);
		return ret;
	}

	private IntVar cachedTotalPower = null;

	/**
	 * @return an {@link IntVar} constrained to the total consumption of the
	 *         servers
	 */
	public IntVar getTotalPower() {
		if (cachedTotalPower == null) {
			cachedTotalPower = makeTotalPower();
		}
		return cachedTotalPower;
	}

	@Goal
	public SearchGoal powerCost() {
		return new TotalPowerEvaluator(this);
	}

	/**
	 * @return an {@link IntVar} constrained to the sumn of the Consumptions of
	 *         the nodes on the last {@link #associate(IReconfigurationProblem)
	 *         injected} {@link IReconfigurationProblem}
	 */
	protected IntVar makeTotalPower() {
		IntVar[] pmPower = new IntVar[c.nbNodes()];
		for (int i = 0; i < pmPower.length; i++) {
			pmPower[i] = getPower((Node) b.location(i));
		}
		IntVar ret = pb.v().createBoundIntVar("dcPower", evalMinVMsCons(), evaluateMaxCons());
		onNewVar(ret);
		post(pb.getModel().sum(pmPower, "=", ret));
		return ret;
	}

	/**
	 * @return the sum of the minimum of consumption induced by every running VM
	 */
	public int evalMinVMsCons() {
		double ret = 0;
		for (VM vm : b.vms()) {
			double min = Double.POSITIVE_INFINITY;
			for (Node n : b.nodes()) {
				PowerModel model = powerData.get(n);
				double mincons = model.getMinPowerIncrease(n, pb.c().resources(), vm);
				if (mincons < min) {
					min = mincons;
				}
			}
			ret += min;
		}
		// System.err.println("eval min cons=" + ret);
		return (int) ret;
	}

	/**
	 * evaluation of the worst max consumption of the problem, if the VMs are
	 * allocated on the worse Nodes
	 */
	public int evaluateMaxCons() {
		int maxNodeCons = 0;
		for (Node n : b.nodes()) {
			PowerModel model = powerData.get(n);
			maxNodeCons += model.maxCons(n);
		}
		return maxNodeCons;
	}

	/**
	 * determine what is the default multiplier of powers
	 *
	 * @param linearCons
	 * @param linearNodes
	 * @return
	 */
	protected int findMultplier(ArrayList<LinearCPUCons> linearCons, ArrayList<Node> linearNodes,
			ResourceSpecification cpu) {
		double mult = 1;
		for (int i = 0; i < linearCons.size(); i++) {
			LinearCPUCons m = linearCons.get(i);
			Node n = linearNodes.get(i);
			double local = params.requiredGranulartity * cpu.getCapacity(n) / (m.max - m.min);
			if (local > mult) {
				mult = local;
			}
		}
		return (int) Math.ceil(mult);
	}

	/**
	 * ensure we cache all the nodes power and return the cached array of nodes
	 * power
	 *
	 * @return the internal table of all nodes' power IntVars.
	 */
	public IntVar[] getAllNodesPowers() {
		for (Node n : pb.b().nodes()) {
			getPower(n);
		}
		return cachedPowers;
	}

	protected IntVar cachedMaxPowerDiff = null;

	public IntVar getMaxPowerDiff() {
		if (cachedMaxPowerDiff == null) {
			IntVar maxLoad = v.max(getAllNodesPowers());
			IntVar minLoad = v.min(getAllNodesPowers());
			cachedMaxPowerDiff = v.createBoundIntVar("consumptionMaxDiff", 0, maxLoad.getUB() - minLoad.getLB());
			post(pb.getModel().arithm(v.plus(cachedMaxPowerDiff, minLoad), "=", maxLoad));
			onNewVar(cachedMaxPowerDiff);
		}
		return cachedMaxPowerDiff;
	}

	@Override
	public void setConfig(ProvidedData conf) {
		powerData = new PowerData();
		powerData.read(conf);
	}

}

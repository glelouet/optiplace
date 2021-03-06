package fr.emn.optiplace.view.linearpower;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.View;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;
import fr.emn.optiplace.view.linearpower.goals.TotalPowerEvaluator;

/**
 * Support description of power consumption in a all-linear model.
 * <p>
 * The total energy is the sum of three values :
 * <ul>
 * <li>each node has a base power depending on whether a VM is hosted on it</li>
 * <li>each VM has a relative power depending on the Computer or hoster it is
 * hosted on</li>
 * <li>each VM migration has an energy cost</li>
 * </ul>
 * VM power and Computer power is multiplied by the time slot value, then added
 * to the VM migration cost.
 * </p>
 * <p>
 * the default values are null cost for VM relative power, migration energy, and
 * base Computer power.
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
@ViewDesc
public class LinearPowerView extends EmptyView {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LinearPowerView.class);

	@Parameter(confName = "lineaPower")
	private LinearPowerData data;

	/** @return the data */
	public LinearPowerData getData() {
		return data;
	}

	/**
	 * @param data
	 *          the data to set
	 */
	public void setData(LinearPowerData data) {
		this.data = data;
	}

	public LinearPowerView() {
		this(new LinearPowerData());
	}

	public LinearPowerView(LinearPowerData data) {
		this.data = data;
	}

	/**
	 * what should we do when a model is missing ? default is throw exception, but
	 * e can also warn and return 0, or just return 0
	 */
	public static enum onMissingModel {
		EXCEPTION {
			@Override
			public IntVar missingModel(Computer n, View lpv) {
				throw new UnsupportedOperationException("no consumption model for node " + n);
			}
		},
		ZEROANDWARN {
			@Override
			public IntVar missingModel(Computer n, View lpv) {
				logger.warn("no consumption model for node " + n);
				return lpv.getProblem().v().createIntegerConstant(0);
			}
		},
		ZERO {
			@Override
			public IntVar missingModel(Computer n, View lpv) {
				return lpv.getProblem().v().createIntegerConstant(0);
			}
		};
		public abstract IntVar missingModel(Computer n, View lpv);
	}

	/** action to perform when the model of a node iss missing */
	protected onMissingModel onmMissingModel = onMissingModel.EXCEPTION;

	/** @return the onmMissingModel */
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

	/** power cost of executing the node i alone */
	protected int[] cachedComputerBaseCons;

	/** power cost of vm i on node j */
	protected int[][] cachedHostingCosts;

	/** base power of node i */
	protected IntVar[] cachedComputerLonePowers;

	/** total power of vm i */
	protected IntVar[] cachedVMCons;

	/** total power of node i */
	protected IntVar[] cachedPowers = null;

	/** total power of nodes + vms */
	protected IntVar totalPower = null;

	@Override
	public void clear() {
		super.clear();
		cachedPowers = null;
		cachedHostingCosts = null;
		cachedComputerBaseCons = null;
		cachedComputerLonePowers = null;
		cachedVMCons = null;
	}

	/**
	 * get the constrained {@link IntVar variable} corresponding to a node
	 * consumption.
	 *
	 * @param n
	 *          the node to get the consumption variable
	 * @return the variable internally constrained to the server consumption,
	 *         created ot retrieved if cached.
	 */
	public IntVar getConsumption(Computer n) {
		if (cachedPowers == null) {
			cachedPowers = new IntVar[pb.c().nbComputers()];
		}
		int nodeidx = pb.b().location(n);
		if (nodeidx < 0) {
			return null;
		}
		IntVar ret = cachedPowers[nodeidx];
		if (ret == null) {
			ret = makePower(n);
			cachedPowers[nodeidx] = ret;
		}
		return ret;
	}

	/**
	 * create the consumption of a server's name, without looking in the cache
	 *
	 * @param n
	 *          the node
	 * @return the {@link IntVar} corresponding to the server consumption
	 */
	protected IntVar makePower(Computer n) {
		LinearPowerModel pm = data.getModel(n);
		if (pm == null) {
			return onmMissingModel.missingModel(n, this);
		}
		IntVar ret = pm.makePower(n, this);
		return ret;
	}

	public int[] getComputerBaseCons() {
		if (cachedComputerBaseCons == null) {
			cachedComputerBaseCons = data.makeComputerRunningCost(pb.b().nodes());
		}
		return cachedComputerBaseCons;
	}

	/**
	 *
	 * @return the array , for each node, of its self running cost 0 if offline, a
	 *         fixed cost if online)
	 */
	public IntVar[] getComputersLonePower() {
		if (cachedComputerLonePowers == null) {
			int[] cachedComputerBase = getComputerBaseCons();
			cachedComputerLonePowers = new IntVar[cachedComputerBase.length];
			for (int i = 0; i < cachedComputerLonePowers.length; i++) {
				cachedComputerLonePowers[i] = v.createIntegerConstant(cachedComputerBase[i]);
			}
		}
		return cachedComputerLonePowers;
	}

	public IntVar getComputerLonePower(Computer n) {
		return getComputersLonePower()[pb.b().location(n)];
	}

	public int[][] getHostingCosts() {
		if (cachedHostingCosts == null) {
			cachedHostingCosts = data.makeHostingCost(pb.getSourceConfiguration(), null, null);
		}
		return cachedHostingCosts;
	}

	public IntVar[] getVMsPower() {
		if (cachedVMCons == null) {
			int[][] hostingCosts = getHostingCosts();
			int nbVMs = pb.c().nbVMs();
			cachedVMCons = new IntVar[nbVMs];
			for (int i = 0; i < nbVMs; i++) {
				IntVar vmCons = pb.v().createBoundIntVar("vm" + i + ".cons", 0, Integer.MAX_VALUE - 1);
				cachedVMCons[i] = vmCons;
				h.element(pb.getVMLocation(pb.b().vm(i)), hostingCosts[i], vmCons);
			}
		}
		return cachedVMCons;
	}

	public IntVar getVMPower(VM vm) {
		return getVMsPower()[pb.b().vm(vm)];
	}

	public IntVar getTotalPower() {
		if (totalPower == null) {
			totalPower = v.sum("LinearPower.totalPower", v.sum("LinearPower.nodesLonePower", getComputersLonePower()),
					v.sum("LinearPower.VMsPower", getVMsPower()));
		}
		return totalPower;
	}

	@Goal
	public TotalPowerEvaluator powerCost() {
		return new TotalPowerEvaluator(this);
	}

}

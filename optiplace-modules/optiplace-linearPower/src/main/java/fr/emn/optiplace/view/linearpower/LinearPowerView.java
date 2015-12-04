package fr.emn.optiplace.view.linearpower;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.View;

/**
 * Support description of power consumption in a all-linear model.
 * <p>
 * The total energy is the sum of three values :
 * <ul>
 * <li>each node has a base power depending on whether a VM is hosted on it</li>
 * <li>each VM has a relative power depending on the Node or hoster it is hosted
 * on</li>
 * <li>each VM migration has an energy cost</li>
 * </ul>
 * VM power and Node power is multiplied by the time slot value, then added to
 * the VM migration cost.
 * </p>
 * <p>
 * the default values are null cost for VM relative power, migration energy, and
 * base Node power.
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
public class LinearPowerView extends EmptyView {

  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
      .getLogger(LinearPowerView.class);

  private LinearPowerData data;

  /** @return the data */
  public LinearPowerData getData() {
    return data;
  }

  /** @param data the data to set */
  public void setData(LinearPowerData data) {
    this.data = data;
  }

  public LinearPowerView() {
    this(new LinearPowerData());
  }

  public LinearPowerView(LinearPowerData data) {
    this.data = data;
  }

  /** what should we do when a model is missing ? default is throw exception, but
   * e can also warn and return 0, or just return 0 */
  public static enum onMissingModel {
    EXCEPTION {
      @Override
      public IntVar missingModel(Node n, View lpv) {
        throw new UnsupportedOperationException(
            "no consumption model for node " + n);
      }
    },
    ZEROANDWARN {
      @Override
      public IntVar missingModel(Node n, View lpv) {
        logger.warn("no consumption model for node " + n);
				return lpv.getProblem().v().createIntegerConstant(0);
      }
    },
    ZERO {
      @Override
      public IntVar missingModel(Node n, View lpv) {
				return lpv.getProblem().v().createIntegerConstant(0);
      }
    };
    public abstract IntVar missingModel(Node n, View lpv);
  }

  /** action to perform when the model of a node iss missing */
  protected onMissingModel onmMissingModel = onMissingModel.EXCEPTION;

  /** @return the onmMissingModel */
  public onMissingModel getOnmMissingModel() {
    return onmMissingModel;
  }

  /** @param onmMissingModel the onmMissingModel to set */
  public void setOnmMissingModel(onMissingModel onmMissingModel) {
    this.onmMissingModel = onmMissingModel;
  }

  /** power cost of executing the node i alone */
  protected int[] cachedNodeBaseCons;

  /** power cost of vm i on node j */
  protected int[][] cachedHostingCosts;

  /** base power of node i */
  protected IntVar[] cachedNodeLonePowers;

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
    cachedNodeBaseCons = null;
    cachedNodeLonePowers = null;
    cachedVMCons = null;
  }

  /** get the constrained {@link IntVar variable} corresponding to a node
   * consumption.
   * @param n the node to get the consumption variable
   * @return the variable internally constrained to the server consumption,
   * created ot retrieved if cached. */
  public IntVar getConsumption(Node n) {
    if (cachedPowers == null) {
			cachedPowers = new IntVar[pb.c().nbNodes()];
    }
		int nodeidx = pb.b().node(n);
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

  /** create the consumption of a server's name, without looking in the cache
   * @param n the node
   * @return the {@link IntVar} corresponding to the server consumption */
  protected IntVar makePower(Node n) {
    IntVar powerstate = pb.isOnline(n);
    if (powerstate.isInstantiatedTo(0)) {
			return pb.v().createIntegerConstant(0);
    }
    LinearPowerModel pm = data.getModel(n);
    if (pm == null) {
      return onmMissingModel.missingModel(n, this);
    }
    IntVar ret = pm.makePower(n, this);
    if (!powerstate.contains(0)) {
      return ret;
    } else {
			return v.mult(pb.isOnline(n), ret);
    }
  }

  public int[] getNodeBaseCons() {
    if (cachedNodeBaseCons == null) {
			cachedNodeBaseCons = data.makeNodeRunningCost(pb.b().nodes());
    }
    return cachedNodeBaseCons;
  }

  public IntVar[] getNodesLonePower() {
    if (cachedNodeLonePowers == null) {
      int[] cachedNodeBase = getNodeBaseCons();
      cachedNodeLonePowers = new IntVar[cachedNodeBase.length];
      for (int i = 0; i < cachedNodeLonePowers.length; i++) {
				cachedNodeLonePowers[i] = v.bswitch(
pb.isOnline(pb.b().node(i)), 0, cachedNodeBase[i]);
      }
    }
    return cachedNodeLonePowers;
  }

  public IntVar getNodeLonePower(Node n) {
		return getNodesLonePower()[pb.b().node(n)];
  }

  public int[][] getHostingCosts() {
    if (cachedHostingCosts == null) {
      cachedHostingCosts = data.makeHostingCost(
pb.getSourceConfiguration(), null, null);
    }
    return cachedHostingCosts;
  }

  public IntVar[] getVMsPower() {
    if (cachedVMCons == null) {
      int[][] hostingCosts = getHostingCosts();
			int nbVMs = pb.c().nbVMs();
      cachedVMCons = new IntVar[nbVMs];
      for (int i = 0; i < nbVMs; i++) {
				IntVar vmCons = pb.v().createBoundIntVar("vm" + i + ".cons", 0,
            Integer.MAX_VALUE - 1);
        cachedVMCons[i] = vmCons;
				v.nth(pb.getNode(pb.b().vm(i)), hostingCosts[i], vmCons);
      }
    }
    return cachedVMCons;
  }

  public IntVar getVMPower(VM vm) {
		return getVMsPower()[pb.b().vm(vm)];
  }

  public IntVar getTotalPower() {
    if (totalPower == null) {
			totalPower = v.sum("LinearPower.totalPower", v.sum("LinearPower.nodesLonePower", getNodesLonePower()),
			    v.sum("LinearPower.VMsPower", getVMsPower()));
    }
    return totalPower;
  }

}

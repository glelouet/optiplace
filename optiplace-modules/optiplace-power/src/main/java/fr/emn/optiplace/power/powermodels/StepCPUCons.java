package fr.emn.optiplace.power.powermodels;

import java.util.Map;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.power.PowerModel;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.solver.choco.RangePropagator;

/** @author guillaume */
public class StepCPUCons implements PowerModel {

  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(StepCPUCons.class);

  int min = 0;
  double[] thres = {};
  int[] vals = {};
  int[] increases = null;

  public void setSteps(int min, double[] thres, int[] vals) {
    this.min = min;
    this.thres = thres;
    this.vals = vals;
  }

  /** modify the table of increases between two steps. null by default until this
   * method is called */
  protected void makeIncreases() {
    increases = new int[vals.length];
    int last = min;
    for (int i = 0; i < vals.length; i++) {
      increases[i] = vals[i] - last;
      last = vals[i];
    }
  }

  /**
   * convert the thresholds by multiplying them by given value
   *
   * @param mult
   *            the value to multiply the thresholds by
   * @return a new array containing the multiplied thresholds
   */
  protected int[] makeProportionnal(int mult) {
    int[] ret = new int[thres.length];
    for (int i = 0; i < thres.length; i++) {
      ret[i] = (int) (thres[i] * mult);
    }
    return ret;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("[").append(min);
    for (int i = 0; i < thres.length && i < vals.length; i++) {
      sb.append(";").append(thres[i]).append(":").append(vals[i]);
    }
    return sb.append("]").toString();
  }

	public static final Parser PARSER = new Parser() {

		@Override
		public StepCPUCons parse(String s) {
			// TODO
			return null;
		}
	};

	// TODO remove this and place it in the static parser
  public void parse(String s) {
    s = s.substring(1, s.length() - 1);
    String[] split = s.split(";");
    min = Integer.parseInt(split[0]);
    thres = new double[split.length - 1];
    vals = new int[split.length - 1];
    for (int i = 1; i < split.length; i++) {
      String[] keyval = split[i].split(":");
      thres[i - 1] = Double.parseDouble(keyval[0]);
      vals[i - 1] = Integer.parseInt(keyval[1]);
    }
    makeIncreases();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj.getClass() == this.getClass()) {
      StepCPUCons other = (StepCPUCons) obj;
      if (min != other.min) {
        return false;
      }
      int size = thres.length;
      if (other.thres.length != size || other.vals.length != size) {
        return false;
      }
      for (int i = 0; i < size; i++) {
        if (other.thres[i] != thres[i] || other.vals[i] != vals[i]) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  @Override
  public double getConsumption(Map<String, ResourceSpecification> specs,
      Node n, VM... vms) {
		return getCons(1.0 * specs.get("CPU").sumUses(vms)
        / specs.get("CPU").getCapacity(n));
  }

  public int getCons(double cpuUsage) {
    int ret = min;
    if (thres != null) {
      for (int i = 0; i < thres.length; i++) {
        if (thres[i] <= cpuUsage) {
          ret = vals[i];
        } else {
          break;
        }
      }
    }
    return ret;
  }

  /** set to false to use a specific constraint instead of elementV */
  public enum MakeMethod {
    ELEMENT {

      @Override
      public IntVar makeConsumption(StepCPUCons target, Node n, PowerView parent) {
        return target.makeConsumptionElement(n, parent);
      }
    },
    STAGE {

      @Override
      public IntVar makeConsumption(StepCPUCons target, Node n, PowerView parent) {
        return target.makeConsumptionStage(n, parent);
      }
    };

    public abstract IntVar makeConsumption(StepCPUCons target, Node n,
        PowerView parent);
  }

  public MakeMethod constructStrategy = MakeMethod.STAGE;

  @Override
  public IntVar makePower(Node n, PowerView parent) {
    return constructStrategy.makeConsumption(this, n, parent);
  }

  public IntVar makeConsumptionElement(Node n, PowerView parent) {
    IReconfigurationProblem pb = parent.getProblem();
		IntVar ret = parent.v.createBoundIntVar(n.getName() + ".cons", min,
        (int) maxCons(n));
    IntVar[] ranges = new IntVar[thres.length + 1];
    for (int i = 0; i <= thres.length; i++) {
      int min = i == 0 ? 0 : ranges[i - 1].getUB() + 1;
			int max = (int) ((i == thres.length ? 1.0 : thres[i]) * parent.getProblem().specs("cpu").getCapacity(n));
			ranges[i] = parent.v.createBoundIntVar(n.getName() + ".cpu.range_" + i,
          min, max);
      // ranges[i]=pb.
    }
		IntVar index = parent.v.createEnumIntVar(n.getName() + "cons_idx", 0,
        thres.length);
		pb.v().nth(index, ranges, pb.getUsedCPU(n));
    int[] consumptions = new int[vals.length + 1];
    for (int i = 0; i < vals.length; i++) {
      consumptions[i + 1] = vals[i];
    }
    consumptions[0] = min;
		pb.v().nth(index, consumptions, ret);
    return ret;
  }

  /**
   * make an array of the ints from 0 to size-1
   *
   * @param size
   *            the number of elements to store
   * @return a new array containing all the ints from 0 to size-1 in
   *         increasing order
   */
  public static int[] range(int size) {
    int[] ret = new int[size];
    for (int i = 0; i < size; i++) {
      ret[i] = i;
    }
    return ret;
  }

  /** make the consumption of a node using a RANGE and then an ELEMENT
   * constraints
   * @param n
   * @param parent
   * @return */
  public IntVar makeConsumptionStage(Node n, PowerView parent) {
    IReconfigurationProblem pb = parent.getProblem();
		IntVar ret = parent.v.createBoundIntVar(n.getName() + ".cons", min,
        (int) maxCons(n));
		int[] thres = makeProportionnal(parent.getProblem().specs("cpu").getCapacity(n));
    int[] vals = new int[thres.length + 1];
    vals[0] = min;
    for (int i = 0; i < this.vals.length; i++) {
      vals[i + 1] = this.vals[i];
    }
		IntVar idx = parent.v.createEnumIntVar("" + n.getName() + ".consRangeIdx",
        range(vals.length));
    parent.post(new Constraint("range", new RangePropagator(pb.getUsedCPU(n),
        idx, thres)));
    parent.post(ICF.element(ret, vals, idx));
    return ret;
  }

  @Override
  public double getMinPowerIncrease(Node n,
      Map<String, ResourceSpecification> specs, VM v) {
    ResourceSpecification cpu = specs.get("CPU");
		double cpuIncrease = 1.0 * cpu.getUse(v) / cpu.getCapacity(n);
    return getMinConsumptionIncrease(cpuIncrease);
  }

  public double getMinConsumptionIncrease(double cpuIncrease) {
    double ret = getCons(cpuIncrease) - min;
    for (int i = 0; i < thres.length && thres[i] + cpuIncrease <= 1; i++) {
      ret = Math.min(ret, getCons(cpuIncrease + thres[i]) - vals[i]);
    }
    return ret;
  }

  @Override
  public double maxCons(Node n) {
    if (vals == null || vals.length == 0) {
      return min;
    }
    return vals[vals.length - 1];
  }
}

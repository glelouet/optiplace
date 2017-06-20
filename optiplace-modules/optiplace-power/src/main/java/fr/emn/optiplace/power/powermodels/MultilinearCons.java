/**
 *
 */

package fr.emn.optiplace.power.powermodels;

import java.util.Map;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceLoad;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.power.PowerModel;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;


/**
 * cons(node)=min+sum(resource r)(use(node, r)*weight(r)/capa(node, r))
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 *
 */
public class MultilinearCons implements PowerModel {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MultilinearCons.class);

	double min;

	String[] resources;

	double[] weights;

	/**
	 * @return the min
	 */
	public double getMin() {
		return min;
	}

	/**
	 * @param min
	 *          the min to set
	 */
	public void setMin(double min) {
		this.min = min;
	}

	/**
	 * @return the resources
	 */
	public String[] getResources() {
		return resources;
	}

	/**
	 * @param resources
	 *          the resources to set
	 */
	public void setResources(String[] resources) {
		this.resources = resources;
	}

	/**
	 * @return the weights
	 */
	public double[] getWeights() {
		return weights;
	}

	/**
	 * @param weights
	 *          the weights to set
	 */
	public void setWeights(double[] weights) {
		this.weights = weights;
	}

	/**
	 *
	 */
	public MultilinearCons() {}

	/**
	 *
	 */
	public MultilinearCons(double min) {
		this.min = min;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		sb.append(min);
		for (int i = 0; i < resources.length; i++) {
			sb.append(";").append(resources[i]).append(":").append(weights[i]);
		}
		return sb.append("]").toString();
	}

	public static boolean iscorrect(String description) {
		if (!description.startsWith("[") || !description.endsWith("]")) {
			return false;
		}
		return true;
	}

	@Override
	public double getConsumption(Map<String, ResourceSpecification> specs, Computer n, VM... vms) {
		double ret = min;
		for (int i = 0; i < resources.length; i++) {
			ResourceSpecification spec = specs.get(resources[i]);
			if (spec != null) {
				double resCons = spec.sumUses(vms) * weights[i] / spec.getCapacity(n);
				ret += resCons;
			} else {
				throw new UnsupportedOperationException("resource " + resources[i] + " not specified in " + specs);
			}
		}
		return ret;
	}

	public IntVar makeScalarConsumption(Computer n, PowerView parent) {
		IntVar[] uses = new IntVar[resources.length + 1];
		double[] mults = new double[resources.length + 1];
		IReconfigurationProblem pb = parent.getProblem();
		int nodeidx = parent.b.location(n);
		for (int i = 0; i < resources.length; i++) {
			ResourceLoad handler = pb.getUse(resources[i]);
			if (handler == null) {
				throw new UnsupportedOperationException("resource not specified " + resources[i]);
			}
			uses[i] = handler.getComputersLoad()[nodeidx];
			mults[i] = weights[i] / handler.getComputersCapa()[nodeidx];
		}
		uses[resources.length] = parent.v.createIntegerConstant((int) min);
		mults[resources.length] = 1.0;
		return pb.v().scalar(uses, mults);
	}

	public IntVar makeSumConsumption(Computer n, PowerView parent) {
		IReconfigurationProblem pb = parent.getProblem();
		int nodeidx = parent.b.location(n);
		IntVar[] resourceAdd = new IntVar[resources.length + 1];
		for (int i = 0; i < resources.length; i++) {
			ResourceLoad handler = pb.getUse(resources[i]);
			if (handler == null) {
				throw new UnsupportedOperationException("resource not specified " + resources[i]);
			}
			resourceAdd[i] = pb.v().div(pb.v().mult(handler.getComputersLoad()[nodeidx], (int) weights[i]),
					handler.getComputersCapa()[nodeidx]);
		}
		resourceAdd[resources.length] = pb.v().createIntegerConstant((int) min);
		return pb.v().sum(n.getName() + ".cons", resourceAdd);
	}

	boolean useScalar = false;

	@Override
	public IntVar makePower(Computer n, PowerView parent) {
		return useScalar ? makeScalarConsumption(n, parent) : makeSumConsumption(n, parent);
	}

	@Override
	public double getMinPowerIncrease(Computer n, Map<String, ResourceSpecification> specs, VM v) {
		double ret = 0;
		for (int i = 0; i < resources.length; i++) {
			ResourceSpecification spec = specs.get(resources[i]);
			if (spec != null) {
				ret += spec.getUse(v) * weights[i] / spec.getCapacity(n);
			} else {
				throw new UnsupportedOperationException("resource " + resources[i] + " not specified in " + specs);
			}
		}
		return ret;
	}

	@Override
	public double maxCons(Computer n) {
		double ret = min;
		for (double weight : weights) {
			ret += weight;
		}
		return ret;
	}
}

package fr.emn.optiplace.view.linearpower;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceLoad;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * a model of linear power consumption.<br />
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
public class LinearPowerModel {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(LinearPowerModel.class);

	double base = 0;

	public LinearPowerModel withBase(double base) {
		this.base = base;
		return this;
	}

	String[] resources;

	double[] weights;

	/**
	 * get the weight of a resource
	 *
	 * @param resName
	 *          the id of the resource
	 * @return 0 if name is null, resource is not found ; or the number of W each
	 *         unit of the resource used will consume.
	 */
	public double weight(String resName) {
		if (resName == null || resources == null) {
			return 0;
		}
		for (int i = 0; i < resources.length; i++) {
			if (resName.equals(resources[i])) {
				return weights[i];
			}
		}
		return 0;
	}

	public LinearPowerModel addRes(String name, double weight) {
		if (name == null) {
			return this;
		}
		if (resources == null) {
			resources = new String[1];
			weights = new double[1];
			resources[0] = name;
			weights[0] = weight;
			return this;
		} else {
			for (int i = 0; i < resources.length; i++) {
				if (name.equals(resources[i])) {
					weights[i] = weight;
					return this;
				}
			}
			resources = Arrays.copyOf(resources, resources.length + 1);
			weights = Arrays.copyOf(weights, weights.length + 1);
			resources[resources.length - 1] = name;
			weights[weights.length - 1] = weight;
			return this;
		}
	}

	Pattern resourceWeight = Pattern.compile("(.*)=(.*)");

	public LinearPowerModel parse(String... lines) {
		for (String l : lines) {
			if (l == null) {
				continue;
			}
			Matcher m = resourceWeight.matcher(l);
			if (m.matches()) {
				addRes(m.group(1), Double.parseDouble(m.group(2)));
			} else {
				System.out.println("discarding unmatching line " + l);
			}
		}
		return this;
	}

	public double apply(IConfiguration cfg, Node n) {
		Map<String, ResourceSpecification> resources = cfg.resources();
		double res = base;
		if (resources == null || weights == null) {
			return res;
		}
		for (int i = 0; i < this.resources.length; i++) {
			ResourceSpecification specs = resources.get(this.resources[i]);
			if (specs != null) {
				res += specs.getUse(cfg, n) * weights[i];
			}
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("linear(").append(base);
		for (int i = 0; i < resources.length; i++) {
			sb.append(';').append(resources[i]).append('=').append(weights[i]);
		}
		return sb.append(")").toString();
	}

	/**
	 * @param n
	 * @param linearPowerView
	 * @return
	 */
	public IntVar makePower(Node n, LinearPowerView parent) {
		IntVar[] uses = new IntVar[resources.length + 1];
		double[] mults = new double[resources.length + 1];
		IReconfigurationProblem pb = parent.getProblem();
		int nodeidx = parent.b.location(n);
		for (int i = 0; i < resources.length; i++) {
			ResourceLoad load = pb.getUse(resources[i]);
			if (load == null) {
				throw new UnsupportedOperationException(
						"resource not specified " + resources[i]);
			}
			uses[i] = load.getNodesLoad()[nodeidx];
			mults[i] = weights[i] / load.getNodesCapa()[nodeidx];
		}
		uses[resources.length] = parent.v.createIntegerConstant((int) base);
		mults[resources.length] = 1.0;
		return pb.v().scalar(uses, mults);
	}

	/**
	 * computes how many Watt a vm will consume if hosted on a node with this
	 * model
	 *
	 * @param v
	 *            the vm
	 * @param resourcesSpecs
	 *            the specifications of resources consumption.
	 * @return the power, in W , the vm will make the host consume
	 */
	public double makePower(VM v,
			Map<String, ResourceSpecification> resourcesSpecs) {
		double res = 0;
		for (int i = 0; i < resources.length; i++) {
			ResourceSpecification spec = resourcesSpecs.get(resources[i]);
			if (spec == null) {
				throw new UnsupportedOperationException(
						"resource not specified " + resources[i]);
			}
			res += spec.getUse(v) * weights[i];
		}
		return res;
	}

}

package fr.emn.optiplace.power.powermodels;

import java.util.Map;

import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.power.PowerModel;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 *
 */
public class QuadraticCPUCons implements PowerModel {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(QuadraticCPUCons.class);

	public double min, max, lcoef;

	@Override
	public double getConsumption(Map<String, ResourceSpecification> specs, Node n, VM... vms) {
		double cpuCons = 0;
		ResourceSpecification spec = specs.get("CPU");
		for (VM vm : vms) {
			cpuCons += spec.getUse(vm);
		}
		return getConsumption(cpuCons / spec.getCapacity(n));
	}

	/**
	 * @param n
	 * @param cpuCons
	 * @return
	 */
	private double getConsumption(double cpuCons) {
		return min + (max - min) * lcoef + (max - min) * (max - min) * (1 - lcoef);
	}

	@Override
	public IntVar makePower(Node n, PowerView parent) {
		IReconfigurationProblem pb = parent.getProblem();
		ResourceHandler cpu = pb.getResourcesHandlers().get("CPU");
		int capa = cpu.getSpecs().getCapacity(n);
		double alpha = (max - min) * (1 - lcoef) / capa / capa;
		double beta = (max - min) * lcoef / capa;
		double gamma = min;

		IntVar use = cpu.getNodeLoads()[parent.b.node(n)];
		IntVar sqr = pb.v().mult(use, use);
		IntVar sum = pb.v().scalar(new IntVar[] { sqr, use, parent.v.createIntegerConstant(1) },
				new double[] { alpha, beta, gamma });
		IntVar ret = parent.v.createBoundIntVar(n.getName() + ".consumption", (int) min, (int) max);
		pb.getSolver().post(ICF.times(ret, capa, sum));
		return ret;
	}

	@Override
	public double getMinPowerIncrease(Node n, Map<String, ResourceSpecification> specs, VM v) {
		int use = specs.get("CPU").getUse(v);
		int capa = specs.get("CPU").getCapacity(n);
		return (max - min) * (1 - lcoef) / capa / capa * use * use + (max - min) * lcoef / capa * use + min;
	}

	@Override
	public double maxCons(Node n) {
		return max;
	}
}

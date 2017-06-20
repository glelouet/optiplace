package fr.emn.optiplace.power.powermodels;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.power.PowerModel;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * linear consumption of a server. simply a min(base) value and a max value
 * corresponding to the 100% load of this node's CPU, meaning the node's
 * cons=min+(max-min)*cpuusage/capacpu.
 *
 * @author guillaume
 */
public class LinearCPUCons implements PowerModel {

	@SuppressWarnings("unused")
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LinearCPUCons.class);

	public double min, max;

	public LinearCPUCons() {
	}

	public LinearCPUCons(double min, double max) {
		this.min = min;
		this.max = max;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof LinearCPUCons) {
			LinearCPUCons other = (LinearCPUCons) obj;
			return other.min == min && other.max == max;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return (int) (min + max);
	}

	@Override
	public String toString() {
		return "linearCPU[" + min + ":" + max + "]";
	}

	@Override
	public double getConsumption(Map<String, ResourceSpecification> specs, Computer n, VM... vms) {
		int cpuCons = 0;
		ResourceSpecification spec = specs.get("CPU");
		for (VM vm : vms) {
			cpuCons += spec.getUse(vm);
		}
		return getConsumption(n, cpuCons);
	}

	@Override
	public IntVar makePower(Computer n, PowerView parent) {
		IReconfigurationProblem pb = parent.getProblem();
		double delta = max - min;
		int capa = parent.getProblem().getResourceSpecification("cpu").getCapacity(n);
		IntVar use = pb.getUsedCPU(n);
		IntVar ret = pb.v().createBoundIntVar(n.getName() + ".power", (int) min, (int) max);
		parent.onNewVar(ret);
		Constraint eq = parent.pb.getModel().arithm(pb.v().mult(ret, capa), "=", pb.v().mult(use, (int) delta), "+",
				(int) (min * capa));
		parent.post(eq);
		return ret;
	}

	public double getConsumption(Computer n, double cpuLoad) {
		return min + (max - min) * cpuLoad;
	}

	@Override
	public double getMinPowerIncrease(Computer n, Map<String, ResourceSpecification> specs, VM v) {
		ResourceSpecification cpu = specs.get("CPU");
		return cpu.getUse(v) / cpu.getCapacity(n) * (max - min);
	}

	@Override
	public double maxCons(Computer n) {
		return max;
	}

	@Override
	public double getBestEfficiency(Map<String, ResourceSpecification> specs, Computer n, VM vm) {
		int maxVMs = (int) Math.floor(IConfiguration.maxNBVms(n, vm, specs.values().stream()));
		ResourceSpecification cpu = specs.get("CPU");
		return getConsumption(n, maxVMs * cpu.getUse(vm) / cpu.getCapacity(n)) / maxVMs;
	}

	public static final Parser PARSER = new Parser() {

		private final Pattern matcher = Pattern.compile("linearCPU\\[(.*):(.*)]");

		@Override
		public LinearCPUCons parse(String s) {
			Matcher m = matcher.matcher(s);
			if (!m.matches()) {
				return null;
			}
			return new LinearCPUCons(Double.parseDouble(m.group(1)), Double.parseDouble(m.group(2)));
		}
	};

}

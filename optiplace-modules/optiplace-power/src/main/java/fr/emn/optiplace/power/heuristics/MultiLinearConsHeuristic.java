
package fr.emn.optiplace.power.heuristics;

import java.util.List;

import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 *
 */
public class MultiLinearConsHeuristic {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MultiLinearConsHeuristic.class);

	private final PowerView cons;

	public MultiLinearConsHeuristic(PowerView cd) {
		cons = cd;
	}

	public List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {
		// ArrayList<AbstractStrategy<? extends Variable>> ret = new ArrayList<>();
		VM[] vms = rp.b().vms();
		Computer[] nodes = rp.b().nodes();

		double[] minVMEff = new double[vms.length];
		double[] maxVMEff = new double[vms.length];
		for (int i = 0; i < vms.length; i++) {
			minVMEff[i] = Double.POSITIVE_INFINITY;
			maxVMEff[i] = 0;
		}
		double[] minNodeEff = new double[nodes.length];
		double[] maxNodeEff = new double[nodes.length];
		for (int j = 0; j < nodes.length; j++) {
			minNodeEff[j] = Double.POSITIVE_INFINITY;
			maxNodeEff[j] = 0;
		}
		for (int i = 0; i < vms.length; i++) {
			VM vm = vms[i];
			for (int j = 0; j < nodes.length; j++) {
				Computer n = nodes[j];
				// vm i, node j
				if (rp.getVMLocation(vm).contains(j)) {
					double eff = cons.getPowerData().get(n).getBestEfficiency(rp.c().resources(), n, vm);
					if (eff > maxVMEff[i]) {
						maxVMEff[i] = eff;
					}
					if (eff < minVMEff[i]) {
						minVMEff[i] = eff;
					}
					if (eff > maxNodeEff[j]) {
						maxNodeEff[j] = eff;
					}
					if (eff < minNodeEff[j]) {
						minNodeEff[j] = eff;
					}
				}
			}
		}
		double[] vmDelta = new double[vms.length], nodeDelta = new double[nodes.length];
		for (int i = 0; i < vms.length; i++) {
			vmDelta[i] = maxVMEff[i] - minVMEff[i];
		}
		for (int j = 0; j < vms.length; j++) {
			nodeDelta[j] = maxNodeEff[j] - minNodeEff[j];
		}
		// TODO add smthg in ret
		throw new UnsupportedOperationException();
		// return ret;
	}
}

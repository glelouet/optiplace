package fr.emn.optiplace.core.heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.center.configuration.VM;
import fr.emn.optiplace.center.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.SearchGoal;

/**
 * An heuristic to place the VMs on their hosters in the source configuration.
 * Assign the vms to their hoster, or forbid them.The vms are selected in
 * decreasing order of mem consumption by default, or by the provided comparator
 * in the constructor.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class StickVMsHeuristic {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(StickVMsHeuristic.class);

	public static AbstractStrategy<? extends Variable> makeStickVMs(VM[] vms, IReconfigurationProblem p) {
		int[] srcLoc = new int[vms.length];
		IntVar[] hosters = new IntVar[vms.length];
		for (int i = 0; i < vms.length; i++) {
			srcLoc[i] = p.node(p.getSourceConfiguration().getLocation(vms[i]));
			hosters[i] = p.host(vms[i]);
		}
		Var2ValSelector heuristic = new Var2ValSelector(hosters, srcLoc) {

			private static final long serialVersionUID = 1L;

			// @Override
			// public IntVar getVariable(IntVar[] variables) {
			// IntVar var = super.getVariable(variables);
			// logger.error("StickVMHeuristic.Var2val returned " + var);
			// return var;
			// }
		};
		return SearchGoal.makeAssignHeuristic(StickVMsHeuristic.class.getSimpleName(), heuristic, heuristic, hosters);
	}

	/** the comparator to define in which order to assign the vms */
	private final Comparator<VM> cmp;

	public StickVMsHeuristic(Comparator<VM> cmp) {
		this.cmp = cmp;
	}

	/**
	 * @param spec
	 *          the resource specification to order the vms by.
	 */
	public StickVMsHeuristic(final ResourceSpecification spec) {
		cmp = spec.makeVMComparator(false);
	}

	/** @return the cmp */
	public Comparator<VM> getCmp() {
		return cmp;
	}

	public List<AbstractStrategy<? extends Variable>> getHeuristics(IReconfigurationProblem rp) {
		List<AbstractStrategy<? extends Variable>> ret = new ArrayList<>();
		VM[] vms = rp.getSourceConfiguration().getRunnings().collect(Collectors.toList()).toArray(new VM[0]);
		if (vms == null || vms.length == 0) { return ret; }
		Arrays.sort(vms, cmp);
		ret.add(makeStickVMs(vms, rp));
		return ret;
	}

	@Override
	public String toString() {
		return "StickVMsHeuristic(" + cmp + ")";
	}

}

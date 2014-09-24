package fr.emn.optiplace.core.heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.SearchHeuristic;

/**
 * An heuristic to place the VMs on their hosters in the source configuration.
 * Assign the vms to their hoster, or forbid them.The vms are selected in
 * decreasing order of mem consumption by default, or by the provided comparator
 * in the constructor.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class StickVMsHeuristic implements SearchHeuristic {

	/** the comparator to define in which order to assign the vms */
	private final Comparator<VM> cmp;

	public StickVMsHeuristic(Comparator<VM> cmp) {
		this.cmp = cmp;
	}

	/**
	 * @param spec
	 * the resource specification to order the vms by.
	 */
	public StickVMsHeuristic(final ResourceSpecification spec) {
		cmp = spec.makeVMComparator(false);
	}

	/** @return the cmp */
	public Comparator<VM> getCmp() {
		return cmp;
	}

	@Override
	public List<AbstractIntBranchingStrategy> getHeuristics(
			ReconfigurationProblem rp) {
		List<AbstractIntBranchingStrategy> ret = new ArrayList<AbstractIntBranchingStrategy>();
		VM[] vms = rp.vms().clone();
		if (vms == null || vms.length == 0) {
			return ret;
		}
		Arrays.sort(vms, cmp);
		int[] correspondingNodes = new int[vms.length];
		IntDomainVar[] sortedHosters = new IntDomainVar[vms.length];
		for (int i = 0; i < vms.length; i++) {
			correspondingNodes[i] = rp.node2(rp.getSourceConfiguration().getLocation(
					vms[i]));
			sortedHosters[i] = rp.host(vms[i]);
		}
		Var2ValSelector heuristic = new Var2ValSelector(rp.getEnvironment(),
				sortedHosters, correspondingNodes) {
			@Override
			public IntDomainVar selectVar() {
				IntDomainVar var = super.selectVar();
				// if (var != null) {
				// System.err.println("stickVM selected " + var);
				// }
				return var;
			}
		};
		ret.add(new AssignOrForbidIntVarVal(heuristic, heuristic));
		return ret;
	}

	@Override
	public String toString() {
		return "StickVMsHeuristic(" + cmp + ")";
	}

}

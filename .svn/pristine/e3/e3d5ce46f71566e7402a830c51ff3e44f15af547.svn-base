package fr.emn.optiplace.view;

import java.util.List;

import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * produces heuristics to inject into a {@link ReconfigurationProblem} in order
 * to enhance its solving process.
 * 
 * @author Guillaume Le Louët
 */
public interface SearchHeuristic {

	/**
	 * 
	 * @param rp
	 *            the problem to solve
	 * @return a new list of strategies to add in the solver, or null. This list
	 *         should not be modified externally.
	 */
	public List<AbstractIntBranchingStrategy> getHeuristics(
			ReconfigurationProblem rp);

}

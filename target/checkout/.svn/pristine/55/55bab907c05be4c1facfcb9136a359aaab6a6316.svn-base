package entropy.view;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.solver.choco.ReconfigurationProblem;

/**
 * Describes an objective variable to reduce. Creates a variable in a model and
 * inject constraints linking this variable to its formal representation.<br />
 * can provide heuristics that MAY be used IFF the search request does not
 * provide its own heuristics.
 * 
 * @author Guillaume Le Louët
 */
public interface SearchGoal {

	/**
	 * Produces a formal expression of the objective variable in a model
	 * 
	 * @param rp
	 *            the solver to add constraints into and extract global cost
	 *            from
	 * @return the expression of the added formal cost of the model
	 */
	public IntDomainVar getObjective(ReconfigurationProblem rp);

	/**
	 * @return the heuristics associated with the objective to reduce. Can be
	 *         null, or an empty array, if no heuristic is interesting.
	 */
	public SearchHeuristic[] getHeuristics();
}

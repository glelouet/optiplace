package fr.emn.optiplace.view;

import java.util.List;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * A view, seen as a module in a reconfigurationProblem. Such a module provides
 * resources specifications, rules, objectives and heuristics.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface ViewAsModule {

	/**
	 * associate that view to a problem. May let the problem untouched.<br />
	 * set the used {@link ReconfigurationProblem}
	 * 
	 * @param rp
	 *            the problem to modify and use.
	 */
	void associate(ReconfigurationProblem rp);

	/**
	 * @return the list of resources declared by this view that should be packed
	 *         by the solver.
	 */
	public ResourceSpecification[] getPackedResource();

	/** @return the modifiable list of constraints added by the administrator. */
	public List<Rule> getRequestedRules();

	/**
	 * get the heuristics specified in the view
	 * 
	 * @return the list of heuristics made by the view from its internal
	 *         algorithms. <br />
	 *         The LAST added view's algorithms are used first, but in the order
	 *         they were provided by the view.
	 */
	public List<SearchHeuristic> getSearchHeuristics();

	/**
	 * @return the (optionnal) goal specified by the view.<br />
	 *         The LAST view's specified goal is used, so if any view v2 added
	 *         after this view specifies its own goal the solver will use only
	 *         v2's goal.
	 */
	public SearchGoal getSearchGoal();

	/**
	 * is called by the solver when the solving of the problem has ended. As
	 * such, the view should retrieve the results of the problem
	 * 
	 * @param actionGraph
	 */
	public void endSolving(ActionGraph actionGraph);

}

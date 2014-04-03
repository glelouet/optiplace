package entropy.view;

import java.util.List;

import entropy.configuration.resources.ResourceSpecification;
import entropy.solver.choco.ReconfigurationProblem;

/**
 * A view, seen as a module in a reconfigurationProblem
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

	/**
	 * is called by the solver when the solving of the problem has ended. As
	 * such, the view should retrieve the results of the problem
	 */
	public void endSolving();

	/** @return the modifiable list of constraints added by the administrator. */
	public List<Rule> getRequestedRules();

}

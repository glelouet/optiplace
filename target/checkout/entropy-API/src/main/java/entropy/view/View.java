package entropy.view;

import java.io.File;
import java.util.List;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.solver.choco.ReconfigurationProblem;

/**
 * <p>
 * Add informations to a {@link ReconfigurationProblem problem}. Those
 * informations may be directly added to the problem on the
 * {@link #associate(ReconfigurationProblem)} call, or added on demand when the
 * associated constraints require their {@link ReconfigurationProblem}
 * </p>
 * <p>
 * A view is not supposed to be added to several {@link ReconfigurationProblem}s
 * at the same time, and doing so may crash
 * </p>
 * <p>
 * The constraints should be EITHER managed by the view, using
 * {@link #getRequestedRules()} and {@link #addRule(Rule)}, or externally by the
 * problem launcher.
 * </p>
 * <p>
 * When the problem has been resolved and a solution found (or no solution), the
 * solver will call {@link #endSolving()} for the views to retrieve their
 * results
 * </p>
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
// TODO add a way for a view to be notified when another view creates
// constraints, in order to alter the variables or add its own constraints
public interface View {

	/** @return the modifiable list of constraints added by the administrator. */
	public List<Rule> getRequestedRules();

	/**
	 * shortcut for {@link #getRequestedRules()}.add(cst)
	 * 
	 * @param cst
	 */
	public void addRule(Rule cst);
	/**
	 * add a constraint to the problem, if not already added, and store it in
	 * the list of added constraints.
	 * 
	 * @param eq
	 *            the constraint to add to the problem
	 */
	public void post(SConstraint<? extends Var> eq);

	/**
	 * Declares a new variable has been created by this view. Only variables
	 * directly created by the view should be declared, i.e. the views must not
	 * declare the variables created by other views.
	 * 
	 * @param var
	 */
	public void onNewVar(Var var);

	public IntDomainVar newIntVar(int val);

	public IntDomainVar newIntVar(String name, int min, int max);

	public IntDomainVar newEnumVar(String name, int min, int max);

	public IntDomainVar newEnumVar(String name, int[] sortedValues);

	/**
	 * @return an unmodifiable list of the variables that have been added to the
	 *         model by this view
	 */
	public List<Var> getAddedVars();

	/**
	 * @return an unmodifiable list of the constraints that have been posted to
	 *         the model by this view
	 */
	public List<SConstraint<? extends Var>> getAddedConstraints();

	/** empty any {@link ReconfigurationProblem} - related internal data. */
	public void clear();

	/**
	 * set the required config files. if this implementation should not require
	 * config files, this should do nothing.
	 * 
	 * @param files
	 *            the config files required. use {@link File.getPath()} to get
	 *            its name.
	 */
	public void setConfigFiles(File... files);

	/** @return the problem */
	public ReconfigurationProblem getProblem();

}

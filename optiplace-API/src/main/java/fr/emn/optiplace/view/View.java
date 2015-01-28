package fr.emn.optiplace.view;

import java.util.List;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.Variable;
import fr.emn.optiplace.center.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * <p>
 * A view represents additional domain to inject in a problem to solve.<br/>
 * A view can add
 * <ul>
 * <li>{@link ResourceSpecification}, specified by the
 * {@link #getPackedResource()}, which describes the resources provided by the
 * nodes and used by their vms.</li>
 * <li>{@link Rule} specified by the administrator, which will be translated
 * into constraints in the problem, or will restrict the variables' domains</li>
 * <li>{@link SearchGoal} and their associated {@link SearchHeuristic} to
 * specify the goal when resolving a problem and one or more strategies to
 * explore this problem's potential solutions</li>
 * </ul>
 * </p>
 * <p>
 * The domain informations may be added to the problem
 * <ul>
 * <li>on a {@link View.#associate(ReconfigurationProblem)} call, eg for some
 * new variables to integrate to the problem</li>
 * <li>by a solver call to {@linkplain Rule.#inject(ReconfigurationProblem)} for
 * the rules returned by {@link #getRequestedRules()}</li>
 * </ul>
 * </p>
 * <p>
 * A view can not be associated to several {@link IReconfigurationProblem}s at
 * the same time, and doing so may crash
 * </p>
 * <p>
 * When a problem in which a view was added has been resolved by the solver, and
 * a solution found, the solver will call
 * {@link #endSolving(fr.emn.optiplace.actions.ActionGraph))} for this view. At
 * this moment, the view is supposed to extract it's result solution data from
 * the problem, and deduce its actions to integrate into the ActionGraph. eg. a
 * CPU throttle view would extract the real throttle lvl of the CPUs and add
 * corresponding actions to the action graph.
 * </p>
 * <p>
 * The parameters of the view are specified using {@link ProvidedData}. The view
 * can REQUIRE one configuration, specified using {@link ViewDesc.#configURI()},
 * which will be translated into
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public interface View extends ViewAsModule {

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
    public void post(Constraint eq);

    /**
     * Declares a new variable has been created by this view. Only variables
     * directly created by the view should be declared, i.e. the views must not
     * declare the variables created by other views.
     *
     * @param var
     */
    public void onNewVar(Variable var);

    /**
     * @return an unmodifiable list of the variables that have been added to the
     *         model by this view
     */
    public List<Variable> getAddedVars();

    /**
     * @return an unmodifiable list of the constraints that have been posted to
     *         the model by this view
     */
    public List<Constraint> getAddedConstraints();

    /**
     * set the required configuration. If this implementation does not require a
     * configuration, such as having no specification in {@link
     * ViewDesc.#configURI()}, this should do nothing.
     *
     * @param conf
     *            the configuration retrieved by the core for this view, from
     *            its description annotation.
     */
    public void setConfig(ProvidedData conf);

    /** @return the problem */
    public IReconfigurationProblem getProblem();

    public default String getName() {
	return getClass().getName();
    }

}

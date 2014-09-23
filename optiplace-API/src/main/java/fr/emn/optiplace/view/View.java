package fr.emn.optiplace.view;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Parameter;

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
 * A view can not be associated to several {@link ReconfigurationProblem}s at
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

	static final Logger logger = LoggerFactory.getLogger(View.class);

	/**
	 * shortcut for {@link #getRequestedRules()}.add(cst)
	 *
	 * @param cst
	 */
	public void addRule(Rule cst);

	/**
	 * add a constraint to the problem, if not already added, and store it in the
	 * list of added constraints.
	 *
	 * @param eq
	 * the constraint to add to the problem
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

	/**
	 * @return an unmodifiable list of the variables that have been added to the
	 * model by this view
	 */
	public List<Var> getAddedVars();

	/**
	 * @return an unmodifiable list of the constraints that have been posted to
	 * the model by this view
	 */
	public List<SConstraint<? extends Var>> getAddedConstraints();

	/**
	 * set the required configuration. If this implementation does not require a
	 * configuration, such as having no specification in {@link
	 * ViewDesc.#configURI()}, this should do nothing.
	 *
	 * @param conf
	 * the configuration retrieved by the core for this view, from its description
	 * annotation.
	 */
	public void setConfig(ProvidedData conf);

	/** @return the problem */
	public ReconfigurationProblem getProblem();

	/**
	 * provides view to fulfill the dependencies.
	 *
	 * @param activatedViews
	 * a map of view name to views.
	 * @return true if all dependencies were satisfied
	 */
	default boolean setDependencies(Map<String, View> activatedViews) {
		for (Field f : getClass().getDeclaredFields()) {
			if (f.getAnnotation(Depends.class) != null) {
				System.err.println("working on field " + f.getName());
				View v = activatedViews.get(f.getType().getName());
				if (v == null) {
					System.err.println(" X can't set dependency : "
							+ f.getType().getName() + " as we have " + activatedViews);
					return false;
				}
				f.setAccessible(true);
				try {
					f.set(this, v);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.warn("", e);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * set the data used in this view. The fields annotated with {@link Parameter}
	 * are found by reflection, their object is then cast to a ProvidedDataReader
	 * which then reads the data .
	 *
	 * @param prv
	 * the provider of ViewData
	 * @return true if all the required configurations were satisfied.
	 */
	default boolean setConfs(ViewDataProvider prv) {
		for (Field f : getClass().getDeclaredFields()) {
			Parameter a = f.getAnnotation(Parameter.class);
			if (a != null) {
				System.err.println("working on field " + f.getName());
				ProvidedData d = prv.getData(a.confName());
				if (d == null && a.required()) {
					return false;
				}
				try {
					ProvidedDataReader pdr = (ProvidedDataReader) f.get(this);
					pdr.read(d);
				} catch (IllegalArgumentException | IllegalAccessException e) {
					logger.warn("", e);
					if (a.required()) {
						return false;
					}
				}
			}
		}
		return true;
	}

}

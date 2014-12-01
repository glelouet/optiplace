package fr.emn.optiplace.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import solver.constraints.Constraint;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.Variable;
import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * A view that does nothing. classes inheriting from view should implement it to
 * get a minimal set of usefull creation tools.
 * <ul>
 * <li>all variables created by a view should be added using
 * {@link #onNewVar(Var)}</li>
 * <li>all new constraints should be added to the model using
 * {@link #post(SConstraint)} instead of {@link #problem}.post()</li>
 * <li>any call to {@link #clear()} should call super.clear() and all
 * problem-relative variables should be cleaned in the clear()</li>
 * <li>any call to {@link #associate(IReconfigurationProblem)} should call
 * super.associate()</li>
 * </ul>
 *
 * @author guillaume
 */
public class EmptyView implements View {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(EmptyView.class);

	protected boolean debugVarsAndPosts = false;

	public void setDebugVarsAndPosts(boolean debug) {
		debugVarsAndPosts = debug;
	}

	protected IReconfigurationProblem problem = null;

	@Override
	public IReconfigurationProblem getProblem() {
		return problem;
	}

	@Override
	public void associate(IReconfigurationProblem rp) {
		clear();
		problem = rp;
	}

	/** remove all data added to the last {@link #problem} and cached */
	@Override
	public void clear() {
		addedConstraints.clear();
		addedVars.clear();
		problem = null;
	}

	protected LinkedHashSet<Constraint> addedConstraints = new LinkedHashSet<>();

	@Override
	public void post(Constraint cc) {
		if (addedConstraints.add(cc)) {
			if (debugVarsAndPosts) {
				logger.debug(getClass().getSimpleName() + " posted " + cc);
			}
			problem.getSolver().post(cc);
		}
	}

	@Override
	public List<Constraint> getAddedConstraints() {
		return Collections
.unmodifiableList(new ArrayList<Constraint>(
						addedConstraints));
	}

	protected ArrayList<Variable> addedVars = new ArrayList<Variable>();

	@Override
	public void onNewVar(Variable var) {
		if (debugVarsAndPosts) {
			logger.debug(getClass().getSimpleName() + " adding var "
 + var.getName());
		}
		addedVars.add(var);
	}

	@Override
	public List<Variable> getAddedVars() {
		return Collections.unmodifiableList(addedVars);
	}

	protected ArrayList<Rule> requestedRules = new ArrayList<Rule>();

	@Override
	public List<Rule> getRequestedRules() {
		return requestedRules;
	}

	@Override
	public void addRule(Rule cst) {
		requestedRules.add(cst);
	}

    protected ArrayList<AbstractStrategy<? extends Variable>> searchHeuristics = new ArrayList<>();

	@Override
    public List<AbstractStrategy<? extends Variable>> getSearchHeuristics() {
		return searchHeuristics;
	}

	protected SearchGoal searchGoal = null;

	@Override
	public SearchGoal getSearchGoal() {
		return searchGoal;
	}

	public void setSearchGoal(SearchGoal sg) {
		this.searchGoal = sg;
	}

	@Override
	public void endSolving(ActionGraph a) {
	}

	/**
	 * an empty resource array to be returned by {@link #getPackedResource()}
	 * when no resource must be packed by the solver
	 */
	private ResourceSpecification[] resourcesSpecs = {};

	@Override
	public ResourceSpecification[] getPackedResource() {
		return resourcesSpecs;
	}

	public void setResourceSpecs(ResourceSpecification... specs) {
		resourcesSpecs = specs;
	}

	@Override
	public void setConfig(ProvidedData conf) {
	}
}

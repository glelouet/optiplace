package fr.emn.optiplace.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

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
 * <li>any call to {@link #associate(ReconfigurationProblem)} should call
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

	protected ReconfigurationProblem problem = null;

	@Override
	public ReconfigurationProblem getProblem() {
		return problem;
	}

	@Override
	public void associate(ReconfigurationProblem rp) {
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

	protected LinkedHashSet<SConstraint<? extends Var>> addedConstraints = new LinkedHashSet<SConstraint<? extends Var>>();

	@Override
	public void post(SConstraint<? extends Var> eq) {
		if (addedConstraints.add(eq)) {
			if (debugVarsAndPosts) {
				logger.debug(getClass().getSimpleName() + " posted " + eq);
			}
			problem.post(eq);
		}
	}

	@Override
	public List<SConstraint<? extends Var>> getAddedConstraints() {
		return Collections
				.unmodifiableList(new ArrayList<SConstraint<? extends Var>>(
						addedConstraints));
	}

	protected ArrayList<Var> addedVars = new ArrayList<Var>();

	@Override
	public void onNewVar(Var var) {
		if (debugVarsAndPosts) {
			logger.debug(getClass().getSimpleName() + " adding var "
					+ var.pretty());
		}
		addedVars.add(var);
	}

	@Override
	public List<Var> getAddedVars() {
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

	protected ArrayList<SearchHeuristic> viewHeuristics = new ArrayList<>();

	@Override
	public List<SearchHeuristic> getViewHeuristics() {
		return viewHeuristics;
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

	/** do nothing. Overwrite if a view require config file. */
	@Override
	public void setConfigFiles(File... files) {
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
}

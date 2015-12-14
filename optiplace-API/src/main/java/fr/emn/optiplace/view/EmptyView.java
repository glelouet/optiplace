
package fr.emn.optiplace.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Stream;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.solver.choco.Bridge;
import fr.emn.optiplace.solver.choco.ConstraintHelper;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.solver.choco.VariablesManager;


/**
 * Common view behaviour. classes inheriting from view should implement it to
 * get a minimal set of useful creation tools.
 * <ul>
 * <li>all variables created by a view should be added using
 * {@link #onNewVar(Var)}</li>
 * <li>all new constraints should be added to the model using
 * {@link #post(SConstraint)} instead of {@link #problem#post(Constraint)}</li>
 * <li>any call to {@link #clear()} should call super.clear() and all
 * problem-relative variables should be cleaned in the clear()</li>
 * <li>any overwrite of {@link #associate(IReconfigurationProblem)} should call
 * {@link EmptyView#associate(IReconfigurationProblem)}</li>
 * </ul>
 *
 * @author guillaume
 */
public class EmptyView implements View {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(EmptyView.class);

	protected boolean debugVarsAndPosts = false;

	public void setDebugVarsAndPosts(boolean debug) {
		debugVarsAndPosts = debug;
	}

	public IReconfigurationProblem pb = null;

	@Override
	public IReconfigurationProblem getProblem() {
		return pb;
	}

	public VariablesManager v;

	public ConstraintHelper h;

	public Bridge b;

	public IConfiguration c;

	@Override
	public void associate(IReconfigurationProblem rp) {
		clear();
		pb = rp;
		c = rp.c();
		b = rp.b();
		v = rp.v();
		h = rp.h();
	}

	/** remove all data added to the last {@link #pb} and cached */
	@Override
	public void clear() {
		addedConstraints.clear();
		addedVars.clear();
		pb = null;
	}

	protected LinkedHashSet<Constraint> addedConstraints = new LinkedHashSet<>();

	@Override
	public void post(Constraint cc) {
		if (addedConstraints.add(cc)) {
			if (debugVarsAndPosts) {
				logger.debug(getClass().getSimpleName() + " posted " + cc);
			}
			pb.getSolver().post(cc);
		}
	}

	@Override
	public List<Constraint> getAddedConstraints() {
		return Collections.unmodifiableList(new ArrayList<Constraint>(addedConstraints));
	}

	protected ArrayList<Variable> addedVars = new ArrayList<Variable>();

	@Override
	public void onNewVar(Variable var) {
		if (debugVarsAndPosts) {
			logger.debug(getClass().getSimpleName() + " adding var " + var.getName());
		}
		addedVars.add(var);
	}

	@Override
	public List<Variable> getAddedVars() {
		return Collections.unmodifiableList(addedVars);
	}

	protected ArrayList<Rule> requestedRules = new ArrayList<Rule>();

	@Override
	public Stream<Rule> getRequestedRules() {
		return requestedRules.stream();
	}

	public ArrayList<Rule> getInternalRules() {
		return requestedRules;
	}

	@Override
	public void addRule(Rule cst) {
		requestedRules.add(cst);
	}

	protected ArrayList<AbstractStrategy<? extends Variable>> searchHeuristics = new ArrayList<>();

}

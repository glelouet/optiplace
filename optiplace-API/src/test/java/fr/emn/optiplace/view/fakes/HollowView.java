/**
 *
 */
package fr.emn.optiplace.view.fakes;

import java.util.List;
import java.util.stream.Stream;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.Rule;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.View;


/**
 * view that has no method working. used to check if the introspection mechanism
 * works
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class HollowView implements View {

	@Override
	public void associate(IReconfigurationProblem rp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Stream<Rule> getRequestedRules() {
		throw new UnsupportedOperationException();
	}

	@Override
    public List<AbstractStrategy<? extends Variable>> getSearchHeuristics() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SearchGoal getSearchGoal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void extractActions(ActionGraph actionGraph, Configuration dest) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addRule(Rule cst) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void post(Constraint eq) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onNewVar(Variable var) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Variable> getAddedVars() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Constraint> getAddedConstraints() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setConfig(ProvidedData conf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IReconfigurationProblem getProblem() {
		throw new UnsupportedOperationException();
	}

}
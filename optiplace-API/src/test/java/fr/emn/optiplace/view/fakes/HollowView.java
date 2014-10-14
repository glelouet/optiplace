/**
 *
 */
package fr.emn.optiplace.view.fakes;

import java.util.List;

import solver.constraints.Constraint;
import solver.variables.Variable;
import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.Rule;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.SearchHeuristic;
import fr.emn.optiplace.view.View;

public class HollowView implements View {

	@Override
	public void associate(IReconfigurationProblem rp) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ResourceSpecification[] getPackedResource() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Rule> getRequestedRules() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<SearchHeuristic> getSearchHeuristics() {
		throw new UnsupportedOperationException();
	}

	@Override
	public SearchGoal getSearchGoal() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void endSolving(ActionGraph actionGraph) {
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
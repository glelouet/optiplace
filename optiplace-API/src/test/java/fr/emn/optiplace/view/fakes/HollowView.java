/**
 *
 */
package fr.emn.optiplace.view.fakes;

import java.util.List;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.Rule;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.SearchHeuristic;
import fr.emn.optiplace.view.View;

public class HollowView implements View {

	@Override
	public void associate(ReconfigurationProblem rp) {
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
	public void post(SConstraint<? extends Var> eq) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void onNewVar(Var var) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Var> getAddedVars() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<SConstraint<? extends Var>> getAddedConstraints() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setConfig(ProvidedData conf) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ReconfigurationProblem getProblem() {
		throw new UnsupportedOperationException();
	}

}
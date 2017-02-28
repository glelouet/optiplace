/**
 *
 */
package fr.emn.optiplace.view.fakes;

import java.util.List;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;
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
	public List<Rule> getRules() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addRule(Rule cst) {
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
	public IReconfigurationProblem getProblem() {
		throw new UnsupportedOperationException();
	}

}
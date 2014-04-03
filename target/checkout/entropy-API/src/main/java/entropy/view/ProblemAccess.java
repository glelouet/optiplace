package entropy.view;

import entropy.solver.choco.ReconfigurationProblem;
import entropy.view.access.ConstraintsManager;
import entropy.view.access.CoreView;
import entropy.view.access.VariablesManager;

/**
 * A view is given access to the problem through this interface.<br />
 * variables are created through the {@link #variables}, while constraints are
 * created through the {@link #constraints}.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class ProblemAccess {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ProblemAccess.class);

	public ConstraintsManager constraints;

	public VariablesManager variables;

	public CoreView core;

	public ReconfigurationProblem solver = null;

	public ProblemAccess solver(ReconfigurationProblem solver) {
		this.solver = solver;
		return this;
	}

	public ProblemAccess variables(VariablesManager vars) {
		variables = vars;
		return this;
	}

	public ProblemAccess constraints(ConstraintsManager cst) {
		constraints = cst;
		return this;
	}

}

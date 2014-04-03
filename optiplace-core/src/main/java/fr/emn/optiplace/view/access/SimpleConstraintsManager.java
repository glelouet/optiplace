package fr.emn.optiplace.view.access;

import java.util.LinkedHashSet;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * delegate the calls to a {@link ReconfigurationProblem}
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class SimpleConstraintsManager implements ConstraintsManager {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimpleConstraintsManager.class);

	ReconfigurationProblem problem;

	protected boolean debugPosts = false;

	public void setDebugVarsAndPosts(boolean debug) {
		debugPosts = debug;
	}

	protected LinkedHashSet<SConstraint<? extends Var>> addedConstraints = new LinkedHashSet<SConstraint<? extends Var>>();

	@Override
	public void post(SConstraint<? extends Var> eq) {
		if (addedConstraints.add(eq)) {
			problem.post(eq);
			if (debugPosts) {
				logger.debug(getClass().getSimpleName() + " posted " + eq);
			}
		}
	}

	@Override
	public void linear(IntDomainVar y, int a, IntDomainVar x, int b) {
		// TODO Auto-generated method stub
	}

	@Override
	public void linear(IntDomainVar y, int a, int b, IntDomainVar... vars) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void linear(IntDomainVar y, int[] a, IntDomainVar[] x, int b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void scalar(IntDomainVar[] y, int[] a, IntDomainVar[] x, int[] b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void mult(IntDomainVar z, int a, IntDomainVar x, int b,
			IntDomainVar y, int c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void mult(IntDomainVar z, IntDomainVar x, IntDomainVar y) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void geq(IntDomainVar x, IntDomainVar y) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}
}

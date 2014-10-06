package fr.emn.optiplace.view.access;

import java.util.LinkedHashSet;

import solver.constraints.Constraint;
import solver.variables.IntVar;
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

	protected LinkedHashSet<Constraint> addedConstraints = new LinkedHashSet<Constraint>();

	@Override
	public void post(Constraint eq) {
		if (addedConstraints.add(eq)) {
			problem.getSolver().post(eq);
			if (debugPosts) {
				logger.debug(getClass().getSimpleName() + " posted " + eq);
			}
		}
	}

	@Override
	public void linear(IntVar y, int a, IntVar x, int b) {
		// TODO Auto-generated method stub
	}

	@Override
	public void linear(IntVar y, int a, int b, IntVar... vars) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void linear(IntVar y, int[] a, IntVar[] x, int b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void scalar(IntVar[] y, int[] a, IntVar[] x, int[] b) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void mult(IntVar z, int a, IntVar x, int b,
			IntVar y, int c) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void mult(IntVar z, IntVar x, IntVar y) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}

	@Override
	public void geq(IntVar x, IntVar y) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this !");
	}
}

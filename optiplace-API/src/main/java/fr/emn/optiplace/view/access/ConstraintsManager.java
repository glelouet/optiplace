package fr.emn.optiplace.view.access;

import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.Var;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * grant access to the management of constraints in a view
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public interface ConstraintsManager {

	/**
	 * add a constraint to the problem, if not already added, and store it in
	 * the list of added constraints.
	 * 
	 * @param eq
	 *            the constraint to add to the problem
	 */
	public void post(SConstraint<? extends Var> eq);

	/**
	 * post a new constraint specifying y=a⋅x+b
	 * 
	 * @param y
	 *            a variable
	 * @param a
	 *            a constant int
	 * @param x
	 *            a variable
	 * @param b
	 *            a constant int
	 */
	public void linear(IntDomainVar y, int a, IntDomainVar x, int b);

	/**
	 * post a new constraint, y=b+a⋅sum(vars).<br />
	 * Multi-dimension form of
	 * {@link #linear(IntDomainVar, int, IntDomainVar, int)}
	 * 
	 * @param y
	 * @param a
	 * @param b
	 * @param vars
	 */
	public void linear(IntDomainVar y, int a, int b, IntDomainVar... vars);

	/**
	 * post a constraint specifying y=sum(a[i]⋅x[i]) +b
	 * 
	 * @param y
	 * @param a
	 * @param x
	 * @param b
	 */
	public void linear(IntDomainVar y, int[] a, IntDomainVar[] x, int b);

	/**
	 * post a new constraint specifying y[i]=a[i]⋅x[i]+b[i].<br />
	 * scalar version of {@link #linear(IntDomainVar, int, IntDomainVar, int)}
	 * 
	 * @param y
	 * @param a
	 * @param x
	 * @param b
	 */
	public void scalar(IntDomainVar[] y, int[] a, IntDomainVar[] x, int[] b);

	/**
	 * post a new constraint, z=a(x-b)(y-c)
	 * 
	 * @param z
	 * @param a
	 * @param x
	 * @param b
	 * @param y
	 * @param c
	 */
	public void mult(IntDomainVar z, int a, IntDomainVar x, int b,
			IntDomainVar y, int c);

	/**
	 * shortcut for {@link #mult(IntDomainVar, 0, IntDomainVar, 0, IntDomainVar,
	 * 0)}
	 * 
	 * @param z
	 * @param x
	 * @param y
	 */
	public void mult(IntDomainVar z, IntDomainVar x, IntDomainVar y);

	/**
	 * post a new constraint specifying x≥y
	 * 
	 * @param x
	 * @param y
	 */
	public void geq(IntDomainVar x, IntDomainVar y);

}

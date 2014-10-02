package fr.emn.optiplace.view.access;

import solver.constraints.SConstraint;
import solver.variables.Var;
import solver.variables.IntVar;

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
	public void linear(IntVar y, int a, IntVar x, int b);

	/**
	 * post a new constraint, y=b+a⋅sum(vars).<br />
	 * Multi-dimension form of
	 * {@link #linear(IntVar, int, IntVar, int)}
	 * 
	 * @param y
	 * @param a
	 * @param b
	 * @param vars
	 */
	public void linear(IntVar y, int a, int b, IntVar... vars);

	/**
	 * post a constraint specifying y=sum(a[i]⋅x[i]) +b
	 * 
	 * @param y
	 * @param a
	 * @param x
	 * @param b
	 */
	public void linear(IntVar y, int[] a, IntVar[] x, int b);

	/**
	 * post a new constraint specifying y[i]=a[i]⋅x[i]+b[i].<br />
	 * scalar version of {@link #linear(IntVar, int, IntVar, int)}
	 * 
	 * @param y
	 * @param a
	 * @param x
	 * @param b
	 */
	public void scalar(IntVar[] y, int[] a, IntVar[] x, int[] b);

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
	public void mult(IntVar z, int a, IntVar x, int b,
			IntVar y, int c);

	/**
	 * shortcut for {@link #mult(IntVar, 0, IntVar, 0, IntVar,
	 * 0)}
	 * 
	 * @param z
	 * @param x
	 * @param y
	 */
	public void mult(IntVar z, IntVar x, IntVar y);

	/**
	 * post a new constraint specifying x≥y
	 * 
	 * @param x
	 * @param y
	 */
	public void geq(IntVar x, IntVar y);

}

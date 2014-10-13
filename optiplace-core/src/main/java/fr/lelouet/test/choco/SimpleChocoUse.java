/**
 *
 */
package fr.lelouet.test.choco;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.exception.ContradictionException;
import solver.objective.ObjectiveManager;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.solution.Solution;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class SimpleChocoUse {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimpleChocoUse.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		for (int loadFactor = 1; loadFactor < 10; loadFactor++) {
			for (int nbelems = 256; nbelems <= 2048; nbelems *= 1.414213562) {
				Solver solver = makeLinear(nbelems, nbelems * loadFactor);
				solver.findAllSolutions();
				Solution s = solver.getSolutionRecorder().getLastSolution();
				try {
					s.restore();
				} catch (ContradictionException e) {
					logger.warn("", e);
				}
				System.err.println(((IntVar) solver.getObjectiveManager()
						.getObjective()).getValue());
				// for (Variable iv : solver.getVars()) {
				// System.err.println(iv.toString() + " : " +
				// s.getIntVal((IntVar) iv));
				// }
				System.err.println(" " + nbelems + " * " + loadFactor + " : "
						+ solver.getMeasures().getTimeCount() + "s, "
						+ solver.getMeasures().getSolutionCount() + " sols");
			}
		}
	}

	/**
	 * makes a problem of n variable, each having a range of 0 to n-1+loose <br />
	 * They are constrained as all different, and each of them being lower than
	 * the next one. ie var[0]&ltvar[1] , etc.
	 *
	 * @param nbVar
	 *            the number of variables ( &gt 0) to use
	 * @param loose
	 *            the added range of the variables
	 * @return a new Solver with those specifications
	 */
	public static Solver makeLinear(int nbVar, int loose) {
		Solver solver = new Solver("linear difference");
		IntVar[] vars = new IntVar[nbVar];
		IntVar[] diffs = new IntVar[nbVar + 1];
		for (int i = 0; i < nbVar; i++) {
			vars[i] = VariableFactory
					.bounded("var" + i, 0, nbVar + loose - 1, solver);
			if (i > 0) {
				solver.post(IntConstraintFactory.arithm(vars[i - 1], "<", vars[i]));
				diffs[i] = VariableFactory.bounded("diff" + i + "-" + (i - 1), 0, nbVar
						+ loose - 1, solver);
				solver.post(IntConstraintFactory.sum(new IntVar[] { diffs[i],
						vars[i - 1] }, vars[i]));
			}
		}
		diffs[0] = vars[0];
		diffs[nbVar] = VariableFactory.bounded("diffMax", 0, nbVar + loose - 1,
				solver);
		solver.post(IntConstraintFactory.sum(new IntVar[] { diffs[nbVar],
				vars[nbVar - 1] }, "=", VariableFactory.fixed(nbVar + loose, solver)));
		// solver.post(IntConstraintFactory.alldifferent(vars, "DEFAULT"));
		SearchMonitorFactory.log(solver, false, false);
		IntVar max = VariableFactory.bounded("max", 0, nbVar + loose - 1, solver);
		solver.post(IntConstraintFactory.maximum(max, diffs));
		solver.set(new ObjectiveManager<>(max, ResolutionPolicy.MINIMIZE, true));
		return solver;
	}
}

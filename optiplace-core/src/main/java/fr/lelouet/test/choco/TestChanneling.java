/**
 *
 */
package fr.lelouet.test.choco;

import java.util.Arrays;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.set.SCF;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.VF;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class TestChanneling {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(TestChanneling.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Solver s = new Solver();
		IntVar vm0 = VF.enumerated("vm0.hoster", 0, 1, s);
		IntVar vm1 = VF.enumerated("vm1.hoster", 0, 1, s);
		IntVar vm2 = VF.enumerated("vm2.hoster", 0, 1, s);
		IntVar vm3 = VF.enumerated("vm3.hoster", 0, 1, s);
		SetVar n0 = VF.set("n0.hosted", 0, 3, s);
		SetVar n1 = VF.set("n1.hosted", 0, 3, s);
		Constraint c = SCF.int_channel(new SetVar[] { n0, n1 }, new IntVar[] { vm0,
				vm1, vm2, vm3 }, 0, 0);
		System.err.println(c);
		s.post(c);
		s.findSolution();
		System.err.println(Arrays.asList(s.getVars()));
	}
}

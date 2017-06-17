
package fr.emn.optiplace.homogeneous.heuristics;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.core.heuristics.Var2ValSelector;
import fr.emn.optiplace.homogeneous.goals.PackingGoal.ElemWeighter;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * An heuristic to set the number of VMs a node hosts to 0. The Nodes are sorted
 * by their VM weight, increasing, so a node with low use will be shut first.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class NodesEmptyHeuristic extends IntStrategy {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NodesEmptyHeuristic.class);

	/**
	 * store the ranking of an IntVar var to an int val. Is a couple of (Var, val)
	 * ; comparison between two couples are made upon their respective values
	 * (c1.compareTo c2 = c1.val-c2.val)
	 *
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2015
	 *
	 */
	protected static class IntVarRanked implements Comparable<IntVarRanked> {

		IntVar var;
		int val;

		public IntVarRanked(IntVar var, int val) {
			this.var = var;
			this.val = val;
		}

		@Override
		public int compareTo(IntVarRanked o) {
			return val - o.val;
		}

		public IntVar getVar() {
			return var;
		}

		@Override
		public String toString() {
			return "[" + var + ":" + val + "]";
		}
	}

	/**
	 * redirection to super to use with the static method call
	 *
	 * @param dec
	 *
	 * @param variables
	 */
	private NodesEmptyHeuristic(Var2ValSelector sel, IntVar[] nodesSizes, DecisionOperator<IntVar> dec) {
		super(nodesSizes, sel, sel, dec);
	}

	protected ElemWeighter weight = null;

	protected NodesEmptyHeuristic(IReconfigurationProblem rp, ElemWeighter weight, DecisionOperator<IntVar> dec) {
		this(makeSelector(rp, weight), rp.nbVMsOn(), dec);
		this.weight = weight;
	}

	/**
	 * make an heuristic that tries to set the number of VMs on each node to zero.
	 * The first Node selected are those with the lowest amount of weight of their
	 * VMs
	 *
	 * @param rp
	 *          the problem to create an heuristic for
	 * @param weight
	 *          defines the weight of the VMs on a node
	 */
	public NodesEmptyHeuristic(IReconfigurationProblem rp, ElemWeighter weight) {
		this(rp, weight, DecisionOperatorFactory.makeIntEq());
	}

	/**
	 * make a list of the nodes ordered by lowest weight first, in order to set
	 * their nb of VMs to 0.
	 *
	 * @param rp
	 *          the problem from which to extract the data
	 * @param weighter
	 *          the mapper of Nodes/VMs to their weight(actually only uses VM
	 *          weight)
	 * @return a selector that affects the first Node nb of VM to 0 when possible.
	 */
	protected static Var2ValSelector makeSelector(IReconfigurationProblem rp, ElemWeighter weighter) {
		Node[] nodes = rp.b().nodes();
		IntVarRanked[] maxNodeCost = new IntVarRanked[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			Node n = nodes[i];
			int weight = rp.getSourceConfiguration().getHosted(n).mapToInt(v -> weighter.weight(v, rp)).sum();
			maxNodeCost[i] = new IntVarRanked(rp.nbVMsOn(n), weight);
		}
		Arrays.sort(maxNodeCost);
		for (int i = 1; i < maxNodeCost.length; i++) {
			assert maxNodeCost[i - 1].val <= maxNodeCost[i].val;
			// System.err.println("IntVarRanked " + i + " is " + maxNodeCost[i]);
		}
		assert maxNodeCost[0].val <= maxNodeCost[maxNodeCost.length - 1].val;
		return new Var2ValSelector(
				Arrays.stream(maxNodeCost).map(IntVarRanked::getVar).collect(Collectors.toList()).toArray(new IntVar[0]), 0);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "(" + weight + ")";
	}
}

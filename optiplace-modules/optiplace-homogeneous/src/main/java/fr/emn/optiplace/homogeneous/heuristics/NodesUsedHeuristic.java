
package fr.emn.optiplace.homogeneous.heuristics;

import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;

import fr.emn.optiplace.homogeneous.goals.PackingGoal.ElemWeighter;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;


/**
 *
 * decides the nodes are hosting AT LEAST one VM
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class NodesUsedHeuristic extends NodesEmptyHeuristic {


	private static final long             serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger           = org.slf4j.LoggerFactory.getLogger(NodesUsedHeuristic.class);


	public NodesUsedHeuristic(IReconfigurationProblem rp, ElemWeighter weight) {
		super(rp, weight.opposite(), DecisionOperator.int_neq);
	}

}

/**
 *
 */
package fr.emn.optiplace.homogeneous.heuristics;

import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.solver.ActivatedHeuristic;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * <p>
 * This heuristic selects a Node to place VMs on. Its job is to select a Node
 * that MUST have at least one VM
 * </p>
 * <p>
 * It considers, for each Node, its number of VMs. The nodes are sorted by most
 * used first.
 * </p>
 * <p>
 * This heuristic is activated when a Node's number of VMs is instantiated ; it
 * requests that, if the nth node is the last one with an instantiated number of
 * VMs, then the n+1 th node must have at least one VM.
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2015
 *
 */
@SuppressWarnings("serial")
public class FillMostLoadedNode extends ActivatedHeuristic<IntVar> {

	protected FillMostLoadedNode(IReconfigurationProblem pb) {
		super(pb.nbVMsNodes());
	}

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(FillMostLoadedNode.class);

	@Override
	protected boolean checkActivated() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Decision<IntVar> getDecision() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}
}

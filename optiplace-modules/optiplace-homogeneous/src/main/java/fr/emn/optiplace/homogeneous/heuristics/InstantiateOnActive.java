/**
 *
 */
package fr.emn.optiplace.homogeneous.heuristics;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.fast.FastDecision;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import fr.emn.optiplace.solver.ActivatedHeuristic;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * This Heuristic is activated when a Node hosts at least one VMs, and has room
 * for other VMs.<br />
 * It places the waiting VMs on this node.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class InstantiateOnActive extends ActivatedHeuristic<IntVar> {

    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(InstantiateOnActive.class);

    boolean[] activateds;
    BoolVar[] isHosters;

    /**
     * @param variables
     * @param observed
     */
    public InstantiateOnActive(IReconfigurationProblem pb) {
	super(pb.getSourceConfiguration().getWaitings().map(pb::getNode).collect(Collectors.toList())
		.toArray(new IntVar[0]), pb.isHosters());
	isHosters = pb.isHosters();
	activateds = new boolean[isHosters.length];
	Arrays.fill(activateds, false);
    }

    @Override
    protected boolean checkActivated() {
	boolean activated = false;
	for (int i = 0; i < isHosters.length; i++) {
	    if (isHosters[i].isInstantiatedTo(1)) {
		activateds[i] = true;
		activated = true;
	    }
	}
	return activated;
    }

    @Override
    public void init() throws ContradictionException {
    }

    @Override
    public Decision<IntVar> getDecision() {
	for (int i = 0; i < activateds.length; i++) {
	    if (activateds[i]) {
		for (IntVar var : vars) {
		    if (var.contains(i) && !var.isInstantiated()) {
			FastDecision d = getFastDecision();
			d.set(var, i, DecisionOperator.int_eq);
			return d;
		    }
		}
	    }
	}
	return null;
    }
}

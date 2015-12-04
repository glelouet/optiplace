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
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.solver.ActivatedHeuristic;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;


/**
 * When a Node is already hosting VMs, we make it host its VM in the source
 * configuration.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class PackOnHoster extends ActivatedHeuristic<IntVar> {

	private static final long             serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger           = org.slf4j.LoggerFactory.getLogger(PackOnHoster.class);

	boolean[]                             activateds;
	IntVar[]                              nbVMs;
	IntVar[][]                            vmsToHost;

	/**
	 * @param variables
	 * @param observed
	 */
	public PackOnHoster(IReconfigurationProblem rp) {
		super(rp.getNodes(), rp.isHosters());
		nbVMs = rp.nbVMsNodes();
		activateds = new boolean[nbVMs.length];
		Arrays.fill(activateds, false);
		vmsToHost = new IntVar[nbVMs.length][];
		for (int i = 0; i < nbVMs.length; i++) {
			vmsToHost[i] = rp.getSourceConfiguration().getHosted(rp.b().node(i)).map(rp::getNode).collect(Collectors.toList())
					.toArray(new IntVar[0]);
		}
	}

	@Override
	protected boolean checkActivated() {
		boolean activated = false;
		for (int i = 0; i < nbVMs.length; i++) {
			if (!nbVMs[i].isInstantiated() && !nbVMs[i].contains(0)) {
				activateds[i] = true;
				activated = true;
			}
		}
		return activated;
	}

	@Override
	public void init() throws ContradictionException {}

	@Override
	public Decision<IntVar> getDecision() {
		for (int i = 0; i < activateds.length; i++) {
			if (activateds[i]) {
				for (IntVar v : vmsToHost[i]) {
					if (v.contains(i) && !v.isInstantiated()) {
						FastDecision ret = getFastDecision();
						ret.set(v, i, DecisionOperator.int_eq);
						return ret;
					}
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}

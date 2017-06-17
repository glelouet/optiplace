package fr.emn.optiplace.core.packers;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.resources.ResourceLoad;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class DefaultPacker implements ChocoResourcePacker {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultPacker.class);

	@Override
	public List<Constraint> pack(IntVar[] binAssign, ResourceLoad... resourceUse) {
		ArrayList<Constraint> res = new ArrayList<>();
		if (binAssign == null || binAssign.length == 0) {
			return res;
		}
		// at least one VM can be set to non-running ?
		boolean hasNonRunning = false;
		Model m = binAssign[0].getModel();
		for (IntVar i : binAssign) {
			if (i.getLB() < 0) {
				hasNonRunning = true;
				break;
			}
		}
		for (ResourceLoad ru : resourceUse) {
			IntVar[] nodesUses = ru.getNodesLoad();
			if (hasNonRunning) {
				IntVar nonRunningNode = m.intVar("res_" + ru.toString() + "_nonrunningload", 0, ru.getTotalVMLoads(), true);
				IntVar[] newNodesUses = new IntVar[nodesUses.length + 1];
				newNodesUses[0] = nonRunningNode;
				for (int i = 1; i < newNodesUses.length; i++) {
					newNodesUses[i] = nodesUses[i - 1];
				}
				nodesUses = newNodesUses;
			}
			if (ru.isAdditionalUse()) {
				// if the resource has additional use for nodes : use(node n) = use0 +
				// sum(vm v on n) (use(vm)) . So we need to add constant value to the
				// standard bin packing for nodes with non-null additional use.
				IntVar[] sumElems = new IntVar[nodesUses.length];
				for (int i = 0; i < nodesUses.length; i++) {
					if (hasNonRunning && i == 0) {
						sumElems[i] = nodesUses[i];
					} else {
						int additionallUse = ru.getAdditionalUse()[hasNonRunning ? i - 1 : i];

						sumElems[i] = nodesUses[i];
						if (additionallUse != 0) {
							sumElems[i] = m.intVar(nodesUses[i].getName(), nodesUses[i].getLB(), nodesUses[i].getUB(),
									!nodesUses[i].hasEnumeratedDomain());
						}
					}
				}
				res.add(m.binPacking(binAssign, ru.getVMsLoads(), sumElems, hasNonRunning ? -1 : 0));
				for (int i = 0; i < nodesUses.length; i++) {
					if (sumElems[i] != nodesUses[i]) {
						res.add(m.arithm(nodesUses[i], "=", sumElems[i], "+", ru.getAdditionalUse()[i]));
					}
				}
			} else {
				res.add(m.binPacking(binAssign, ru.getVMsLoads(), nodesUses, hasNonRunning ? -1 : 0));
			}
		}
		return res;
	}
}

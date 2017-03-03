package fr.emn.optiplace.core.packers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

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
		// at least one VM can be set to non-running ?
		boolean hasNonRunning = false;
		for (IntVar i : binAssign) {
			if (i.getLB() < 0) {
				hasNonRunning = true;
				break;
			}
		}
		for (ResourceLoad ru : resourceUse) {
			IntVar[] nodesUses = ru.getNodesLoad();
			if (hasNonRunning) {
				IntVar nonRunningNode = VF.bounded("res_" + ru.toString() + "_nonrunningload", 0, ru.getTotalVMLoads(),
						binAssign[0].getSolver());
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
						sumElems[i] = ru.getAdditionalUse()[hasNonRunning ? i - 1 : i] == 0 ? nodesUses[i]
								: nodesUses[i].duplicate();
					}
				}
				res.addAll(Arrays.asList(ICF.bin_packing(binAssign, ru.getVMsLoads(), sumElems, hasNonRunning ? -1 : 0)));
				for (int i = 0; i < nodesUses.length; i++) {
					if (sumElems[i] != nodesUses[i]) {
						res.add(ICF.arithm(nodesUses[i], "=", sumElems[i], "+", ru.getAdditionalUse()[i]));
					}
				}
			} else {
				res.addAll(Arrays.asList(ICF.bin_packing(binAssign, ru.getVMsLoads(), nodesUses, hasNonRunning ? -1 : 0)));
			}
		}
		return res;
	}
}

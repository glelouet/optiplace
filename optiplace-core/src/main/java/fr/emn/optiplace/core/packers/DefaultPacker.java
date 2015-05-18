package fr.emn.optiplace.core.packers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.center.configuration.resources.ResourceUse;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class DefaultPacker implements ChocoResourcePacker {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DefaultPacker.class);

	@Override
	public List<Constraint> pack(IntVar[] binAssign, ResourceUse... resourceUse) {
		ArrayList<Constraint> res = new ArrayList<>();
		for (ResourceUse ru : resourceUse) {
			IntVar[] nodesUses = ru.getNodesUse();
			if (ru.isAdditionalUse()) {
				// if the resource has additional use for nodes : use(node n) = use0 +
				// sum(vm v on n) (use(vm)) . So we need to add constant value to the
				// standard bin packing for nodes with non-null additional use.
				IntVar[] sumElems = new IntVar[nodesUses.length];
				for (int i = 0; i < nodesUses.length; i++) {
					sumElems[i] = ru.getAdditionalUse()[i] == 0 ? nodesUses[i] : nodesUses[i].duplicate();
				}
				res.addAll(Arrays.asList(ICF.bin_packing(binAssign, ru.getVMsUses(), sumElems, 0)));
				for (int i = 0; i < nodesUses.length; i++) {
					if (sumElems[i] != nodesUses[i]) {
						res.add(ICF.arithm(nodesUses[i], "=", sumElems[i], "+", ru.getAdditionalUse()[i]));
					}
				}
			} else {
				res.addAll(Arrays.asList(ICF.bin_packing(binAssign, ru.getVMsUses(), nodesUses, 0)));
			}
		}
		return res;
	}
}

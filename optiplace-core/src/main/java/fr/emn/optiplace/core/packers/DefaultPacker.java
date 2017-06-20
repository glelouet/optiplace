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
			IntVar[] computersUses = ru.getComputersLoad();
			if (hasNonRunning) {
				IntVar nonRunningComputer = m.intVar("res_" + ru.toString() + "_nonrunningload", 0, ru.getTotalVMLoads(), true);
				IntVar[] newComputersUses = new IntVar[computersUses.length + 1];
				newComputersUses[0] = nonRunningComputer;
				for (int i = 1; i < newComputersUses.length; i++) {
					newComputersUses[i] = computersUses[i - 1];
				}
				computersUses = newComputersUses;
			}
			if (ru.isAdditionalUse()) {
				// if the resource has additional use for computers : use(computer n) =
				// use0 +
				// sum(vm v on n) (use(vm)) . So we need to add constant value to the
				// standard bin packing for computers with non-null additional use.
				IntVar[] sumElems = new IntVar[computersUses.length];
				for (int i = 0; i < computersUses.length; i++) {
					if (hasNonRunning && i == 0) {
						sumElems[i] = computersUses[i];
					} else {
						int additionallUse = ru.getAdditionalUse()[hasNonRunning ? i - 1 : i];

						sumElems[i] = computersUses[i];
						if (additionallUse != 0) {
							sumElems[i] = m.intVar(computersUses[i].getName(), computersUses[i].getLB(), computersUses[i].getUB(),
									!computersUses[i].hasEnumeratedDomain());
						}
					}
				}
				res.add(m.binPacking(binAssign, ru.getVMsLoads(), sumElems, hasNonRunning ? -1 : 0));
				for (int i = 0; i < computersUses.length; i++) {
					if (sumElems[i] != computersUses[i]) {
						res.add(m.arithm(computersUses[i], "=", sumElems[i], "+", ru.getAdditionalUse()[i]));
					}
				}
			} else {
				res.add(m.binPacking(binAssign, ru.getVMsLoads(), computersUses, hasNonRunning ? -1 : 0));
			}
		}
		return res;
	}
}

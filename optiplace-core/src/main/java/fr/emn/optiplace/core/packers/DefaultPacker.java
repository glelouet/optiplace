package fr.emn.optiplace.core.packers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.variables.IntVar;
import fr.emn.optiplace.center.configuration.resources.ResourceUse;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class DefaultPacker implements ChocoResourcePacker {
  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(DefaultPacker.class);

  @Override
	public List<Constraint> pack(IntVar[] binAssign,
      ResourceUse... resourceUse) {
    ArrayList<Constraint> res = new ArrayList<>();
    for (ResourceUse ru : resourceUse) {
			res.addAll(Arrays.asList(ICF.bin_packing(binAssign, ru.getVMsUses(),
					ru.getNodesUse(), 0)));
    }
		return res;
  }
}

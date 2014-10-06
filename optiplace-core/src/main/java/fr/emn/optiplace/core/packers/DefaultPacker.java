package fr.emn.optiplace.core.packers;

import java.util.ArrayList;

import solver.constraints.Constraint;
import solver.constraints.ICF;
import solver.variables.IntVar;
import fr.emn.optiplace.configuration.resources.ResourceUse;
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
  public Constraint[] pack(IntVar[] binAssign,
      ResourceUse... resourceUse) {
    ArrayList<Constraint> res = new ArrayList<>();
    for (ResourceUse ru : resourceUse) {
      ICF.bin_packing(binAssign, ru.getVMsUses(), ru.getNodesUse(), 0);
    }
    return res.toArray(new Constraint[] {});
  }
}

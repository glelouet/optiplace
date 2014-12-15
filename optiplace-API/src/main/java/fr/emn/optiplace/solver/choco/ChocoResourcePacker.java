package fr.emn.optiplace.solver.choco;

import java.util.List;

import solver.constraints.Constraint;
import solver.variables.IntVar;
import fr.emn.optiplace.center.configuration.resources.ResourceUse;

public interface ChocoResourcePacker {

  /**
   * create a constraint to add to the solver
   *
   * @param environment
   *            the solver's environment
   * @param binAssign
   *            the affectation of the elements in their bins
   * @param resourceUse
   *            specifications of the usage of the elements and capacities of
   *            the bins
   * @return
   */
	public List<Constraint> pack(IntVar[] binAssign, ResourceUse... resourceUse);

}

package fr.emn.optiplace.core.heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import solver.search.strategy.ISF;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.Variable;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.SearchHeuristic;

/**
 * An heuristic to place the VMs on their hosters in the source configuration.
 * Assign the vms to their hoster, or forbid them.The vms are selected in
 * decreasing order of mem consumption by default, or by the provided comparator
 * in the constructor.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class StickVMsHeuristic implements SearchHeuristic {

  /** the comparator to define in which order to assign the vms */
  private final Comparator<VM> cmp;

  public StickVMsHeuristic(Comparator<VM> cmp) {
    this.cmp = cmp;
  }

  /**
   * @param spec
   * the resource specification to order the vms by.
   */
  public StickVMsHeuristic(final ResourceSpecification spec) {
    cmp = spec.makeVMComparator(false);
  }

  /** @return the cmp */
  public Comparator<VM> getCmp() {
    return cmp;
  }

  @Override
  public List<AbstractStrategy<? extends Variable>> getHeuristics(
      ReconfigurationProblem rp) {
    List<AbstractStrategy<? extends Variable>> ret = new ArrayList<>();
    VM[] vms = rp.vms().clone();
    if (vms == null || vms.length == 0) {
      return ret;
    }
    Arrays.sort(vms, cmp);
    int[] correspondingNodes = new int[vms.length];
    IntVar[] sortedHosters = new IntVar[vms.length];
    for (int i = 0; i < vms.length; i++) {
      correspondingNodes[i] = rp.node(rp.getSourceConfiguration().getLocation(
          vms[i]));
      sortedHosters[i] = rp.host(vms[i]);
    }
    Var2ValSelector heuristic = new Var2ValSelector(sortedHosters,
        correspondingNodes);
    ret.add(ISF.custom(heuristic, heuristic, sortedHosters));
    return ret;
  }

  @Override
  public String toString() {
    return "StickVMsHeuristic(" + cmp + ")";
  }

}

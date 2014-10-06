package fr.emn.optiplace.core.heuristics;

import java.util.Arrays;

import solver.search.strategy.selectors.IntValueSelector;
import solver.search.strategy.selectors.VariableSelector;
import solver.variables.IntVar;

/** select vars in the given order and try to assign them to their provided
 * value.
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013 */
public class Var2ValSelector implements IntValueSelector,
VariableSelector<IntVar> {

  private static final long serialVersionUID = 1L;

  private final IntVar[] vars;
  private final int[] vals;

  public Var2ValSelector(IntVar[] vars, int[] vals) {
    assert vars.length == vals.length;
    this.vars = vars;
    this.vals = vals;
  }

  int val = 0;

  @Override
  public int selectValue(IntVar var) {
    // TODO can we use val instead ? it is set to the last value possible on
    // getVariable()
    for (int i = 0; i < vars.length; i++) {
      if (vars[i] == var) {
        return vals[i];
      }
    }
    throw new UnsupportedOperationException("can not set variable " + var
        + ", know vars are " + Arrays.asList(vars));
  }

  @Override
  public IntVar getVariable(IntVar[] variables) {
    assert variables == vars;
    for (int i = 0; i < vars.length; i++) {
      if (vars[i].contains(vals[i])) {
        val = vals[i];
        return vars[i];
      }
    }
    return null;
  }

}

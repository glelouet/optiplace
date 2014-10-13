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
  private final int val;

  /** vars[i] must be set to vals[i].
   * @param vars variables to try to instantiate to given value
   * @param vals at least as many vals as in vars */
  public Var2ValSelector(IntVar[] vars, int[] vals) {
    this.vars = vars;
    this.vals = vals;
    val = 0;
    assert vars.length <= vals.length;
  }

  /** all variables are set to the same value. */
  public Var2ValSelector(IntVar[] vars, int val) {
    this.vars = vars;
    vals = null;
    this.val = val;
  }

  int nextVal = 0;

  @Override
  public int selectValue(IntVar var) {
    if (vars == null) {
      return val;
    }
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
		// assert variables == vars : "expected " + Arrays.asList(vars) + ", got "
		// + Arrays.asList(variables);
    for (int i = 0; i < vars.length; i++) {
			if (!vars[i].isInstantiated()
					&& (vals == null && vars[i].contains(val) || vars[i]
							.contains(vals[i]))) {
        return vars[i];
      }
    }
    return null;
  }

}

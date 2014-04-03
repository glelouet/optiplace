package entropy.core.heuristics;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.solver.branch.VarSelector;
import choco.kernel.solver.search.ValSelector;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * select vars in the given order and try to assign them to their provided
 * value.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class Var2ValSelector
		implements
			VarSelector<IntDomainVar>,
			ValSelector<IntDomainVar> {

	private final IntDomainVar[] vars;
	private final int[] vals;
	private final IStateInt last;

	public Var2ValSelector(IEnvironment env, IntDomainVar[] vars, int[] vals) {
		assert vars.length == vals.length;
		last = env.makeInt(-1);
		this.vars = vars;
		this.vals = vals;
	}

	@Override
	public int getBestVal(IntDomainVar x) {
		return vals[last.get()];
	}

	@Override
	public IntDomainVar selectVar() {
		for (int i = last.get() + 1; i < vars.length; i++) {
			if (!vars[i].isInstantiated()
					&& vars[i].canBeInstantiatedTo(vals[i])) {
				last.set(i);
				return vars[i];
			}
		}
		return null;
	}

}

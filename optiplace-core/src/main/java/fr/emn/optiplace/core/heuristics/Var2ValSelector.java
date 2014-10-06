package fr.emn.optiplace.core.heuristics;

import memory.IEnvironment;
import memory.IStateInt;
import solver.branch.VarSelector;
import solver.search.ValSelector;
import solver.variables.IntVar;

/**
 * select vars in the given order and try to assign them to their provided
 * value.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class Var2ValSelector
		implements
			VarSelector<IntVar>,
			ValSelector<IntVar> {

	private final IntVar[] vars;
	private final int[] vals;
	private final IStateInt last;

	public Var2ValSelector(IEnvironment env, IntVar[] vars, int[] vals) {
		assert vars.length == vals.length;
		last = env.makeInt(-1);
		this.vars = vars;
		this.vals = vals;
	}

	@Override
	public int getBestVal(IntVar x) {
		return vals[last.get()];
	}

	@Override
	public IntVar selectVar() {
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

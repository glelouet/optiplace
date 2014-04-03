/**
 *
 */
package fr.emn.optiplace.core.choco.reified;

import choco.cp.solver.variables.integer.IntVarEvent;
import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.constraints.integer.AbstractBinIntSConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * a constraint specifying that if x is true, then y is also true.<br />
 * Implemented using NOT(x) OR y is true.<br />
 * The values are considered INTEGERS, meaning the check for true is val!=0 and
 * not val==1
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class IntImplies extends AbstractBinIntSConstraint {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(IntImplies.class);

	/**
	 * creates a new constraint specifying x0 implies x1.<br />
	 * x0 and x1 can be integer variables, as false means 0 in integer.
	 * 
	 * @param x0
	 *            boolean value.
	 * @param x1
	 *            boolean value.
	 */
	public IntImplies(IntDomainVar x0, IntDomainVar x1) {
		super(x0, x1);
	}

	@Override
	public int getFilteredEventMask(int idx) {
		return IntVarEvent.INSTINT_MASK;
	}

	@Override
	public void propagate() throws ContradictionException {
		if (!v0.fastCanBeInstantiatedTo(0)) {
			v1.removeVal(0, this, false);
		}
		if (v1.isInstantiatedTo(0)) {
			v0.instantiate(0, this, false);
		}
	}

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if (idx == 0 && !v0.fastCanBeInstantiatedTo(0)) {
			v1.removeVal(0, this, false);
		} else {
			if (v1.getVal() == 0) {
				v0.instantiate(0, this, false);
			}
		}
	}

	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
	}

	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {
	}

	@Override
	public void awakeOnRem(int varIdx, int val) throws ContradictionException {
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		return tuple[0] == 0 || tuple[1] != 0;
	}

	@Override
	public Boolean isEntailed() {
		if (v0.isInstantiatedTo(0) || !v1.fastCanBeInstantiatedTo(0)) {
			return Boolean.TRUE;
		} else if (!v0.fastCanBeInstantiatedTo(0) && v1.isInstantiatedTo(0)) {
			return Boolean.FALSE;
		} else {
			return null;
		}
	}
}

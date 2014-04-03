package fr.emn.optiplace.core.choco.reified;

import choco.kernel.solver.ContradictionException;
import choco.kernel.solver.variables.integer.IntDomainVar;

/**
 * implementation of B <=> X!=CST
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class FastIFFNEQ extends FastIFFEq {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(FastIFFNEQ.class);

	/**
	 * @param b
	 *            the boolean var
	 * @param var
	 *            the var to check the value
	 * @param constante
	 *            the constante var can be equal to
	 */
	public FastIFFNEQ(IntDomainVar b, IntDomainVar var, int constante) {
		super(b, var, constante);
	}

	@Override
	public void propagate() throws ContradictionException {
		if (v0.isInstantiated()) {
			int val = v0.getVal();
			if (val == 0) {
				v1.instantiate(constante, this, false);
				setEntailed();
			} else {
				if (v1.removeVal(constante, this, false)) {
					setEntailed();
				}
			}
		}
		if (v1.isInstantiatedTo(constante)) {
			v0.instantiate(0, this, false);
			setEntailed();
		} else if (!v1.canBeInstantiatedTo(constante)) {
			v0.instantiate(1, this, false);
			setEntailed();
		}
	}

	@Override
	public void awakeOnInst(int idx) throws ContradictionException {
		if (idx == 0) {
			int val = v0.getVal();
			if (val == 1) {
				if (v1.removeVal(constante, this, false)) {
					setEntailed();
				}
			} else {
				v1.instantiate(constante, this, false);
			}
		} else {
			if (v1.isInstantiatedTo(constante)) {
				v0.instantiate(0, this, false);
			} else {
				v0.instantiate(1, this, false);
			}
		}
	}

	@Override
	public void awakeOnRem(int varIdx, int val) throws ContradictionException {
		if (varIdx == 1 && val == constante) {
			v0.instantiate(1, this, false);
		}
	}

	@Override
	public void awakeOnInf(int varIdx) throws ContradictionException {
		if (varIdx == 1) {
			if (!v1.canBeInstantiatedTo(constante)) {
				v0.instantiate(1, this, false);
				setEntailed();
			}
		}
	}

	@Override
	public void awakeOnSup(int varIdx) throws ContradictionException {
		if (varIdx == 1) {
			if (!v1.canBeInstantiatedTo(constante)) {
				v0.instantiate(1, this, false);
				setEntailed();
			}
		}
	}

	@Override
	public boolean isSatisfied(int[] tuple) {
		return tuple[0] == 0 && tuple[1] == constante || tuple[0] == 1
				&& tuple[1] != constante;
	}
}

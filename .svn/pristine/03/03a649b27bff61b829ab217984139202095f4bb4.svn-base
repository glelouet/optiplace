package fr.emn.optiplace.core.packers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.configuration.resources.ResourceUse;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;

/**
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class FastBinPacker implements ChocoResourcePacker {

	public static final FastBinPacker INSTANCE = new FastBinPacker();

	@SuppressWarnings("unchecked")
	@Override
	public SConstraint<IntDomainVar>[] pack(IEnvironment environment,
			IntDomainVar[] binAssign, ResourceUse... resourceUses) {
		ArrayList<SConstraint<IntDomainVar>> ret = new ArrayList<SConstraint<IntDomainVar>>();
		for (ResourceUse ru : resourceUses) {
			// we need to sort the VMs vy decreasing resource consumption. we
			// remove the VMs with 0 consumption BTW.
			ArrayList<IntDomainVar> sortedVMsUses = new ArrayList<IntDomainVar>();
			ArrayList<IntDomainVar> sortedVMsPos = new ArrayList<IntDomainVar>();
			IntDomainVar[] vmsUses = ru.getVMsUses();
			for (int i = 0; i < vmsUses.length; i++) {
				IntDomainVar use = vmsUses[i];
				if (use.getVal() != 0) {
					int index = insertDescreasing(sortedVMsUses, use);
					sortedVMsPos.add(index, binAssign[i]);
				}
			}
			// System.err.println("sorted VMs Usage : " + sortedVMsUses);
			FastBinPacking pack = new FastBinPacking(environment,
					ru.getNodesUse(),
					sortedVMsUses.toArray(new IntDomainVar[]{}),
					sortedVMsPos.toArray(new IntDomainVar[]{}));
			ret.add(pack);
		}
		return ret.toArray(new SConstraint[]{});
	}

	/**
	 * compare two IntDomainVars values. the result is positive IF the first is
	 * lesser than the second.<br />
	 * So this is the opposite order of a comparator. As such, I can be used in
	 * binarysearch to find in a list sorted by decreasing order.
	 * 
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
	 * 
	 */
	public static class InstantiatedDomainVarComparatorDecreasing
			implements
				Comparator<IntDomainVar> {

		public static final InstantiatedDomainVarComparatorDecreasing INSTANCE = new InstantiatedDomainVarComparatorDecreasing();

		@Override
		public int compare(IntDomainVar o1, IntDomainVar o2) {
			int ret = o2.getVal() - o1.getVal();
			return ret;
		}
	}

	/**
	 * insert a value in the arraylist to keep the decreasing order of the
	 * IntdomainVars'sup value
	 * 
	 * @param sortedUSes
	 *            the already sorted by decreasing order vars
	 * @param val
	 *            the value to add
	 * @return the index the value was added
	 */
	public static int insertDescreasing(ArrayList<IntDomainVar> sortedUSes,
			IntDomainVar val) {
		return insertDescreasing(sortedUSes, val,
				InstantiatedDomainVarComparatorDecreasing.INSTANCE);
	}

	/**
	 * insert a value in the arraylist to keep the decreasing order with regard
	 * to the comparator provided
	 * 
	 * @param <T>
	 * 
	 * @param sortedValues
	 *            the already sorted by decreasing order array of T
	 * @param newValue
	 *            the value to add
	 * @return the index the value was added
	 */
	public static <T> int insertDescreasing(ArrayList<T> sortedValues,
			T newValue, Comparator<T> cmp) {
		// binarysearch first
		int foundPoint = Collections.binarySearch(sortedValues, newValue, cmp);
		int insertPoint = foundPoint < 0 ? -1 - foundPoint : foundPoint;
		sortedValues.add(insertPoint, newValue);
		return insertPoint;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}

package fr.emn.optiplace.core.packers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import memory.IEnvironment;
import solver.constraints.SConstraint;
import solver.variables.IntVar;
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
	public SConstraint<IntVar>[] pack(IEnvironment environment,
			IntVar[] binAssign, ResourceUse... resourceUses) {
		ArrayList<SConstraint<IntVar>> ret = new ArrayList<SConstraint<IntVar>>();
		for (ResourceUse ru : resourceUses) {
			// we need to sort the VMs vy decreasing resource consumption. we
			// remove the VMs with 0 consumption BTW.
			ArrayList<IntVar> sortedVMsUses = new ArrayList<IntVar>();
			ArrayList<IntVar> sortedVMsPos = new ArrayList<IntVar>();
			IntVar[] vmsUses = ru.getVMsUses();
			for (int i = 0; i < vmsUses.length; i++) {
				IntVar use = vmsUses[i];
				if (use.getVal() != 0) {
					int index = insertDescreasing(sortedVMsUses, use);
					sortedVMsPos.add(index, binAssign[i]);
				}
			}
			FastBinPacking pack = new FastBinPacking(environment,
					ru.getNodesUse(),
					sortedVMsUses.toArray(new IntVar[]{}),
					sortedVMsPos.toArray(new IntVar[]{}));
			ret.add(pack);
		}
		return ret.toArray(new SConstraint[]{});
	}

	/**
	 * compare two IntVars values. the result is positive IF the first is
	 * lesser than the second.<br />
	 * So this is the opposite order of a comparator. As such, I can be used in
	 * binarysearch to find in a list sorted by decreasing order.
	 * 
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
	 * 
	 */
	public static class InstantiatedDomainVarComparatorDecreasing
			implements
				Comparator<IntVar> {

		public static final InstantiatedDomainVarComparatorDecreasing INSTANCE = new InstantiatedDomainVarComparatorDecreasing();

		@Override
		public int compare(IntVar o1, IntVar o2) {
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
	public static int insertDescreasing(ArrayList<IntVar> sortedUSes,
			IntVar val) {
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

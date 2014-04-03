/**
 *
 */
package fr.emn.optiplace.core.packers;

import java.util.ArrayList;

import choco.kernel.memory.IEnvironment;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.variables.integer.IntDomainVar;
import fr.emn.optiplace.configuration.resources.ResourceUse;

/**
 * packer using the multipacking instead of the fastbinpacking.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class FastMultiBinPacker extends FastBinPacker {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(FastMultiBinPacker.class);

	public static final FastMultiBinPacker INSTANCE = new FastMultiBinPacker();

	@SuppressWarnings("unchecked")
	@Override
	public SConstraint<IntDomainVar>[] pack(IEnvironment environment,
			IntDomainVar[] binAssign, ResourceUse... resourceUse) {
		int nbVMs = binAssign != null ? binAssign.length : 0;
		int nbNodes = -1;
		for (ResourceUse element : resourceUse) {
			assert element.getVMsUses().length == nbVMs;
			if (nbNodes == -1) {
				nbNodes = element.getNodesUse().length;
			}
			assert element.getNodesUse().length == nbNodes;
		}
		if (resourceUse.length == 0) {
			return new SConstraint[]{};
		}
		if (resourceUse.length == 1) {
			return super.pack(environment, binAssign, resourceUse);
		}
		IntDomainVar[][] nodesUses = new IntDomainVar[resourceUse.length][];
		for (int i = 0; i < resourceUse.length; i++) {
			nodesUses[i] = resourceUse[i].getNodesUse();
		}

		// we sort the args by the first resource Use of the elems
		ArrayList<IntDomainVar> sortedHosters = new ArrayList<IntDomainVar>();
		ArrayList<IntDomainVar>[] sortedSizes = new ArrayList[resourceUse.length];
		for (int i = 0; i < resourceUse.length; i++) {
			sortedSizes[i] = new ArrayList<IntDomainVar>();
		}
		// for each vm in the first resource Use : sort it, and propagate its
		// position to the other resources
		for (int i = 0; i < nbVMs; i++) {
			int pos = FastBinPacker.insertDescreasing(sortedSizes[0],
					resourceUse[0].getVMsUses()[i]);
			sortedHosters.add(pos, binAssign[i]);
			for (int j = 1; j < resourceUse.length; j++) {
				sortedSizes[j].add(pos, resourceUse[j].getVMsUses()[i]);
				assert sortedSizes[j].size() == i + 1;
			}
		}

		int[][] sizes = new int[resourceUse.length][];
		for (int i = 0; i < resourceUse.length; i++) {
			sizes[i] = new int[nbVMs];
			for (int j = 0; j < nbVMs; j++) {
				sizes[i][j] = sortedSizes[i].get(j).getVal();
			}
		}
		IntDomainVar[] bins = sortedHosters.toArray(new IntDomainVar[]{});
		return new SConstraint[]{new FastMultiBinPacking(environment,
				nodesUses, sizes, bins)};
	}
}

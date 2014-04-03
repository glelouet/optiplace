package entropy.core.heuristics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import choco.cp.solver.search.integer.branching.AssignOrForbidIntVarVal;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.VirtualMachine;
import entropy.solver.choco.ReconfigurationProblem;
import entropy.view.SearchHeuristic;

/**
 * An heuristic to place the VMs on their hosters in the source configuration.
 * Assign the vms to their hoster, or forbid them.The vms are selected in
 * increasing order of CPU*mem consumption by default, or by the provided
 * comparator in the constructor.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class StickVMsHeuristic implements SearchHeuristic {

	/**
	 * comparator that returns >0 iff the value of its first arg's CPU⋅Mem is
	 * &gt; the second arg's CPU⋅Mem <br />
	 * Use it to sort an array of {@link VirtualMachine} by increasing order of
	 * CPU⋅Mem consumption
	 */
	public static final Comparator<VirtualMachine> VMS_BY_CPUTIMESMEM_INCREASING = new Comparator<VirtualMachine>() {
		@Override
		public int compare(VirtualMachine o1, VirtualMachine o2) {
			return o1.getCPUConsumption() * o1.getMemoryConsumption()
					- o2.getCPUConsumption() * o2.getMemoryConsumption();
		}
	};

	/** the comparator to define in which order to assign the vms */
	private Comparator<VirtualMachine> cmp = VMS_BY_CPUTIMESMEM_INCREASING;

	public StickVMsHeuristic(Comparator<VirtualMachine> cmp) {
		this.cmp = cmp;
	}

	public StickVMsHeuristic() {
	}

	/** @return the cmp */
	public Comparator<VirtualMachine> getCmp() {
		return cmp;
	}

	/**
	 * @param cmp
	 *            the cmp to set
	 */
	public void setCmp(Comparator<VirtualMachine> cmp) {
		this.cmp = cmp;
	}

	@Override
	public List<AbstractIntBranchingStrategy> getHeuristics(
			ReconfigurationProblem rp) {
		List<AbstractIntBranchingStrategy> ret = new ArrayList<AbstractIntBranchingStrategy>();
		VirtualMachine[] vms = rp.vms().clone();
		if (vms == null || vms.length == 0) {
			return ret;
		}
		Arrays.sort(vms, cmp);
		int[] correspondingNodes = new int[vms.length];
		IntDomainVar[] sortedHosters = new IntDomainVar[vms.length];
		for (int i = 0; i < vms.length; i++) {
			correspondingNodes[i] = rp.node(rp.getSourceConfiguration()
					.getLocation(vms[i]));
			sortedHosters[i] = rp.host(vms[i]);
		}
		Var2ValSelector heuristic = new Var2ValSelector(rp.getEnvironment(),
				sortedHosters, correspondingNodes);
		ret.add(new AssignOrForbidIntVarVal(heuristic, heuristic));
		return ret;
	}

	protected ArrayList<VirtualMachine> findMoveableVMs(
			ReconfigurationProblem rp) {
		ArrayList<VirtualMachine> ret = new ArrayList<VirtualMachine>(
				rp.getFutureRunnings());
		ret.addAll(rp.getFutureSleepings());
		ret.removeAll(rp.getSourceConfiguration().getWaitings());
		return ret;
	}

}

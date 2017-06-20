
package fr.emn.optiplace.configuration.resources;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * <p>
 * The link between a {@link ResourceSpecification}, which maps VM and Computers
 * to their use and capacity, and a {@link IReconfigurationProblem} which has
 * indexed VM and Computers.<br />
 * Store the data related to the specifications in the problem
 * </p>
 * <p>
 * must be constructed with its {@link ResourceSpecification}, then
 * {@link #associate(IReconfigurationProblem)} to the problem.<br />
 * This is then indexed by its specifications' type in the problem's
 * {@link ReconfigurationProblem.#getUse(String)},
 * {@link ReconfigurationProblem.#getUses()} and
 * {@link ReconfigurationProblem.#getHandlers()}
 * </p>
 * <p>
 * The actual IntVar are stored in the #resourceLoad
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class ResourceHandler {

	private final ResourceSpecification specs;

	/**
	 * @return the internal resource specifications
	 */
	public ResourceSpecification getSpecs() {
		return specs;
	}

	protected IntVar[] locationLoadsByIndex = null;
	protected int minVMUse = Integer.MAX_VALUE;
	protected int maxVMUse = Integer.MAX_VALUE;
	protected int minComputerCapa = Integer.MAX_VALUE;
	protected int maxComputerCapa = Integer.MIN_VALUE;
	protected int[] nodesCapacities = null;
	protected int[] vmsLoads = null;
	protected IReconfigurationProblem associatedPb = null;

	public ResourceHandler(ResourceSpecification specs) {
		assert specs != null;
		this.specs = specs;
	}

	/**
	 * stores the actual IntVar [] of Computer uses. is used for fast access to
	 * the variables in a solver context
	 */
	protected ResourceLoad resourceLoad = null;

	/**
	 * create the variables in the problem and store them in this object.
	 *
	 * @param pb
	 *          the {@link IReconfigurationProblem} to add the variables into
	 */
	public void associate(IReconfigurationProblem pb) {
		int totalVMUse = 0;
		minVMUse = Integer.MAX_VALUE;
		maxVMUse = Integer.MAX_VALUE;
		minComputerCapa = Integer.MAX_VALUE;
		maxComputerCapa = Integer.MIN_VALUE;
		associatedPb = pb;
		locationLoadsByIndex = new IntVar[pb.b().waitIdx() + 1];
		vmsLoads = new int[pb.c().nbVMs()];
		for (int i = 0; i < pb.c().nbVMs(); i++) {
			int use = specs.getUse(pb.b().vm(i));
			vmsLoads[i] = use;
			maxVMUse = Math.max(maxVMUse, use);
			minVMUse = Math.min(minVMUse, use);
			totalVMUse += use;
		}
		nodesCapacities = new int[pb.c().nbComputers()];
		for (int i = pb.b().firstComputerIdx(); i <= pb.b().waitIdx(); i++) {
			VMLocation n = pb.b().location(i);
			int capa = totalVMUse;
			if (i >= pb.b().firstComputerIdx() && i <= pb.b().lastComputerIdx()) {
				capa = specs.getCapacity(n);
				maxComputerCapa = Math.max(maxComputerCapa, capa);
				minComputerCapa = Math.min(minComputerCapa, capa);
				nodesCapacities[i] = capa;
			}
			locationLoadsByIndex[i] = pb.v()
					.createBoundIntVar((n != null ? n.getName() : "waitingVM") + "." + specs.getType(), 0, capa);
		}
		resourceLoad = new ResourceLoad(vmsLoads, locationLoadsByIndex, nodesCapacities);
		for (VM v : pb.c().getMigratingVMs()) {
			VMLocation host = pb.c().getLocation(v);
			resourceLoad.addUse(pb.b().location(host), pb.b().vm(v));
		}
	}

	public ResourceLoad getResourceLoad() {
		return resourceLoad;
	}

	/**
	 * @return the table of IntVar corresponding to the resource load for each
	 *         node(ie the sum of the use of its hosted vms)
	 */
	public IntVar[] getComputerLoads() {
		return locationLoadsByIndex;
	}

	public IntVar getComputerLoad(Computer n) {
		return locationLoadsByIndex[associatedPb.b().location(n)];
	}

	/**
	 *
	 * @return the array of nodes capacities, indexed by the nodes index in the
	 *         problem associated
	 */
	public int[] getCapacities() {
		return nodesCapacities;
	}

	public int getCapacity(Computer n) {
		return specs.getCapacity(n);
	}

	/**
	 *
	 * @return the array of vms consumptions, indexed by the vms index in the
	 *         problem associated
	 */
	public int[] getVmsLoads() {
		return vmsLoads;
	}

	public int getVMLoad(VM vm) {
		return specs.getUse(vm);
	}

	/**
	 * @return the minVMUsage
	 */
	public int getMinVMLoad() {
		return minVMUse;
	}

	/**
	 * @return the maxVMUsage
	 */
	public int getMaxVMLoad() {
		return maxVMUse;
	}

	/**
	 * @return the minComputerCapa
	 */
	public int getMinComputerCapa() {
		return minComputerCapa;
	}

	/**
	 * @return the maxComputerCapa
	 */
	public int getMaxComputerCapa() {
		return maxComputerCapa;
	}
}

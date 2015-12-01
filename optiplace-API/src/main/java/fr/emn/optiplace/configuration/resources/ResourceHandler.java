
package fr.emn.optiplace.configuration.resources;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * <p>
 * The link between a {@link ResourceSpecification}, which maps VM and Nodes to
 * their use and capacity, and a {@link IReconfigurationProblem} which has
 * indexed VM and Nodes.<br />
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

	protected IntVar[] nodesLoadsByIndex = null;
	protected int minVMUse = Integer.MAX_VALUE;
	protected int maxVMUse = Integer.MAX_VALUE;
	protected int minNodeCapa = Integer.MAX_VALUE;
	protected int maxNodeCapa = Integer.MIN_VALUE;
	protected int[] nodesCapacities = null;
	protected int[] vmsLoads = null;
	protected IReconfigurationProblem associatedPb = null;

	public ResourceHandler(ResourceSpecification specs) {
		assert specs != null;
		this.specs = specs;
	}

	/**
	 * stores the actual IntVar [] of Node uses. is used for fast access to the
	 * variables in a solver context
	 */
	protected ResourceLoad resourceLoad = null;

	/**
	 * create the variables in the problem and store them in this object.
	 *
	 * @param pb
	 *          the {@link IReconfigurationProblem} to add the variables into
	 */
	public void associate(IReconfigurationProblem pb) {
		minVMUse = Integer.MAX_VALUE;
		maxVMUse = Integer.MAX_VALUE;
		minNodeCapa = Integer.MAX_VALUE;
		maxNodeCapa = Integer.MIN_VALUE;
		associatedPb = pb;
		nodesLoadsByIndex = new IntVar[pb.c().nbNodes()];
		vmsLoads = new int[pb.c().nbVMs()];
		for (int i = 0; i < pb.c().nbVMs(); i++) {
			int use = specs.getUse(pb.b().vm(i));
			vmsLoads[i] = use;
			maxVMUse = Math.max(maxVMUse, use);
			minVMUse = Math.min(minVMUse, use);
		}
		nodesCapacities = new int[pb.c().nbNodes()];
		for (int i = 0; i < pb.c().nbNodes(); i++) {
			Node n = pb.b().node(i);
			int capa = specs.getCapacity(n);
			nodesCapacities[i] = capa;
			maxNodeCapa = Math.max(maxNodeCapa, capa);
			minNodeCapa = Math.min(minNodeCapa, capa);
			nodesLoadsByIndex[i] = pb.v().createBoundIntVar(n.getName() + "." + specs.getType(), 0, capa);
		}
		resourceLoad = new ResourceLoad(vmsLoads, nodesLoadsByIndex);
		for(VM v : pb.c().getMIgratingVMs()) {
			VMHoster host = pb.c().getLocation(v);
			if (host instanceof Node)
				resourceLoad.addUse(pb.b().node((Node) host), pb.b().vm(v));
		}
	}

	public ResourceLoad getResourceLoad() {
		return resourceLoad;
	}

	/**
	 * @return the table of IntVar corresponding to the resource load for each
	 *         node(ie the sum of the use of its hosted vms)
	 */
	public IntVar[] getNodeLoads() {
		return nodesLoadsByIndex;
	}

	public IntVar getNodeLoad(Node n) {
		return nodesLoadsByIndex[associatedPb.b().node(n)];
	}

	/**
	 *
	 * @return the array of nodes capacities, indexed by the nodes index in the
	 *         problem associated
	 */
	public int[] getCapacities() {
		return nodesCapacities;
	}

	public int getCapacity(Node n) {
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
	 * @return the minNodeCapa
	 */
	public int getMinNodeCapa() {
		return minNodeCapa;
	}

	/**
	 * @return the maxNodeCapa
	 */
	public int getMaxNodeCapa() {
		return maxNodeCapa;
	}
}

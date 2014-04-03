package entropy.configuration.resources;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;
import entropy.solver.choco.ReconfigurationProblem;

/**
 * The link between a {@link ResourceSpecification} and a
 * {@link ReconfigurationProblem}, that is, store the data related to the
 * specifications in the problem<br />
 * Construct it with its {@link ResourceSpecification}, then
 * {@link #associate(ReconfigurationProblem)} it to the problem. This is then
 * indexed by its specifications ' type in the problem's {@link
 * ReconfigurationProblem.#getUse(String)}, {@link
 * ReconfigurationProblem.#getUses()} and {@link
 * ReconfigurationProblem.#getHandlers()}
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class ResourceHandler {

	private final ResourceSpecification specs;

	/** @return the internal resource specifications */
	public ResourceSpecification getSpecs() {
		return specs;
	}

	protected IntDomainVar[] vmsUsesByIndex = null;
	protected IntDomainVar[] nodesUsesByIndex = null;
	protected int minVMUse = Integer.MAX_VALUE;
	protected int maxVMUse = Integer.MAX_VALUE;
	protected int minNodeCapa = Integer.MAX_VALUE;
	protected int maxNodeCapa = Integer.MIN_VALUE;
	protected int[] nodesCapacities = null;
	protected int[] vmsUses = null;
	protected ReconfigurationProblem AssociatedPb = null;

	public ResourceHandler(ResourceSpecification specs) {
		this.specs = specs;
	}

	protected ResourceUse resourceUse = null;

	/**
	 * create the variables in the problem and store them in this object.
	 * 
	 * @param pb
	 *            the {@link ReconfigurationProblem} to add the variables into
	 */
	public void associate(ReconfigurationProblem pb) {
		minVMUse = Integer.MAX_VALUE;
		maxVMUse = Integer.MAX_VALUE;
		minNodeCapa = Integer.MAX_VALUE;
		maxNodeCapa = Integer.MIN_VALUE;
		AssociatedPb = pb;
		Node[] nodes = pb.nodes();
		VirtualMachine[] vms = pb.vms();
		vmsUsesByIndex = new IntDomainVar[vms.length];
		nodesUsesByIndex = new IntDomainVar[nodes.length];
		vmsUses = new int[vms.length];
		for (int i = 0; i < vms.length; i++) {
			Integer iuse = specs.toUses().get(vms[i]);
			if (iuse == null) {
				throw new UnsupportedOperationException("vm " + vms[i]
						+ " not specified in resources " + specs.toUses());
			}
			int use = iuse;
			vmsUses[i] = use;
			if (maxVMUse < use) {
				maxVMUse = use;
			}
			if (minVMUse > use) {
				minVMUse = use;
			}
			vmsUsesByIndex[i] = pb.createIntegerConstant(use);
		}
		nodesCapacities = new int[nodes.length];
		for (int i = 0; i < nodes.length; i++) {
			Node n = nodes[i];
			int capa = specs.toCapacities().get(n);
			nodesCapacities[i] = capa;
			if (maxNodeCapa < capa) {
				maxNodeCapa = capa;
			}
			if (minNodeCapa > capa) {
				minNodeCapa = capa;
			}
			nodesUsesByIndex[i] = pb.createBoundIntVar(n.getName() + "."
					+ specs.getType(), 0, capa);
		}
		resourceUse = new ResourceUse(vmsUsesByIndex, nodesUsesByIndex);
	}

	public ResourceUse getResourceUse() {
		return resourceUse;
	}

	/** @return the vmsUsagesByIndex */
	public IntDomainVar[] getVmsUsesByIndex() {
		return vmsUsesByIndex;
	}

	/** @return the nodesUsagesByIndex */
	public IntDomainVar[] getNodesUsesByIndex() {
		return nodesUsesByIndex;
	}

	/**
	 * 
	 * @return the array of nodes capacities, indexed by the nodes index in the
	 *         problem associated
	 */
	public int[] getNodesCapacities() {
		return nodesCapacities;
	}

	/**
	 * 
	 * @return the array of vms consumptions, indexed by the vms index in the
	 *         problem associated
	 */
	public int[] getVmsUses() {
		return vmsUses;
	}

	/** @return the minVMUsage */
	public int getMinVMUse() {
		return minVMUse;
	}

	/** @return the maxVMUsage */
	public int getMaxVMUse() {
		return maxVMUse;
	}

	/** @return the minNodeCapa */
	public int getMinNodeCapa() {
		return minNodeCapa;
	}

	/** @return the maxNodeCapa */
	public int getMaxNodeCapa() {
		return maxNodeCapa;
	}
}

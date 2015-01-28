package fr.emn.optiplace.center.configuration.resources;

import org.chocosolver.solver.variables.IntVar;
import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.center.configuration.VM;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * <p>
 * The link between a {@link ResourceSpecification}, which specifies the use and
 * capacity of VMs and Nodes, and a {@link IReconfigurationProblem} which has
 * IntVar to store information<br />
 * store the data related to the specifications in the problem
 * </p>
 * <p>
 * must be constructed with its {@link ResourceSpecification}, then
 * {@link #associate(IReconfigurationProblem)} to the problem.<br />
 * This is then indexed by its specifications ' type in the problem's {@link
 * ReconfigurationProblem.#getUse(String)}, {@link
 * ReconfigurationProblem.#getUses()} and {@link
 * ReconfigurationProblem.#getHandlers()}
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class ResourceHandler {

    private final ResourceSpecification specs;

    /** @return the internal resource specifications */
    public ResourceSpecification getSpecs() {
	return specs;
    }

    protected IntVar[] nodesUsesByIndex = null;
    protected int minVMUse = Integer.MAX_VALUE;
    protected int maxVMUse = Integer.MAX_VALUE;
    protected int minNodeCapa = Integer.MAX_VALUE;
    protected int maxNodeCapa = Integer.MIN_VALUE;
    protected int[] nodesCapacities = null;
    protected int[] vmsUses = null;
    protected IReconfigurationProblem AssociatedPb = null;

    public ResourceHandler(ResourceSpecification specs) {
	assert specs != null;
	this.specs = specs;
    }

    /**
     * stores the actual IntVar [] of Node uses. is used for fast access to the
     * variables in a solver context
     */
    protected ResourceUse resourceUse = null;

    /**
     * create the variables in the problem and store them in this object.
     *
     * @param pb
     *            the {@link IReconfigurationProblem} to add the variables into
     */
    public void associate(IReconfigurationProblem pb) {
	minVMUse = Integer.MAX_VALUE;
	maxVMUse = Integer.MAX_VALUE;
	minNodeCapa = Integer.MAX_VALUE;
	maxNodeCapa = Integer.MIN_VALUE;
	AssociatedPb = pb;
	Node[] nodes = pb.nodes();
	VM[] vms = pb.vms();
	nodesUsesByIndex = new IntVar[nodes.length];
	vmsUses = new int[vms.length];
	for (int i = 0; i < vms.length; i++) {
	    Integer iuse = specs.toUses().get(vms[i]);
	    if (iuse == null) {
		throw new UnsupportedOperationException("vm " + vms[i] + " not specified in resources "
			+ specs.toUses());
	    }
	    int use = iuse;
	    vmsUses[i] = use;
	    if (maxVMUse < use) {
		maxVMUse = use;
	    }
	    if (minVMUse > use) {
		minVMUse = use;
	    }
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
	    nodesUsesByIndex[i] = pb.createBoundIntVar(n.getName() + "." + specs.getType(), 0, capa);
	}
	resourceUse = new ResourceUse(vmsUses, nodesUsesByIndex);
    }

    public ResourceUse getResourceUse() {
	return resourceUse;
    }

    /**
     * @return the table of IntVar corresponding to the resource load for each
     *         node(ie the sum of the use of its hosted vms)
     */
    public IntVar[] getNodeUses() {
	return nodesUsesByIndex;
    }

    public IntVar getNodeUse(Node n) {
	return nodesUsesByIndex[AssociatedPb.node(n)];
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
    public int[] getVmsUses() {
	return vmsUses;
    }

    public int getVMUse(VM vm) {
	return specs.getUse(vm);
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

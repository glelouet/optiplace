/**
 *
 */
package fr.emn.optiplace.view.access;

import java.util.HashMap;

import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.SetVar;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceUse;

/**
 * View of the core problem. A core problem contains nodes, vms, and the hosting
 * of the vms on the nodes. It also has resource handlers specifying the
 * capacities of the hosts and the consumptions of the vms.<br />
 * It does NOT extend the view because it is used as a base view by the other
 * modules.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public interface CoreView {

    /**
     * @return an array of the nodes of the problem, each at its index position
     */
    public Node[] nodes();

    /**
     * @param n
     * a node of the problem
     * @return the index of the node in the problem, or -1
     */
    public int node(Node n);

    /**
     * @param idx
     * @return the node at given position, or null
     */
    public Node node(int idx);

    /**
     * @return the array of vm of this problem, each vm being on its index,
     * meaning vm(i)= vms[i]
     */
    public VM[] vms();

    /**
     * @param vm
     * a virtual machine of this problem
     * @return the internal index for this vm, or -1 if not known
     */
    public int vm(VM vm);

    /**
     * @param idx
     * @return the vm at given pos, or null
     */
    public VM vm(int idx);

    /**
     * @param vm
     * a virtual machine of the problem
     * @return the index of the node hosting this vm
     */
    public IntVar host(VM vm);

    /** @return the internal array of IntVar, each corresponding to the hsoter of
     * the vm at this index. eg hosts()[5] correspond to host(vm(5)) */
    public IntVar[] hosts();

    /** get the array of VMs hosters. faster to iterate over it than using
     * {@link #host(VM)}
     * @params vms the vms to filter the hosters on if specified.
     * @return the array of VM hosters, indexed by the vms indexes or the position
     * of each vm in vms if not null and not empty. */
    IntVar[] hosts(VM... vms);

    /** @param n a node of the problem
     * @return the number of vms hosted on this node */
    public IntVar nbVM(Node n);

    /** get the table {@link #nbVM(Node)} , indexed by the nodes index (
     * {@link #getNode(int)} )
     * @return */
    IntVar[] nbVMs();

    /**
     * @param n
     * a node of the problem
     * @return the set of all VMs hosted on this node in the dest configuration
     */
    public SetVar hosted(Node n);

    /**
     * @return the array of setVar, each setvar at index i corresponding to the
     * set of VMs hosted by the Node i in the dest configuration.
     */
    public SetVar[] hosteds();

    /**
     * @param n
     * a node of the problem
     * @return the boolean presence of a vm to host on the node
     */
    public BoolVar isHoster(Node n);

    /**
     * @param vm
     * a vm of the problem
     * @return true if the vm change host from source to target
     */
    public BoolVar isMigrated(VM vm);

    /** get the table of boolean for the VMs.
     * @see #isMigrated(VM)
     * @return the table of IntVar, so that
     * ret[i]==isLiveMigrate(getVirtualMachine(i)) */
    BoolVar[] isMigrateds();

    /** get the variable representing the power state of a node in the resulting
     * configuration
     * @param n a node
     * @return a Boolean {@link IntVar} , set to true if the node is supposed to
     * be online. */
    BoolVar isOnline(Node n);

    /**
     * @return the number of migrations performed to pass from source to target
     */
    public IntVar nbMigrations();

    /**
     * @return the map of types to the associated resource handlers
     */
    public HashMap<String, ResourceHandler> getResourcesHandlers();

    /** @param res the name of the resource to get the usage, should be present in
     * {@link #getResourceSpecifications()} keys
     * @return the variable of the uses of the resource */
    ResourceUse getUse(String res);

    /** create a new table of the different {@link ResourceUse}
     * @return */
    ResourceUse[] getUses();

}

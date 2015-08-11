/**
 *
 */
package fr.emn.optiplace.view.access;

import java.util.HashMap;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import fr.emn.optiplace.configuration.Extern;
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
	 *            a node of the problem
	 * @return the index of the node in the problem, or -1
	 */
	public int node(Node n);

	/**
	 * @param idx
	 * @return the node at given position, or null
	 */
	public default Node node(int idx) {
		if (idx < 0) {
			return null;
		}
		Node[] t = nodes();
		return idx >= t.length ? null : t[idx];
	}

	/**
	 * @return the array of vm of this problem, each vm being on its index,
	 *         meaning vm(i)= vms[i]
	 */
	public VM[] vms();

	/**
	 * @param vm
	 *            a virtual machine of this problem
	 * @return the internal index for this vm, or -1 if not known
	 */
	public int vm(VM vm);

	/**
	 * @param idx
	 * @return the vm at given pos, or null
	 */
	public default VM vm(int idx) {
		if (idx < 0) {
			return null;
		}
		VM[] t = vms();
		return idx >= t.length ? null : t[idx];
	}

	/**
	 *
	 * @return the array of known externs
	 */
	public Extern[] externs();

	/**
	 *
	 * @param e
	 *            the extern
	 * @return the index of the extern in {@link #externs()} array or -1
	 */
	public int extern(Extern e);

	/**
	 *
	 * @param idx
	 *            the index of the extern in the {@link #externs()} array
	 * @return the corresponding extern
	 */
	public default Extern extern(int idx) {
		if (idx < 0) {
			return null;
		}
		Extern[] t = externs();
		return idx >= t.length ? null : t[idx];
	}

	///////////////////////////////////////////////////
	// state of the VM, and corresponding location

	public static final int VM_RUNNING = 0, VM_EXTERNED = 1, VM_WAITING = 2;

	/**
	 *
	 * @param vmindex
	 *          a VM of the source problem
	 * @return a Variable constrained to 0: VM is hosted, 1:VM is externed, 2: VM
	 *         is waiting {@link #VM_EXTERNED}{@link #VM_RUNNING}
	 *         {@link #VM_WAITING}
	 */
	public IntVar getState(int vmindex);

	/**
	 * shortcut for {@link #getState(int) getState(vm(vm))}
	 */
	public default IntVar getState(VM vm) {
		return getState(vm(vm));
	}

	/**
	 * get the host variable of the specified vm
	 *
	 * @param vmindex
	 *          the index of the vm in the
	 * @return the intvar constrained to the Node index hosting the VM, or -1 if
	 *         vm not running.
	 */
	public IntVar getHost(int vmindex);

	/**
	 * shortcut for {@link #getHost(int) getHost(vm(vm))}
	 */
	public default IntVar getHost(VM vm) {
		return getHost(vm(vm));
	}

	/**
	 * @return the internal array of IntVar, each corresponding to the hoster of
	 *         the vm at this indexif this vm is in state {@link #VM_RUNNING}.
	 *         eg hosts()[5] correspond to host(vm(5))
	 */
	public IntVar[] getHosts();

	/**
	 * @param vmindex
	 *          the index of a vm
	 * @return the variable constrained to the index of the extern hosting this VM
	 *         if the VM state is {@link #VM_EXTERNED}
	 */
	public IntVar getExtern(int vmindex);

	/**
	 * shortcut to {@link #getExtern(int) getExtern(vm(vm))}
	 */
	public default IntVar getExtern(VM vm) {
		return getExtern(vm(vm));
	}

	/**
	 *
	 * @param vm
	 *            a virtual machine of the problem
	 * @return the site index of the node hosting this VM
	 */
	public IntVar getSite(VM vm);

	/**
	 * set a VM as shadowing a node, eg when a VM is migrating.
	 *
	 * @param vm
	 *            a VM of the source
	 * @param n
	 *            the node this VM shadows (so the one it migrates to)
	 * @return true if the VM was not shadowing a node before
	 */
	public boolean setShadow(VM vm, Node n);

	/**
	 * get the node a VM shadows
	 *
	 * @param vm
	 *            a VM of the center
	 * @return a Node shadowed, if any, or null if none.
	 */
	public Node getShadow(VM vm);

	/**
	 * @param vm
	 *            a vm of the problem
	 * @return true if the vm change host from source to target
	 */
	public BoolVar isMigrated(VM vm);

	/**
	 * get the table of boolean for the VMs.
	 *
	 * @see #isMigrated(VM)
	 * @return the table of IntVar, so that
	 *         ret[i]==isLiveMigrate(getVirtualMachine(i))
	 */
	BoolVar[] isMigrateds();

	/**
	 * @return the number of migrations performed to pass from source to target
	 */
	public IntVar nbMigrations();

	////////////////////////////////////////
	// Node hosting of VM

	/**
	 * @param n
	 *            a node of the problem
	 * @return the number of vms hosted on this node
	 */
	public IntVar nbVM(Node n);

	/**
	 * get the table {@link #nbVM(Node)} , indexed by the nodes index (
	 * {@link #getNodeHost(int)} )
	 *
	 * @return
	 */
	IntVar[] nbVMs();

	/**
	 * @param n
	 *            a node of the problem
	 * @return the set of all VMs hosted on this node in the dest configuration
	 */
	public SetVar hosted(Node n);

	/**
	 * @return the array of setVar, each setvar at index i corresponding to the
	 *         set of VMs hosted by the Node i in the dest configuration.
	 */
	public SetVar[] hosteds();

	/**
	 * @param n
	 *            a node of the problem
	 * @return the boolean presence of a vm to host on the node
	 */
	public BoolVar isHoster(Node n);

	/**
	 *
	 * @return The array of {@link BoolVar} bv, so that bv[i] is constrained to
	 *         true when {@link #nodes()}[i] has at least one VM
	 */
	public BoolVar[] isHosters();

	/**
	 * @return a Variable constrained to the number of Nodes running VMs
	 */
	public IntVar nbHosters();

	/**
	 * get the variable representing the power state of a node in the resulting
	 * configuration
	 *
	 * @param n
	 *            a node
	 * @return a Boolean {@link IntVar} , set to true if the node is supposed to
	 *         be online.
	 */
	BoolVar isOnline(Node n);

	///////////////////////////////////////////////
	// resource management.

	/**
	 * @return the map of types to the associated resource handlers
	 */
	public HashMap<String, ResourceHandler> getResourcesHandlers();

	/**
	 * @param res
	 *            the name of the resource to get the usage, should be present
	 *            in {@link #getResourceSpecifications()} keys
	 * @return the variable of the uses of the resource
	 */
	ResourceUse getUse(String res);

	/**
	 * create a new table of the different {@link ResourceUse}
	 *
	 * @return
	 */
	ResourceUse[] getUses();

}

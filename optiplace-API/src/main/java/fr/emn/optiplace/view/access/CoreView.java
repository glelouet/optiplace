/**
 *
 */
package fr.emn.optiplace.view.access;

import java.util.HashMap;
import java.util.Set;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceLoad;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.Bridge;
import fr.emn.optiplace.solver.choco.ConstraintHelper;
import fr.emn.optiplace.solver.choco.VariablesManager;

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
	 *
	 * @return the bridge between the configuration and the internal indexes
	 */
	public Bridge b();

	/**
	 *
	 * @return the manager of variables. Create simple or constrained variables
	 */
	public VariablesManager v();

	/**
	 *
	 * @return the helper to constraint elements
	 */
	public ConstraintHelper h();

	/**
	 *
	 * @return the source configuration of the problem.
	 */
	public IConfiguration c();

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
		return getState(b().vm(vm));
	}

	/**
	 * get the host variable of the specified vm
	 *
	 * @param vmindex
	 *          the index of the vm in the
	 * @return the intvar constrained to the Node index hosting the VM, or -1 if
	 *         vm not running.
	 */
	public IntVar getNode(int vmindex);

	/**
	 * shortcut for {@link #getNode(int) getHost(vm(vm))}
	 */
	public default IntVar getNode(VM vm) {
		return getNode(b().vm(vm));
	}

	/**
	 * @return the internal array of IntVar, each corresponding to the hoster of
	 *         the vm at this indexif this vm is in state {@link #VM_RUNNING}.
	 *         eg hosts()[5] correspond to host(vm(5))
	 */
	public IntVar[] getNodes();

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
		return getExtern(b().vm(vm));
	}

	public IntVar[] getExterns();

	/**
	 *
	 * @param vm
	 *            a virtual machine of the problem
	 * @return the site index of the node hosting this VM
	 */
	public default IntVar getSite(VM vm) {
		return getSite(b().vm(vm));
	}

	public IntVar getSite(int vmidx);

	/**
	 *
	 * @param vm
	 *          a vm of the problem
	 * @return a variable constrained to be -1 if the VM is waiting, node(n) if
	 *         the VM is executed on the node n, extern(e)+nodes.length if the
	 *         extern e is executing the vm.
	 */
	public default IntVar getHoster(VM vm) {
		return getHoster(b().vm(vm));
	}

	public IntVar getHoster(int vmidx);

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

	/**
	 * is the VM running in dest config ?
	 *
	 * @param vmindex
	 *          the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         running
	 */
	public BoolVar isRunning(int vmindex);

	/**
	 * is the VM running in dest config ?
	 *
	 * @param vm
	 *          the vm the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         running
	 */
	public default BoolVar isRunning(VM vm) {
		return isRunning(b().vm(vm));
	}

	/**
	 * is the VM externed in dest config ?
	 *
	 * @param vmindex
	 *          the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         externed
	 */
	public BoolVar isExterned(int vmindex);

	/**
	 * is the VM externed in dest config ?
	 *
	 * @param vm
	 *          the vm the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         externed
	 */
	public default BoolVar isExterned(VM vm) {
		return isExterned(b().vm(vm));
	}

	/**
	 * is the VM waitin in dest config ?
	 *
	 * @param vmindex
	 *          the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         waiting
	 */
	public BoolVar isWaiting(int vmindex);

	/**
	 * is the VM waiting in dest config ?
	 *
	 * @param vm
	 *          the vm the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         waiting
	 */
	public default BoolVar isWaiting(VM vm) {
		return isWaiting(b().vm(vm));
	}

	////////////////////////////////////////
	// Node hosting of VM

	/**
	 * @param n
	 *            a node of the problem
	 * @return the number of vms hosted on this node
	 */
	public IntVar nbVMs(Node n);

	public IntVar nbVMsOnNode(int nodeIdx);

	/**
	 * get the table {@link #nbVMs(Node)} , indexed by the nodes index (
	 * {@link #getNode(int)} )
	 *
	 * @return
	 */
	IntVar[] nbVMsNodes();

	/**
	 * @param n
	 *            a node of the problem
	 * @return the set of all VMs hosted on this node in the dest configuration
	 */
	public SetVar hosted(Node n);

	SetVar hosted(Extern e);

	public IntVar nbVMs(Extern e);

	public IntVar nbVMsOnExtern(int externIdx);

	/**
	 * @param n
	 *            a node of the problem
	 * @return the boolean presence of a vm to host on the node
	 */
	public default BoolVar isHoster(Node n) {
		return isHoster(b().node(n));
	}

	public BoolVar isHoster(int idx);

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
	 *
	 * @return an unmodifiable set of the resources that are specified
	 */
	public Set<String> knownResources();

	/**
	 * @deprecated
	 * @return the map of types to the associated resource handlers
	 */
	@Deprecated
	public HashMap<String, ResourceHandler> getResourcesHandlers();

	public ResourceSpecification specs(String resName);

	/**
	 * @param res
	 *            the name of the resource to get the usage, should be present
	 *            in {@link #getResourceSpecifications()} keys
	 * @return the variable of the uses of the resource
	 */
	ResourceLoad getUse(String res);

	/**
	 * create a new table of the different {@link ResourceLoad}
	 *
	 * @return
	 */
	ResourceLoad[] getUses();

	IConfiguration extractConfiguration();

}

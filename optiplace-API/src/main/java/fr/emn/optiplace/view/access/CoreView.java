/**
 *
 */
package fr.emn.optiplace.view.access;

import java.util.Set;

import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
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
	 * @return the helper to create constraints
	 */
	public ConstraintHelper h();

	/**
	 *
	 * @return the source configuration of the problem.
	 */
	public IConfiguration c();

	///////////////////////////////////////////////////
	// state of the VM, and corresponding location

	public static final int VM_RUNNODE = 0, VM_RUNEXT = 1, VM_WAITING = 2;

	/**
	 *
	 * @param vmindex
	 *          a VM of the source problem
	 * @return a Variable constrained to 0: VM is running on a node, 1:VM is
	 *         running on an extern, 2: VM is waiting
	 *         {@link #VM_RUNEXT}{@link #VM_RUNNODE} {@link #VM_WAITING}
	 */
	public IntVar getState(int vmindex);

	/**
	 * shortcut for {@link #getState(int) getState(vm(vm))}
	 */
	public default IntVar getState(VM vm) {
		return getState(b().vm(vm));
	}

	/**
	 *
	 * @param vm
	 *            a virtual machine of the problem
	 * @return the site index of the node hosting this VM
	 */
	public default IntVar getVMSite(VM vm) {
		return getVMSite(b().vm(vm));
	}

	public IntVar getVMSite(int vmidx);

	/**
	 *
	 * @param vm
	 *          a vm of the problem
	 * @return a variable constrained to be b().waitidx() if the VM is waiting,
	 *         node(n) if the VM is executed on the node n, extern(e)+nodes.length
	 *         if the extern e is executing the vm.
	 */
	public default IntVar getVMLocation(VM vm) {
		return getVMLocation(b().vm(vm));
	}

	/**
	 * get the Variable constrained to the location of a VM index
	 *
	 * @param vmidx
	 *          the index of a VM.
	 * @return null if unknown VM, the variable constrained to the location of the
	 *         VM otherwise
	 */
	public IntVar getVMLocation(int vmidx);

	/**
	 * get the variables constrained to all VM locations
	 *
	 * @return the internal array of VM location variables, so that ret[i] is
	 *         constrained to the location of the VM i
	 */
	public IntVar[] getVMLocations();

	/**
	 * @param vm
	 *          a vm of the problem
	 * @return a variable constrained to true if the vm change location from
	 *         source to target
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
	 * is the VM running on a Computer in dest config ?
	 *
	 * @param vmindex
	 *          the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         running on node
	 */
	public BoolVar isRunComputer(int vmindex);

	/**
	 * is the VM running in dest config ?
	 *
	 * @param vm
	 *          the vm the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         running on Computer
	 */
	public default BoolVar isRunComputer(VM vm) {
		return isRunComputer(b().vm(vm));
	}

	/**
	 * is the VM externed in dest config ?
	 *
	 * @param vmindex
	 *          the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         running on extern
	 */
	public BoolVar isRunExt(int vmindex);

	/**
	 * is the VM externed in dest config ?
	 *
	 * @param vm
	 *          the vm the index of the VM
	 * @return the boolean variable constrained to bvar==true <==> vm.state ==
	 *         running on extern
	 */
	public default BoolVar isRunExt(VM vm) {
		return isRunExt(b().vm(vm));
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
	// Computer hosting of VM

	/**
	 * @param location
	 *          a location of the problem
	 * @return the number of vms hosted on this node
	 */
	public default IntVar nbVMsOn(VMLocation location) {
		return nbVMsOn(b().location(location));
	}

	/**
	 *
	 * @param locIdx
	 *          the index of the location.
	 * @return a variable constrained to the number of vm with given location.
	 */
	public IntVar nbVMsOn(int locIdx);

	/**
	 * get the table of number of VM running on every location index
	 *
	 * @return
	 */
	IntVar[] nbVMsOn();

	/**
	 * @param loation
	 *          a location of the problem
	 * @return the set of all VMs hosted on this node in the dest configuration
	 */
	public SetVar getHostedOn(VMLocation location);

	/**
	 * @param n
	 *            a node of the problem
	 * @return the boolean presence of a vm to host on the node
	 */
	public default BoolVar isHost(VMLocation n) {
		return isHost(b().location(n));
	}

	public BoolVar isHost(int locIdx);

	/**
	 *
	 * @return The array of {@link BoolVar} bv, so that bv[i] is constrained to
	 *         true when {@link #locations()}[i] has at least one VM
	 */
	public BoolVar[] isHosts();

	/**
	 * @return a Variable constrained to the number of locations running VMs
	 */
	public IntVar nbHosts();


	///////////////////////////////////////////////
	// resource management.

	/**
	 *
	 * @return an unmodifiable set of the resources that are specified
	 */
	public Set<String> knownResources();


	public ResourceSpecification getResourceSpecification(String resName);

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

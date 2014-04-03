/**
 *
 */
package fr.emn.optiplace.configuration;

import fr.emn.optiplace.configuration.CenterStates;
import fr.emn.optiplace.configuration.ManagedElementSet;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class SimpleCenterStates implements CenterStates {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimpleCenterStates.class);

	ManagedElementSet<Node> onlines = new SimpleManagedElementSet<Node>();

	@Override
	public ManagedElementSet<Node> getOnlines() {
		return onlines;
	}

	ManagedElementSet<Node> offlines = new SimpleManagedElementSet<Node>();

	@Override
	public ManagedElementSet<Node> getOfflines() {
		return offlines;
	}

	public void delNode(Node... nodes) {
		if (nodes == null) {
			return;
		}
		for (Node n : nodes) {
			offlines.remove(n);
			onlines.remove(n);
		}
	}

	@Override
	public boolean contains(Node n) {
		return onlines.contains(n) || offlines.contains(n);
	}

	public void addOnline(Node... nodes) {
		if (nodes == null) {
			return;
		}
		delNode(nodes);
		for (Node n : nodes) {
			onlines.add(n);
		}
	}

	public void addOffline(Node... nodes) {
		if (nodes == null) {
			return;
		}
		delNode(nodes);
		for (Node n : nodes) {
			offlines.add(n);
		}
	}

	ManagedElementSet<VirtualMachine> runnings = new SimpleManagedElementSet<VirtualMachine>();

	@Override
	public ManagedElementSet<VirtualMachine> getRunnings() {
		return runnings;
	}

	ManagedElementSet<VirtualMachine> sleepings = new SimpleManagedElementSet<VirtualMachine>();

	@Override
	public ManagedElementSet<VirtualMachine> getSleepings() {
		return sleepings;
	}

	ManagedElementSet<VirtualMachine> waitings = new SimpleManagedElementSet<VirtualMachine>();

	@Override
	public ManagedElementSet<VirtualMachine> getWaitings() {
		return waitings;
	}

	@Override
	public ManagedElementSet<VirtualMachine> getAllVirtualMachines() {
		SimpleManagedElementSet<VirtualMachine> ret = new SimpleManagedElementSet<VirtualMachine>();
		ret.addAll(runnings);
		ret.addAll(waitings);
		ret.addAll(sleepings);
		return ret;
	}

	@Override
	public ManagedElementSet<Node> getAllNodes() {
		SimpleManagedElementSet<Node> ret = new SimpleManagedElementSet<Node>();
		ret.addAll(onlines);
		ret.addAll(offlines);
		return ret;
	}

	@Override
	public boolean isOnline(Node n) {
		return onlines.contains(n);
	}

	@Override
	public boolean isOffline(Node n) {
		return offlines.contains(n);
	}

	@Override
	public boolean isRunning(VirtualMachine vm) {
		return runnings.contains(vm);
	}

	@Override
	public boolean isWaiting(VirtualMachine vm) {
		return waitings.contains(vm);
	}

	@Override
	public boolean isSleeping(VirtualMachine vm) {
		return sleepings.contains(vm);
	}

	public void delVM(VirtualMachine... machines) {
		if (machines == null) {
			return;
		}
		for (VirtualMachine vm : machines) {
			runnings.remove(vm);
			sleepings.remove(vm);
			waitings.remove(vm);
		}
	}

	@Override
	public boolean contains(VirtualMachine vm) {
		return runnings.contains(vm) || sleepings.contains(vm)
				|| waitings.contains(vm);
	}

	public void addSleeping(VirtualMachine... machines) {
		if (machines == null) {
			return;
		}
		delVM(machines);
		for (VirtualMachine vm : machines) {
			sleepings.add(vm);
		}
	}

	public void addRunning(VirtualMachine... machines) {
		if (machines == null) {
			return;
		}
		delVM(machines);
		for (VirtualMachine vm : machines) {
			runnings.add(vm);
		}
	}

	public void addWaiting(VirtualMachine... machines) {
		if (machines == null) {
			return;
		}
		delVM(machines);
		for (VirtualMachine vm : machines) {
			waitings.add(vm);
		}
	}
}

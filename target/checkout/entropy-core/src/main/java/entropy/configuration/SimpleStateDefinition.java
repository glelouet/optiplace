/**
 *
 */
package entropy.configuration;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class SimpleStateDefinition implements StateDefinition {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimpleStateDefinition.class);

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

	@Override
	public boolean contains(VirtualMachine vm) {
		return runnings.contains(vm) || sleepings.contains(vm)
				|| waitings.contains(vm);
	}
}

package entropy.solver;

import entropy.BaseCenter;
import entropy.ConfigStrat;
import entropy.DeducedTarget;
import entropy.configuration.*;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 * 
 */
public abstract class CustomizableSearcher {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(CustomizableSearcher.class);

	protected BaseCenter base = new BaseCenter();

	protected ConfigStrat strat = new ConfigStrat();

	protected DeducedTarget target = new DeducedTarget();

	public abstract void makeBaseProblem() throws PlanException;

	public abstract void solveProblem();

	public void solve() {
		try {
			makeBaseProblem();
		} catch (PlanException e) {
			// TODO Auto-generated catch block
			throw new UnsupportedOperationException(e);
		}

		solveProblem();
	}

	/**
	 * put the vms from a configuration in another configuration, following the
	 * states constraints specfied
	 */
	public static void makeDest(Configuration src, StateDefinition states,
			Configuration dest) throws PlanException {

		ManagedElementSet<VirtualMachine> run = dest.getRunnings();
		ManagedElementSet<VirtualMachine> wait = dest.getWaitings();
		ManagedElementSet<VirtualMachine> sleep = dest.getSleepings();
		ManagedElementSet<Node> on = dest.getOnlines();
		ManagedElementSet<Node> off = dest.getOfflines();
		if (states == null) {
			run.addAll(src.getRunnings());
			wait.addAll(src.getWaitings());
			sleep.addAll(src.getSleepings());
			on.addAll(src.getOnlines());
			off.addAll(src.getOfflines());
		} else {
			for (Node n : src.getAllNodes()) {
				if (states.isOnline(n)) {
					on.add(n);
				} else if (states.isOffline(n)) {
					off.add(n);
				} else if (src.isOnline(n)) {
					on.add(n);
				} else {
					off.add(n);
				}
			}
			for (VirtualMachine vm : src.getAllVirtualMachines()) {
				if (states.isRunning(vm)) {
					run.add(vm);
				} else if (states.isSleeping(vm)) {
					sleep.add(vm);
				} else if (states.isWaiting(vm)) {
					wait.add(vm);
				} else if (src.isRunning(vm)) {
					run.add(vm);
				} else if (src.isSleeping(vm)) {
					sleep.add(vm);
				} else if (src.isWaiting(vm)) {
					wait.add(vm);
				}
			}
		}
	}
}

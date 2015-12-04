package entropy.view.scheduling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import choco.Choco;
import org.chocosolver.solver.search.ISolutionDisplay;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.action.Action;
import entropy.view.scheduling.actionModel.ActionModel;
import entropy.view.scheduling.actionModel.ActionModels;
import entropy.view.scheduling.actionModel.BootNodeActionModel;
import entropy.view.scheduling.actionModel.InstantiateActionModel;
import entropy.view.scheduling.actionModel.MigratableActionModel;
import entropy.view.scheduling.actionModel.NodeActionModel;
import entropy.view.scheduling.actionModel.ReInstantiateActionModel;
import entropy.view.scheduling.actionModel.ResumeActionModel;
import entropy.view.scheduling.actionModel.RunActionModel;
import entropy.view.scheduling.actionModel.ShutdownNodeActionModel;
import entropy.view.scheduling.actionModel.StayOfflineNodeActionModel;
import entropy.view.scheduling.actionModel.StopActionModel;
import entropy.view.scheduling.actionModel.SuspendActionModel;
import entropy.view.scheduling.actionModel.VirtualMachineActionModel;
import entropy.view.scheduling.actionModel.slice.ConsumingSlice;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import entropy.view.scheduling.actionModel.slice.Slice;
import entropy.view.scheduling.choco.SatisfyDemandingSliceHeights;
import entropy.view.scheduling.choco.SatisfyDemandingSlicesHeightsFastBP;
import entropy.view.scheduling.choco.SlicesPlanner;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.ManagedElementSet;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;

public class SchedulingView extends EmptyView {

	private static final Logger logger = LoggerFactory
			.getLogger(SchedulingView.class);

	/** The maximum duration of a plan. */
	public static Integer MAX_TIME = Choco.MAX_UPPER_BOUND / 10;

	/** The duration evaluator. */
	private final DurationEvaluator durEval;

	/**
	 * Make a new module.
	 *
	 * @param eval
	 *            the evaluator for each action
	 */
	public SchedulingView(DurationEvaluator eval) {
		durEval = eval;
	}

	/**
	 * Get the duration evaluator used to estimate the duration of the actions.
	 *
	 * @return the evaluator
	 */
	public final DurationEvaluator getDurationEvaluator() {
		return durEval;
	}

	protected SlicesPlanner planner = new SlicesPlanner(this);

	/** @return the {@link SlicesPlanner} */
	public SlicesPlanner getPlanner() {
		return planner;
	}

	/**
	 * @param planner
	 *            the planner to set
	 */
	public void setPlanner(SlicesPlanner planner) {
		this.planner = planner;
	}

	/** The moment the reconfiguration starts. Equals to 0. */
	private IntVar start;

	/** The moment the reconfiguration ends. Variable. */
	private IntVar end;

	public IntVar getStart() {
		return start;
	}

	public IntVar getEnd() {
		return end;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void associate(ReconfigurationProblem rp) {
		super.associate(rp);

		start = rp.createIntegerConstant(null, 0);
		end = rp.createBoundIntVar("end", 0, MAX_TIME);
		post(rp.geq(end, start));

		try {
			makeBasicActions();
		} catch (DurationEvaluationException e) {
			throw new UnsupportedOperationException(e);
		}
		System.err.println("node actions : " + nodesActions);

		planner.associate(rp);

		new SlicesPlanner(this).associate(rp);
		updateUpperBounds();

		if (packingConstraintInjector != null) {
			packingConstraintInjector.associate(rp);
		}
	}

	/**
	 * Extract the resulting reconfiguration plan if the solving process
	 * succeeded.
	 *
	 * @return a plan if the solving process succeeded or {@code null}
	 */
	public fr.emn.optiplace.solver.ReconfigurationResult extractSolution() {
		// TODO: check if solution is found
		// Configuration dst = extractConfiguration();
		ReconfigurationProblem problem = getProblem();
		DefaultTimedReconfigurationPlan plan = new DefaultTimedReconfigurationPlan(
				problem.getSourceConfiguration());
		for (NodeActionModel action : getNodeMachineActions()) {
			if (action instanceof BootNodeActionModel) {
				for (Action a : action.getDefinedAction(problem)) {
					if (!plan.add(a)) {
						logger.warn("Action " + a
								+ " is not added into the plan");
					}
				}
			}
		}
		for (VirtualMachineActionModel action : getVirtualMachineActions()) {
			for (Action a : action.getDefinedAction(problem)) {
				if (!plan.add(a)) {
					logger.warn("Action " + a + " is not added into the plan");
				}
			}
		}
		for (Action a : plan) {
			if (a.getStartMoment() == a.getFinishMoment()) {
				logger.error("Action " + a + " has a duration equals to 0");
				throw new RuntimeException();
			}
		}

		for (NodeActionModel action : getNodeMachineActions()) {
			if (action instanceof ShutdownNodeActionModel) {
				for (Action a : action.getDefinedAction(problem)) {
					if (!plan.add(a)) {
						logger.warn("Action " + a
								+ " is not added into the plan");
					}
				}
			}
		}
		if (plan.getDuration() != end.getVal()) {
			logger.error("Theoretical duration (" + end.getVal()
					+ ") and plan duration (" + plan.getDuration()
					+ ") mismatch");
			return null;
		}
		return plan;
	}

	/** The moments each VM is ready. */
	private IntVar[] momentVMReady;

	/**
	 * Get the moment the virtual machine is ready to be running.
	 *
	 * @param vm
	 *            the virtual machine
	 * @return {@code null} if the VM is already ready, a variable otherwise
	 */
	public IntVar getTimeVMReady(VirtualMachine vm) {
		int idx = problem.vm(vm);
		return momentVMReady[idx];
	}

	/**
	 * All the virtual machines' action to perform that implies regular actions.
	 */
	private List<VirtualMachineActionModel> vmActions;

	/**
	 * Get all the actions related to virtual machines.
	 *
	 * @return a list of actions.
	 */
	public List<VirtualMachineActionModel> getVirtualMachineActions() {
		List<VirtualMachineActionModel> actions = new ArrayList<VirtualMachineActionModel>();
		for (VirtualMachineActionModel a : vmActions) {
			if (a != null) {
				actions.add(a);
			}
		}
		return actions;
	}

	/**
	 * Get the action associated to a virtual machine.
	 *
	 * @param vm
	 *            the virtual machine
	 * @return the action associated to the virtual machine.
	 */
	public VirtualMachineActionModel getAssociatedAction(VirtualMachine vm) {
		return vmActions.get(getProblem().vm(vm));
	}

	/**
	 * Get all the actions associated to a list of virtual machines.
	 *
	 * @param vms
	 *            the virtual machines
	 * @return a list of actions. The order is the same than the order of the
	 *         VMs.
	 */
	public List<VirtualMachineActionModel> getAssociatedActions(
			ManagedElementSet<VirtualMachine> vms) {
		List<VirtualMachineActionModel> l = new LinkedList<VirtualMachineActionModel>();
		for (VirtualMachine vm : vms) {
			VirtualMachineActionModel a = getAssociatedAction(vm);
			if (a != null) {
				l.add(a);
			}
		}
		return l;
	}

	/**
	 * Get the action associated to a virtual machine.
	 *
	 * @param vmIdx
	 *            the index of the virtual machine
	 * @return the action associated to the virtual machine.
	 */
	public VirtualMachineActionModel getAssociatedVirtualMachineAction(int vmIdx) {
		return vmActions.get(vmIdx);
	}

	/** All the actions of the nodes that manage their state. */
	private List<NodeActionModel> nodesActions;

	/**
	 * Get all the actions related to nodes.
	 *
	 * @return a list of actions.
	 */
	public List<NodeActionModel> getNodeMachineActions() {
		List<NodeActionModel> actions = new ArrayList<NodeActionModel>();
		for (NodeActionModel a : nodesActions) {
			if (a != null) {
				actions.add(a);
			}
		}
		return actions;
	}

	/**
	 * Get the action associated to a node.
	 *
	 * @param n
	 *            the node
	 * @return the associated action, may be null
	 */
	public NodeActionModel getAssociatedAction(Node n) {
		return nodesActions.get(getProblem().node(n));
	}

	/** All the consuming slices in the model. */
	private List<ConsumingSlice> consumingSlices;

	/** All the demanding slices in the model. */
	private List<DemandingSlice> demandingSlices;

	/**
	 * Get all the demanding slices in the model.
	 *
	 * @return a list of slice. May be empty
	 */
	public List<DemandingSlice> getDemandingSlices() {
		return demandingSlices;
	}

	/**
	 * Get all the consuming slices in the model.
	 *
	 * @return a list of slice. May be empty
	 */
	public List<ConsumingSlice> getConsumingSlice() {
		return consumingSlices;
	}

	/**
	 * Create all the basic action that manipulate the state of the virtual
	 * machine and the nodes.
	 *
	 * @throws NoAvailableTransitionException
	 *             if the VM can not be running regarding to its current state
	 * @throws DurationEvaluationException
	 *             if an error occurred while evaluating the duration of the
	 *             action
	 */
	private void makeBasicActions() throws DurationEvaluationException {
		VirtualMachine[] vms = problem.vms();
		ReconfigurationProblem problem = getProblem();
		Configuration source = problem.getSourceConfiguration();
		Node[] nodes = problem.nodes();

		// make the actions for the VMs
		momentVMReady = new IntVar[c.nbVMs()];
		vmActions = new ArrayList<VirtualMachineActionModel>(c.nbVMs());
		for (int i = 0; i < c.nbVMs(); i++) {
			vmActions.add(i, null);
		}

		for (VirtualMachine vm : problem.getSourceConfiguration()
				.getAllVirtualMachines()) {
			boolean dyn = problem.getManageable().contains(vm);
			VirtualMachineActionModel a;
			if (source.isRunning(vm)) {
				int dM = durEval.evaluateMigration(vm);
				if (vm.checkOption("clone")) {
					int dF = durEval.evaluateForge(vm);
					int dR = durEval.evaluateRun(vm);
					if (dR + dF < dM) {
						a = new ReInstantiateActionModel(this, vm, dF, dR,
								durEval.evaluateStop(vm), dyn);
					} else {
						a = new MigratableActionModel(this, vm, dM, dyn);
					}
				} else {
					a = new MigratableActionModel(this, vm, dM, dyn);
				}
			} else if (problem.getSourceConfiguration().isSleeping(vm)) {
				a = new ResumeActionModel(this, vm,
						durEval.evaluateLocalResume(vm),
						durEval.evaluateRemoteResume(vm));
			} else if (problem.getSourceConfiguration().isWaiting(vm)) {
				int d = vm.getOption("boot") != null ? Integer.parseInt(vm
						.getOption("boot")) : durEval.evaluateRun(vm);
				a = new RunActionModel(this, vm, d);
			} else {
				// ? -> running means ? -> instantiate -> running
				momentVMReady[problem.vm(vm)] = problem.createBoundIntVar("",
						0, MAX_TIME);
				int d = vm.getOption("boot") != null ? Integer.parseInt(vm
						.getOption("boot")) : durEval.evaluateRun(vm);
				int f = durEval.evaluateForge(vm);
				a = new RunActionModel(this, vm, d, f);
			}
			vmActions.set(problem.vm(vm), a);
		}
		for (VirtualMachine vm : problem.getFutureWaitings()) {
			if (!problem.getSourceConfiguration().contains(vm)) {
				momentVMReady[problem.vm(vm)] = problem.createBoundIntVar("",
						0, MAX_TIME);
				// TODO WTF ? useless as createBoundIntVar already add the
				// variable
				// addIntVar(momentVMReady[getVirtualMachine(vm)]);
				int f = durEval.evaluateForge(vm);
				vmActions.set(problem.vm(vm), new InstantiateActionModel(this,
						vm, f));

				// from ? -> waiting, just an instantiate action model
			} else if (!source.isWaiting(vm)) {
			}
		}
		for (VirtualMachine vm : problem.getFutureSleepings()) {
			if (source.isRunning(vm)) {
				VirtualMachineActionModel a = new SuspendActionModel(this, vm,
						durEval.evaluateLocalSuspend(vm));
				vmActions.set(problem.vm(vm), a);
			} else if (source.isWaiting(vm)) {
				throw new NoAvailableTransitionException(vm, "waiting",
						"sleeping");
			} else if (!source.isSleeping(vm)) {
				throw new NoAvailableTransitionException(vm, "terminated",
						"sleeping");
			}
		}
		for (VirtualMachine vm : problem.getFutureTerminated()) {
			if (source.isRunning(vm)) {
				int d = vm.getOption("halt") != null ? Integer.parseInt(vm
						.getOption("halt")) : durEval.evaluateStop(vm);
				VirtualMachineActionModel a = new StopActionModel(this, vm, d);
				vmActions.set(problem.vm(vm), a);
			} else if (source.isSleeping(vm)) {
				throw new NoAvailableTransitionException(vm, "sleeping",
						"terminated");
			} else if (source.isWaiting(vm)) {
				throw new NoAvailableTransitionException(vm, "sleeping",
						"waiting");
			}
		}

		// Make the actions for the nodes

		nodesActions = new ArrayList<NodeActionModel>(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			nodesActions.add(i, null);
		}

		for (Node n : problem.getFutureOnlines()) {
			if (source.getOfflines().contains(n)) {
				BootNodeActionModel a = new BootNodeActionModel(this, n,
						durEval.evaluateStartup(n));
				nodesActions.set(problem.node(a.getNode()), a);
			}
		}
		for (Node n : problem.getFutureOfflines()) {
			if (source.getOnlines().contains(n)) {
				ShutdownNodeActionModel a = new ShutdownNodeActionModel(this,
						n, durEval.evaluateShutdown(n));
				nodesActions.set(problem.node(a.getNode()), a);
			} else {
				StayOfflineNodeActionModel a = new StayOfflineNodeActionModel(
						this, n);
				nodesActions.set(problem.node(a.getNode()), a);
			}
		}

		// Get all the slices
		demandingSlices = new ArrayList<DemandingSlice>();
		demandingSlices.addAll(ActionModels
				.extractDemandingSlices(getVirtualMachineActions()));
		demandingSlices.addAll(ActionModels
				.extractDemandingSlices(getNodeMachineActions()));

		consumingSlices = new ArrayList<ConsumingSlice>();
		consumingSlices.addAll(ActionModels
				.extractConsumingSlices(getVirtualMachineActions()));
		consumingSlices.addAll(ActionModels
				.extractConsumingSlices(getNodeMachineActions()));
	}

	// TODO rework here
	/**
	 * Estimate the lower and the upper bound of {@link #getEnd()}
	 *
	 * @param totalDuration
	 *            the totalDuration of all the action
	 * @throws entropy.view.scheduling.DurationEvaluationException
	 *             if an error occured during evaluation of the durations.
	 */
	protected void setTotalDurationBounds(ManagedElementSet<VirtualMachine> vms) {
		int sup = MAX_TIME;
		int min = 0;
		try {
			getEnd().setInf(min);
			getEnd().setSup(sup);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		logger.debug(getEnd().pretty());
	}

	/** Update the upper bounds of all the variable to simplify the problem. */
	protected void updateUpperBounds() {
		int ub = getEnd().getUB();
		List<ActionModel> allActionModels = new LinkedList<ActionModel>(
				getNodeMachineActions());
		allActionModels.addAll(getVirtualMachineActions());

		try {
			for (VirtualMachineActionModel a : getVirtualMachineActions()) {
				if (a.end().getUB() > ub) {
					a.end().setSup(ub);
				}
				if (a.start().getUB() > ub) {
					a.start().setSup(ub);
				}

				if (a.getGlobalCost().getUB() > ub) {
					a.getGlobalCost().setSup(ub);
				}

				Slice task = a.getDemandingSlice();
				if (task != null) {
					if (task.end().getUB() > ub) {
						task.end().setSup(ub);
					}
					if (task.start().getUB() > ub) {
						task.start().setSup(ub);
					}
					if (task.duration().getUB() > ub) {
						task.duration().setSup(ub);
					}
				}

				task = a.getConsumingSlice();
				if (task != null) {
					if (task.end().getUB() > ub) {
						task.end().setSup(ub);
					}
					if (task.start().getUB() > ub) {
						task.start().setSup(ub);
					}
					if (task.duration().getUB() > ub) {
						task.duration().setSup(ub);
					}
				}
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

	/**
	 * displayer that prints the configuration of nodes to vms
	 *
	 * @author guillaume
	 */
	protected class EndconfigurationDisplayer implements ISolutionDisplay {

		@Override
		public String solutionToString() {
			LinkedHashMap<String, LinkedHashSet<String>> ret = new LinkedHashMap<String, LinkedHashSet<String>>();
			for (VirtualMachine vm : problem.getSourceConfiguration()
					.getAllVirtualMachines()) {
				Node h = problem.getNode(getAssociatedAction(vm)
						.getDemandingSlice().hoster().getVal());
				LinkedHashSet<String> s = ret.get("\n " + h.getName());
				if (s == null) {
					s = new LinkedHashSet<String>();
					ret.put("\n " + h.getName(), s);
				}
				s.add(vm.getName());
			}
			return "in " + getEnd() + "ms : " + ret.toString();
		}

		@Override
		public String toString() {
			return "EndConfigurationDisplayer";
		}

	}

	/** return a new {@link EndconfigurationDisplayer} */
	public EndconfigurationDisplayer getEndconfigurationDisplayer() {
		return new EndconfigurationDisplayer();
	}

	protected SatisfyDemandingSliceHeights packingConstraintInjector = new SatisfyDemandingSlicesHeightsFastBP(
			this);

	/** @return the packingConstraintInjector */
	public SatisfyDemandingSliceHeights getPackingConstraintInjector() {
		return packingConstraintInjector;
	}

	/**
	 * @param packingConstraintInjector
	 *            the packingConstraintInjector to set
	 */
	public void setPackingConstraintInjector(
			SatisfyDemandingSliceHeights packingConstraintInjector) {
		this.packingConstraintInjector = packingConstraintInjector;
	}

}

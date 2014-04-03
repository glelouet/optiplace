/**
 *
 */
package entropy;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import choco.cp.solver.search.BranchAndBound;
import choco.cp.solver.search.integer.objective.IntObjectiveManager;
import choco.cp.solver.search.integer.objective.MinIntObjManager;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.common.logging.Verbosity;
import choco.kernel.solver.Solution;
import choco.kernel.solver.branch.AbstractIntBranchingStrategy;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.search.ISolutionDisplay;
import choco.kernel.solver.search.ISolutionPool;
import choco.kernel.solver.search.SolutionPoolFactory;
import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.configuration.Configuration;
import entropy.configuration.ManagedElementSet;
import entropy.configuration.SimpleManagedElementSet;
import entropy.configuration.StateDefinition;
import entropy.configuration.VirtualMachine;
import entropy.configuration.resources.ResourceHandler;
import entropy.configuration.resources.ResourceSpecification;
import entropy.core.goals.MigrationReducerGoal;
import entropy.core.heuristics.DummyPlacementHeuristic;
import entropy.core.packers.FastBinPacker;
import entropy.solver.ObjectiveReducer;
import entropy.solver.PlanException;
import entropy.solver.choco.ChocoResourcePacker;
import entropy.solver.choco.DefaultReconfigurationProblem;
import entropy.solver.choco.MultiSolutionDisplayer;
import entropy.view.DefaultResourcesView;
import entropy.view.Rule;
import entropy.view.SearchGoal;
import entropy.view.SearchHeuristic;
import entropy.view.ViewAsModule;

/**
 * basic implementation of the entropy solving process.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class SolvingProcess extends EntropyProcess {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SolvingProcess.class);

	/**
	 * add a the default view specifying the CPU and RAM resource to the
	 * {@link #center(BaseCenter)}. This is automatically called on creation.
	 */
	public void addDefaultResourceView() {
		center.getViews().add(DefaultResourcesView.INSTANCE);
	}

	/**
	 * add a view for CPU and RAM
	 */
	{
		addDefaultResourceView();
	}

	/** the core problem, modified by the views */
	protected DefaultReconfigurationProblem problem;

	@Override
	public void makeProblem() throws PlanException {
		long st = System.currentTimeMillis();
		StateDefinition states = center.getStates();
		Configuration src = center.getSource();
		ManagedElementSet<VirtualMachine> run = new SimpleManagedElementSet<VirtualMachine>();
		ManagedElementSet<VirtualMachine> sleep = new SimpleManagedElementSet<VirtualMachine>();
		if (states == null) {
			run.addAll(src.getRunnings());
			run.addAll(src.getWaitings());
			sleep.addAll(src.getSleepings());
		} else {
			for (VirtualMachine vm : src.getAllVirtualMachines()) {
				if (states.isRunning(vm)) {
					run.add(vm);
				} else if (states.isSleeping(vm)) {
					sleep.add(vm);
				} else if (src.isRunning(vm)) {
					run.add(vm);
				} else if (src.isSleeping(vm)) {
					sleep.add(vm);
				} else if (src.isWaiting(vm)) {
					run.add(vm);
				}
			}
		}
		problem = new DefaultReconfigurationProblem(src, run,
				new SimpleManagedElementSet<VirtualMachine>(), sleep,
				new SimpleManagedElementSet<VirtualMachine>());
		if (strat.getPacker() == null) {
			strat.setPacker(new FastBinPacker());
		}

		// add all the resources specified by the view
		for (ViewAsModule v : center.getViews()) {
			for (ResourceSpecification r : v.getPackedResource()) {
				problem.addResourceHandler(new ResourceHandler(r));
			}
		}
		for (ViewAsModule view : center.getViews()) {
			view.associate(problem);
		}
		// TODO each view should be able to specify the packer.
		ChocoResourcePacker packer = strat.getPacker();
		// all the resources should be added now, we pack them using the packing
		// constraint.
		for (SConstraint<IntDomainVar> c : packer.pack(
				problem.getEnvironment(), problem.getHosters(),
				problem.getUses())) {
			// System.err.println("packer " + packer + " generated " + c);
			problem.post(c);
		}
		for (Rule p : center.getRules()) {
			p.inject(problem);
		}
		for (ViewAsModule view : center.getViews()) {
			for (Rule cc : view.getRequestedRules()) {
				cc.inject(problem);
			}
		}
		target.setBuildTime(System.currentTimeMillis() - st);
		target.setProblem(problem);
	}

	@Override
	public void configLogging() {
		if (strat.getChocoLoggingDepth() >= 0) {
			ChocoLogging.setLoggingMaxDepth(strat.getChocoLoggingDepth());
		}
		if (strat.getChocoVerbosity() != null) {
			ChocoLogging.setVerbosity(strat.getChocoVerbosity());
		}
		ArrayList<ISolutionDisplay> displayers = strat.getDisplayers();
		if (displayers.size() > 0) {
			if (displayers.size() == 1) {
				problem.setSolutionDisplay(displayers.get(0));
			} else {
				problem.setSolutionDisplay(new MultiSolutionDisplayer(
						displayers));
			}
			if (strat.getChocoVerbosity() == null
					|| strat.getChocoVerbosity().intValue() < Verbosity.SOLUTION
							.intValue()) {
				ChocoLogging.toSolution();
			}
		}
	}

	@Override
	public void configSearch() {
		long st = System.currentTimeMillis();
		SearchGoal generator = strat.getGoal();
		if (generator == null) {
			generator = new MigrationReducerGoal();
		}
		target.setObjective(generator.getObjective(problem));
		problem.setObjective(target.getObjective());

		ArrayList<SearchHeuristic> generators = new ArrayList<SearchHeuristic>(
				strat.getHeuristics());
		if (generators.isEmpty() && strat.getGoal() != null) {
			SearchHeuristic[] arr = strat.getGoal().getHeuristics();
			if (arr != null) {
				generators.addAll(Arrays.asList(arr));
			}
		}
		generators.add(DummyPlacementHeuristic.INSTANCE);
		for (SearchHeuristic hg : generators) {
			List<AbstractIntBranchingStrategy> heuristics = hg
					.getHeuristics(problem);
			for (AbstractIntBranchingStrategy h : heuristics) {
				problem.addGoal(h);
			}
		}
		if (strat.getMaxSearchTime() > 0) {
			problem.setTimeLimit((int) strat.getMaxSearchTime());
		}
		if (target.getObjective() != null
				&& (strat.getReducer() == null || strat.getReducer() == ObjectiveReducer.IDENTITY)) {
			problem.getConfiguration().putBoolean(
					choco.kernel.solver.Configuration.STOP_AT_FIRST_SOLUTION,
					false);
		} else {
			problem.getConfiguration().putBoolean(
					choco.kernel.solver.Configuration.STOP_AT_FIRST_SOLUTION,
					true);
		}

		problem.generateSearchStrategy();
		ISolutionPool sp = SolutionPoolFactory.makeInfiniteSolutionPool(problem
				.getSearchStrategy());
		problem.getSearchStrategy().setSolutionPool(sp);
		target.setConfigTime(System.currentTimeMillis() - st);
	}

	@Override
	public void makeSearch() {
		long st = System.currentTimeMillis();
		if (target.getObjective() == null || strat.getReducer() == null
				|| strat.getReducer() == ObjectiveReducer.IDENTITY) {
			problem.launch();
		} else {
			searchAndReduce();
		}
		target.setSearchTime(System.currentTimeMillis() - st);
	}

	/**
   *
   */
	private void searchAndReduce() {
		BranchAndBound bb = (BranchAndBound) problem.getSearchStrategy();
		MinIntObjManager obj = (MinIntObjManager) bb.getObjectiveManager();
		Field f = null;
		try {
			f = IntObjectiveManager.class.getDeclaredField("targetBound");
			f.setAccessible(true);
		} catch (Exception e) {
			logger.warn("while getting the field", e);
		}
		problem.launch();
		Solution s = null;
		if (problem.isFeasible() == Boolean.TRUE) {
			do {
				int objVal = target.getObjective().getVal();
				s = problem.getSearchStrategy().getSolutionPool()
						.getBestSolution();
				int newMax = (int) Math.ceil(strat.getReducer().reduce(objVal)) - 1;
				if (f != null) {
					try {
						f.set(obj, new Integer(newMax));
					} catch (Exception e) {
						logger.warn("", e);
					}
				}
			} while (problem.nextSolution() == Boolean.TRUE);
		} else {
			return;
		}
		problem.worldPopUntil(problem.getSearchStrategy().baseWorld);
		problem.restoreSolution(s);
	}

	@Override
	public void extractData() {
		if (problem.isFeasible()) {
			target.setDestination(problem.extractConfiguration());
		} else {
			target.setDestination(null);
		}
		target.setSearchBacktracks(problem.getBackTrackCount());
		target.setSearchNodes(problem.getNodeCount());
		target.setSearchSolutions(problem.getNbSolutions());
		for (ViewAsModule v : center.getViews()) {
			v.endSolving();
		}
	}
}

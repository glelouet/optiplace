/**
 *
 */
package fr.emn.optiplace;

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
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.core.goals.MigrationReducerGoal;
import fr.emn.optiplace.core.heuristics.DummyPlacementHeuristic;
import fr.emn.optiplace.core.packers.FastBinPacker;
import fr.emn.optiplace.solver.ObjectiveReducer;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;
import fr.emn.optiplace.solver.choco.DefaultReconfigurationProblem;
import fr.emn.optiplace.solver.choco.MultiSolutionDisplayer;
import fr.emn.optiplace.view.DefaultResourcesView;
import fr.emn.optiplace.view.Rule;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.SearchHeuristic;
import fr.emn.optiplace.view.ViewAsModule;

/**
 * basic implementation of the entropy solving process.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class SolvingProcess extends OptiplaceProcess {

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

	ArrayList<ViewAsModule> views = null;

	@Override
	public void makeProblem() {
		long st = System.currentTimeMillis();
		Configuration src = center.getSource();
		problem = new DefaultReconfigurationProblem(src);
		if (strat.getPacker() == null) {
			strat.setPacker(new FastBinPacker());
		}

		// if we have a view specified by the administrator (containing rules,
		// objectives, etc.) then we add it at the end.
		views = center.getViews();
		if (center.getBaseView() != null) {
			views = new ArrayList<>(center.getViews());
			views.add(center.getBaseView());
		}

		// add all the resources specified by the view
		for (ViewAsModule v : views) {
			for (ResourceSpecification r : v.getPackedResource()) {
				if (r != null) {
					problem.addResourceHandler(new ResourceHandler(r));
				}
			}
		}
		for (ViewAsModule view : views) {
			view.associate(problem);
		}
		ChocoResourcePacker packer = strat.getPacker();
		// all the resources should be added now, we pack them using the packing
		// constraint.
		for (SConstraint<IntDomainVar> c : packer.pack(
				problem.getEnvironment(), problem.getHosters(),
				problem.getUses())) {
			problem.post(c);
		}
		for (ViewAsModule view : views) {
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

		// get the goal if any
		SearchGoal generator = null;
		for (ViewAsModule v : views) {
			SearchGoal sg = v.getSearchGoal();
			if (sg != null) {
				generator = sg;
			}
		}
		if (generator == null) {
			generator = new MigrationReducerGoal();
		}
		target.setObjective(generator.getObjective(problem));
		problem.setObjective(target.getObjective());

		// add all the heuristics.
		// first heuristic : the global goal heuristic
		SearchHeuristic[] l = generator != null
				? generator.getHeuristics()
				: null;
		ArrayList<SearchHeuristic> generators = new ArrayList<SearchHeuristic>();
		if (l != null) {
			generators.addAll(Arrays.asList(l));
		}
		// then add all heuristics from the view, in the views reverse order.
		for (int i = views.size() - 1; i >= 0; i--) {
			generators.addAll(views.get(i).getViewHeuristics());
		}
		// then add default heuristics.
		generators.add(DummyPlacementHeuristic.INSTANCE);

		// all the heuristics are generated and added in the problem here.
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

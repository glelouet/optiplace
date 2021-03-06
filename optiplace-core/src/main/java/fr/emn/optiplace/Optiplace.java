/**
 *<p>this process use all the views to generate the list of heuristics to use in the solver.<br />
 *Also, if the strat.#isDisabledCheckSource() is false AND there is less then 6 VMs waitings,
 *it tries first to place all the VMS at their source position, potentially finding a correct solution very fast.</p>
 */

package fr.emn.optiplace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.FindAndProve;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.actions.Allocate;
import fr.emn.optiplace.actions.Migrate;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.IConfiguration.VMSTATES;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.core.heuristics.DummyPlacementHeuristic;
import fr.emn.optiplace.core.heuristics.NoWaitingHeuristic;
import fr.emn.optiplace.core.heuristics.StickVMsHeuristic;
import fr.emn.optiplace.core.packers.DefaultPacker;
import fr.emn.optiplace.solver.ActivatedHeuristic;
import fr.emn.optiplace.solver.HeuristicsList;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;
import fr.emn.optiplace.solver.heuristics.Static2Activated;
import fr.emn.optiplace.view.SearchGoal;
import fr.emn.optiplace.view.ViewAsModule;

/**
 * basic implementation of the optiplace solving process.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class Optiplace extends IOptiplace {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Optiplace.class);

	/**
	 * find a solution if exists to a Configuration
	 *
	 * @param cfg
	 *          the configuration to test
	 * @return true if an instance of {@link Optiplace} can find a solution.
	 */
	public static boolean hasSolution(IConfiguration cfg) {
		return new Optiplace(cfg).solve().getDestination() != null;
	}

	/** the core problem, modified by the views */
	protected ReconfigurationProblem problem;

	protected Solution lastSolution = null;

	public ReconfigurationProblem getProblem() {
		return problem;
	}

	public Optiplace() {
		this(new Configuration());
	}

	public Optiplace(IConfiguration src) {
		source(src);
	}

	@Override
	public void makeProblem() {
		long st = System.nanoTime();

		// each view can pre-process the configuration, creating or removing VM,
		// computers, etc.
		for (ViewAsModule v : views) {
			v.preProcessConfig(source);
		}

		problem = new ReconfigurationProblem(source);

		for (ViewAsModule view : views) {
			view.associate(problem);
		}
		for (ViewAsModule view : views) {
			view.rulesStream().forEach(cc -> cc.inject(problem));
		}
		if (source.nbComputers() > 0 && source.nbVMs() > 0) {
			ChocoResourcePacker packer = strat.getPacker();
			if (packer == null) {
				packer = new DefaultPacker();
			}
			// all the resources should be added now, we pack them using the packing
			// constraint.
			for (Constraint c : packer.pack(problem.getVMLocations(), problem.getUses())) {
				problem.getModel().post(c);
			}
		}
		// forbid the VM from extern with insufficient resources
		if (source.nbExterns() > 0) {
			for (String resName : problem.knownResources()) {
				ResourceSpecification spec = problem.getResourceSpecification(resName);
				for (int externIdx = problem.b().firstExtIdx(); externIdx <= problem.b().lastExtIdx(); externIdx++) {
					Extern e = (Extern) problem.b().location(externIdx);
					SetVar vms = problem.getHostedOn(externIdx);
					int cap = spec.getCapacity(e);
					for (int vmIdx = 0; vmIdx < problem.b.vms().length; vmIdx++) {
						VM v = problem.b.vm(vmIdx);
						int use = spec.getUse(v);
						if (use > cap) {
							logger.debug("resource " + resName + " prevents vm " + v + " using " + use
									+ " from being hosted on extern " + e + " with cap " + cap);
							try {
								vms.remove(vmIdx, Cause.Null);
							} catch (ContradictionException e1) {
								logger.warn("while removing " + vmIdx + " from " + vms, e1);
							}
						}
					}
				}
			}
		}

		target.setBuildTime(System.nanoTime() - st);
		target.setProblem(problem);
	}

	@Override
	public void configLogging() {
		if (strat.isLogSolutions() || strat.isLogChoices() || strat.isLogContradictions()) {
			if (strat.isLogStats()) {
				problem.getSolver().showStatistics();
			}
			if (strat.isLogSolutions()) {
				problem.getSolver().showSolutions();
			}
			if (strat.isLogChoices()) {
				problem.getSolver().showDecisions();
			}
			if (strat.isLogContradictions()) {
				problem.getSolver().showContradiction();
			}
		}
		strat.getDisplayers().forEach(problem.getSolver()::plugMonitor);
	}

	@Override
	public void configSearch() {
		long st = System.nanoTime();
		// get the goal if any
		SearchGoal goalMaker = null;
		String goalId = strat.getGoalId();
		if (goalId != null) {
			for (ViewAsModule v : views) {
				SearchGoal sg = v.getGoal(goalId);
				if (sg != null) {
					if (goalMaker != null) {
						logger.info("goal " + goalMaker + " overriden by goal " + sg + " from view " + v);
					}
					goalMaker = sg;
				}
			}
		}
		if (goalMaker != null) {
			problem.setObjective(goalMaker.getObjective(problem));
		} else if (goalId != null) {
			logger.warn("error : can't find any objective with id " + goalId + " in present views. available goal ids are :");
			for (ViewAsModule v : views) {
				logger.warn(" (" + v.getClass().getSimpleName() + ")" + v.extractGoals());
			}
			throw new UnsupportedOperationException("can not find goal with id " + goalId);
		}
		AbstractStrategy<Variable> find = null;
		if (!strat.isDisableCheckSource() && problem.getSourceConfiguration().nbVMs(VMSTATES.WAITING) <= 5) {
			find = makeFindHeuristic();
		}
		AbstractStrategy<Variable> prove = makeProveHeuristic(goalMaker);
		if (find == null) {
			problem.getSolver().setSearch(prove);
		} else {
			Variable[] vars = problem.getModel().getVars();
			if (vars != null) {
				problem.getSolver().setSearch(new FindAndProve<>(problem.getModel().getVars(), find, prove));
			} else {
				problem.getSolver().setSearch(prove);
			}
		}

		if (strat.getMaxSearchTime() > 0) {
			problem.getSolver().limitTime(strat.getMaxSearchTime());
		}

		if (strat.isDisableOptimize()) {
			problem.setObjective(null);
		}

		target.setConfigTime(System.nanoTime() - st);
	}

	/**
	 * make an heuristic to quickly find a solution. Basic heuristic is to
	 * retrieve the source configuration if available, do nothing if not.
	 *
	 * @param goalMaker
	 * @return
	 */
	@SuppressWarnings("unchecked")
	AbstractStrategy<Variable> makeFindHeuristic() {
		ArrayList<AbstractStrategy<? extends Variable>> l = new ArrayList<>();
		// heuristic to find a solution fast
		l.addAll(NoWaitingHeuristic.getHeuristics(problem));
		l.add(StickVMsHeuristic.makeStickVMs(
				problem.getSourceConfiguration().getRunnings().collect(Collectors.toList()).toArray(new VM[0]), problem));
		// then add all heuristics from the view, in the views reverse order.
		for (int i = views.size() - 1; i >= 0; i--) {
			l.addAll(views.get(i).getSatisfactionHeuristics());
		}
		l.addAll(DummyPlacementHeuristic.INSTANCE.getHeuristics(problem));
		return Search.sequencer(l.toArray(new AbstractStrategy[0]));
	}

	/**
	 * Make an heuristic to find the best solution. This heuristic is generally
	 * based on the definition of the problem's objective to reduce.
	 *
	 * @param goalMaker
	 * @return
	 */
	@SuppressWarnings("unchecked")
	AbstractStrategy<Variable> makeProveHeuristic(SearchGoal goalMaker) {

		List<AbstractStrategy<? extends Variable>> strats = new ArrayList<>();
		// add all the heuristics.
		// first heuristic : the global goal heuristic
		if (goalMaker != null) {
			strats.addAll(goalMaker.getHeuristics(problem));
		}
		// then add default heuristics.
		strats.addAll(NoWaitingHeuristic.getHeuristics(problem));
		ResourceSpecification memSpec = problem.getResourceSpecification("mem");
		if (memSpec == null && !problem.knownResources().isEmpty()) {
			memSpec = problem.getResourceSpecification(problem.knownResources().iterator().next());
		}
		if (memSpec != null) {
			strats.addAll(new StickVMsHeuristic(memSpec).getHeuristics(problem));
		}
		strats.addAll(DummyPlacementHeuristic.INSTANCE.getHeuristics(problem));

		// we also need to get the activatedHeuristics. Those have higher
		// priority than static heuristics, because they will be inactive most
		// of the time.
		List<ActivatedHeuristic<? extends Variable>> lah = new ArrayList<>();
		if (goalMaker != null) {
			lah.addAll(goalMaker.getActivatedHeuristics(problem));
		}
		lah.add(new Static2Activated<>(Search.sequencer(strats.toArray(new AbstractStrategy[0]))));

		HeuristicsList ret = new HeuristicsList(problem.getModel(), lah.toArray(new ActivatedHeuristic[0]));
		ret.setLogActivated(strat.isLogHeuristicsSelection());
		if (strat.isLogHeuristicsSelection()) {
			logger.debug("proveheuristic is : " + ret.getLeaders());
		}
		return ret;
	}

	@Override
	public void makeSearch() {
		long st = System.nanoTime();
		if (problem.getObjective() != null) {
			lastSolution = problem.getSolver().findOptimalSolution(problem.getObjective(), false);
		} else {
			lastSolution = problem.getSolver().findSolution();
		}
		target.setSearchTime(System.nanoTime() - st);
		if (lastSolution == null) {
			logger.debug("no solution found");
			logger.debug(" variables : " + Arrays.asList(problem.getModel().getVars()));
			logger.debug(" constraints : " + Arrays.asList(problem.getModel().getCstrs()));
		}
		// ObjectiveReducer or = strat.getReducer();
		// if (or != null) {
		// new MaxIntObjManager();
		// <IntVar, Integer> om = new ObjectiveManager<IntVar,
		// Integer>(problem.getObjective(),
		// ResolutionPolicy.MINIMIZE, true) {
		// @Override
		// public void postDynamicCut() throws ContradictionException {
		// objective.updateBounds(bestProvedLB.intValue(),
		// Math.min(or.reduce(bestProvedUB.intValue()), bestProvedUB.intValue()) -
		// 1, this);
		// }
		// };
		// problem.getSolver().setObjectiveManager(om);(om);
		// }
	}

	@Override
	public void extractData() {
		if (lastSolution == null) {
			return;
		}
		try {
			lastSolution.restore();
		} catch (ContradictionException e) {
			throw new UnsupportedOperationException(e);
		}
		IMeasures m = problem.getSolver().getMeasures();
		if (m.getSolutionCount() < 1) {
			return;
		}
		IConfiguration dest = problem.extractConfiguration();
		for (ViewAsModule v : views) {
			v.postProcessConfig(dest);
		}
		target.setDestination(dest);
		if (problem.getObjective() != null) {
			target.setObjective((int) m.getBestSolutionValue());
		}
		Migrate.extractMigrations(source, dest, target.getActions());
		Allocate.extractAllocates(source, dest, target.getActions());
		for (ViewAsModule v : views) {
			v.extractActions(target.getActions(), dest);
		}

		target.setSearchBacktracks(m.getBackTrackCount());
		target.setSearchNodes(m.getNodeCount());
		target.setSearchSolutions(problem.getSolver().getSolutionCount());
	}
}

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
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.objective.ObjectiveManager;
import org.chocosolver.solver.search.loop.monitors.IMonitorContradiction;
import org.chocosolver.solver.search.loop.monitors.SearchMonitorFactory;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.FindAndProve;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;

import fr.emn.optiplace.actions.Allocate;
import fr.emn.optiplace.actions.Migrate;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.IConfiguration.NODESTATES;
import fr.emn.optiplace.configuration.IConfiguration.VMSTATES;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.core.heuristics.DummyPlacementHeuristic;
import fr.emn.optiplace.core.heuristics.StickVMsHeuristic;
import fr.emn.optiplace.core.packers.DefaultPacker;
import fr.emn.optiplace.solver.ActivatedHeuristic;
import fr.emn.optiplace.solver.HeuristicsList;
import fr.emn.optiplace.solver.ObjectiveReducer;
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
		// nodes, etc.
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
		if (source.nbNodes(NODESTATES.ONLINE) > 0 && source.nbVMs() > 0) {
			ChocoResourcePacker packer = strat.getPacker();
			if (packer == null) {
				packer = new DefaultPacker();
			}
			// all the resources should be added now, we pack them using the packing
			// constraint.
			for (Constraint c : packer.pack(problem.getVMLocations(), problem.getUses())) {
				problem.getSolver().post(c);
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
							logger.debug("resource " + resName + " prevents vm " + v + " from being hosted on extern " + e);
							try {
								vms.removeFromEnvelope(vmIdx, Cause.Null);
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
				Chatterbox.showStatistics(problem.getSolver());
			}
			if (strat.isLogSolutions()) {
				Chatterbox.showSolutions(problem.getSolver());
			}
			if (strat.isLogChoices()) {
				Chatterbox.showDecisions(problem.getSolver());
			}
			if (strat.isLogContradictions()) {
				problem.getSolver().plugMonitor((IMonitorContradiction) cex -> System.err.println(cex));
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

		if (strat.isDisableCheckSource() || problem.getSourceConfiguration().nbVMs(VMSTATES.WAITING) > 5) {
			problem.getSolver().set(makeProveHeuristic(goalMaker));
		} else {
			problem.getSolver().set(new FindAndProve<>(problem.getSolver().getVars(), makeFindHeuristic(),
					makeProveHeuristic(goalMaker)));
		}

		if (strat.getMaxSearchTime() > 0) {
			SearchMonitorFactory.limitTime(problem.getSolver(), strat.getMaxSearchTime());
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
		// heuristic to find a solution fast
		AbstractStrategy<? extends Variable> diveSrc = StickVMsHeuristic.makeStickVMs(
				problem.getSourceConfiguration().getRunnings().collect(Collectors.toList()).toArray(new VM[0]), problem);
		ArrayList<AbstractStrategy<? extends Variable>> l = new ArrayList<>();
		l.add(diveSrc);
		// then add all heuristics from the view, in the views reverse order.
		for (int i = views.size() - 1; i >= 0; i--) {
			l.addAll(views.get(i).getSatisfactionHeuristics());
		}
		l.addAll(DummyPlacementHeuristic.INSTANCE.getHeuristics(problem));
		return IntStrategyFactory.sequencer(l.toArray(new AbstractStrategy[0]));
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
		lah.add(new Static2Activated<>(IntStrategyFactory.sequencer(strats.toArray(new AbstractStrategy[0]))));

		HeuristicsList ret = new HeuristicsList(problem.getSolver(), lah.toArray(new ActivatedHeuristic[0]));
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
			problem.getSolver().findOptimalSolution(ResolutionPolicy.MINIMIZE, problem.getObjective());
			if (problem.getSolutionRecorder().getLastSolution() == null) {
				logger.debug("no solution found");
				logger.debug(" variables : " + Arrays.asList(problem.getSolver().getVars()));
				logger.debug(" constraints : " + Arrays.asList(problem.getSolver().getCstrs()));
			}
		} else {
			problem.getSolver().findSolution();
		}
		ObjectiveReducer or = strat.getReducer();
		if (or != null) {
			@SuppressWarnings("serial")
			ObjectiveManager<IntVar, Integer> om = new ObjectiveManager<IntVar, Integer>(problem.getObjective(),
					ResolutionPolicy.MINIMIZE, true) {
				@Override
				public void postDynamicCut() throws ContradictionException {
					objective.updateBounds(bestProvedLB.intValue(),
							Math.min(or.reduce(bestProvedUB.intValue()), bestProvedUB.intValue()) - 1, this);
				}
			};
			problem.set(om);
		}
		target.setSearchTime(System.nanoTime() - st);
	}

	@Override
	public void extractData() {
		try {
			problem.restoreLastSolution();
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
		target.setSearchSolutions(problem.getSolutionRecorder().getSolutions().size());
	}
}

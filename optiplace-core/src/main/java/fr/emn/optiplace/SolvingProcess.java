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

import solver.ResolutionPolicy;
import solver.constraints.Constraint;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.measure.IMeasures;
import solver.search.strategy.IntStrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.FindAndProve;
import solver.variables.IntVar;
import solver.variables.Variable;
import fr.emn.optiplace.actions.Allocate;
import fr.emn.optiplace.actions.Migrate;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Configuration.VMSTATES;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.core.goals.MigrationReducerGoal;
import fr.emn.optiplace.core.heuristics.DummyPlacementHeuristic;
import fr.emn.optiplace.core.heuristics.StickVMsHeuristic;
import fr.emn.optiplace.core.packers.DefaultPacker;
import fr.emn.optiplace.solver.choco.ChocoResourcePacker;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;
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

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SolvingProcess.class);

    /** the core problem, modified by the views */
    protected ReconfigurationProblem problem;

    ArrayList<ViewAsModule> views = null;

    @Override
    public void makeProblem() {
	long st = System.currentTimeMillis();
	Configuration src = center.getSource();
	problem = new ReconfigurationProblem(src);
	if (strat.getPacker() == null) {
	    strat.setPacker(new DefaultPacker());
	}

	// if we have a view specified by the administrator (containing rules,
	// objectives, etc.) then we add it at the end.
	views = center.getViews();
	if (center.getBaseView() != null) {
	    views = new ArrayList<>(views);
	    views.add(center.getBaseView());
	}

	for (ResourceSpecification r : src.resources().values()) {
	    problem.addResourceHandler(new ResourceHandler(r));
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
	for (Constraint c : packer.pack(problem.hosts(), problem.getUses())) {
	    problem.getSolver().post(c);
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
	if (strat.isLogBasics() || strat.isLogSolutions() || strat.isLogChoices()) {
	    SearchMonitorFactory.log(problem.getSolver(), strat.isLogSolutions(), strat.isLogChoices());
	}
	strat.getDisplayers().forEach(problem.getSolver()::plugMonitor);
    }

    @Override
    public void configSearch() {
	long st = System.currentTimeMillis();

	// get the goal if any
	SearchGoal goalMaker = null;
	for (ViewAsModule v : views) {
	    SearchGoal sg = v.getSearchGoal();
	    if (sg != null) {
		goalMaker = sg;
	    }
	}
	if (goalMaker == null) {
	    if (problem.getResourcesHandlers().containsKey("MEM")) {
		goalMaker = new MigrationReducerGoal("MEM");
	    }

	}
	if (goalMaker != null) {
	    problem.setObjective(goalMaker.getObjective(problem));
	}

	if (strat.isDisableCheckSource() || problem.getSourceConfiguration().nbVMs(VMSTATES.WAITING) > 5) {
	    problem.getSolver().set(makeProveHeuristic(goalMaker));
	} else {
	    problem.getSolver().set(
		    new FindAndProve<Variable>(problem.getSolver().getVars(), makeFindHeuristic(),
			    makeProveHeuristic(goalMaker)));
	}

	if (strat.getMaxSearchTime() > 0) {
	    SearchMonitorFactory.limitTime(problem.getSolver(), strat.getMaxSearchTime());
	}

	if (strat.isDisableOptimize()) {
	    problem.setObjective(null);
	}

	target.setConfigTime(System.currentTimeMillis() - st);
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
	AbstractStrategy<IntVar> diveSrc = StickVMsHeuristic.makeStickVMs(problem.getSourceConfiguration()
		.getRunnings().collect(Collectors.toList()).toArray(new VM[0]), problem);
	ArrayList<AbstractStrategy<? extends Variable>> l = new ArrayList<>();
	l.add(diveSrc);
	// then add all heuristics from the view, in the views reverse order.
	for (int i = views.size() - 1; i >= 0; i--) {
	    views.get(i).getFindHeuristics().stream().flatMap(s -> s.getHeuristics(problem).stream())
		    .forEach(t -> l.add(t));

	}
	return IntStrategyFactory.sequencer(diveSrc, DummyPlacementHeuristic.INSTANCE.getHeuristics(problem).get(0));
    }

    /**
     * Make an heuristic to find the best oslution. This heuristic is generally
     * based on the definition of the problem'ss objective to reduce.
     *
     * @param goalMaker
     * @return
     */
    @SuppressWarnings("unchecked")
    AbstractStrategy<Variable> makeProveHeuristic(SearchGoal goalMaker) {
	// add all the heuristics.
	// first heuristic : the global goal heuristic
	SearchHeuristic[] objectiveHeuristics = goalMaker != null ? goalMaker.getHeuristics(problem) : null;
	ArrayList<SearchHeuristic> heuristicsGenerators = new ArrayList<SearchHeuristic>();
	if (objectiveHeuristics != null) {
	    heuristicsGenerators.addAll(Arrays.asList(objectiveHeuristics));
	}
	// then add all heuristics from the view, in the views reverse order.
	for (int i = views.size() - 1; i >= 0; i--) {
	    List<SearchHeuristic> viewGens = views.get(i).getSearchHeuristics();
	    heuristicsGenerators.addAll(viewGens);
	}
	// then add default heuristics.
	if (problem.getResourcesHandlers().get("MEM") != null) {
	    heuristicsGenerators.add(new StickVMsHeuristic(problem.getResourcesHandlers().get("MEM").getSpecs()));
	}
	heuristicsGenerators.add(DummyPlacementHeuristic.INSTANCE);

	// all the heuristics are generated and added in the problem here.
	List<AbstractStrategy<? extends Variable>> strats = heuristicsGenerators.stream()
		.map(sh -> sh.getHeuristics(problem)).flatMap(l -> l.stream()).collect(Collectors.toList());
	return IntStrategyFactory.sequencer(strats.toArray(new AbstractStrategy[0]));

    }

    @Override
    public void makeSearch() {
	long st = System.currentTimeMillis();
	if (problem.getObjective() != null) {
	    problem.getSolver().findOptimalSolution(ResolutionPolicy.MINIMIZE, problem.getObjective());
	} else {
	    problem.getSolver().findSolution();
	}
	// TODO what to do with an ObjectiveReducer ?
	target.setSearchTime(System.currentTimeMillis() - st);
    }

    /**
     *
     */
    // Commented to pass from choc 2 to choco 3
    // TODO implement this in choco 3
    // private void searchAndReduce() {
    // BranchAndBound bb = (BranchAndBound) problem.getSearchStrategy();
    // MinIntObjManager obj = (MinIntObjManager) bb.getObjectiveManager();
    // Field f = null;
    // try {
    // f = IntObjectiveManager.class.getDeclaredField("targetBound");
    // f.setAccessible(true);
    // } catch (Exception e) {
    // logger.warn("while getting the field", e);
    // }
    // problem.launch();
    // Solution s = null;
    // if (problem.isFeasible() == Boolean.TRUE) {
    // do {
    // int objVal = ((IntVar) problem.getObjective()).getVal();
    // s = problem.getSearchStrategy().getSolutionPool().getBestSolution();
    // int newMax = (int) Math.ceil(strat.getReducer().reduce(objVal)) - 1;
    // if (f != null) {
    // try {
    // f.set(obj, new Integer(newMax));
    // } catch (Exception e) {
    // logger.warn("", e);
    // }
    // }
    // } while (problem.nextSolution() == Boolean.TRUE);
    // } else {
    // return;
    // }
    // problem.worldPopUntil(problem.getSearchStrategy().baseWorld);
    // problem.restoreSolution(s);
    // }

    @Override
    public void extractData() {
	IMeasures m = problem.getSolver().getMeasures();
	if (m.getSolutionCount() < 1) {
	    return;
	}
	target.setDestination(problem.extractConfiguration());
	if (problem.getObjective() != null) {
	    target.setObjective(((IntVar) problem.getSolver().getObjectiveManager().getObjective()).getValue());
	}
	Migrate.extractMigrations(center.getSource(), target.getDestination(), target.getActions());
	Allocate.extractAllocates(center.getSource(), target.getDestination(), target.getActions());
	for (ViewAsModule v : center.getViews()) {
	    v.endSolving(target.getActions());
	}

	target.setSearchBacktracks(m.getBackTrackCount());
	target.setSearchNodes(m.getNodeCount());
	target.setSearchSolutions(problem.getSolutionRecorder().getSolutions().size());
    }
}

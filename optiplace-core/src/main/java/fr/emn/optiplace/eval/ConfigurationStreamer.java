
package fr.emn.optiplace.eval;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.propagation.NoPropagationEngine;
import org.chocosolver.solver.search.solution.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;

import fr.emn.optiplace.configuration.*;
import fr.emn.optiplace.configuration.resources.MappedResourceSpecification;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;


/**
 * tool class to make streams of configurations.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class ConfigurationStreamer {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationStreamer.class);

	/**
	 * Stream over the solutions not found already.
	 * <p>
	 * As Solver does not support concurrent solution searches, you must ensure
	 * there is no call to any search method between this method call and the last
	 * use of the returned stream.
	 * </p>
	 *
	 * @param s
	 *          the solver to stream over its solutions
	 * @return a new sequential Stream containing the next solutions to call.
	 */
	public static Stream<Solution> nextSolutions(Solver s) {
		return StreamSupport.stream(new Spliterator<Solution>() {

			@Override
			public int characteristics() {
				return DISTINCT | NONNULL | IMMUTABLE | CONCURRENT;
			}

			@Override
			public Spliterator<Solution> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return Long.MAX_VALUE;
			}

			@Override
			public boolean tryAdvance(Consumer<? super Solution> action) {
				boolean ret = false;
				if (s.getEngine() == NoPropagationEngine.SINGLETON || !s.getEngine().isInitialized()) {
					ret = s.findSolution();
				} else {
					ret = s.nextSolution();
				}
				if (ret) {
					action.accept(s.getSolutionRecorder().getLastSolution());
				}
				return ret;
			}

		}, false);
	}

	/**
	 * get the next solutions of a solver and convert them to another type
	 *
	 * @param <T>
	 *          the type of returned solutions
	 * @param s
	 *          the solver to extract the solutions
	 * @param mapper
	 *          the function to convert the solutions of the solver to a T
	 * @return the stream of the next solutions casted to T of the solver s
	 */
	public static <T> Stream<T> nextSolutions(Solver s, Function<Solution, T> mapper) {
		return nextSolutions(s).map(mapper);
	}

	/**
	 * generates a stream of Configurations which do not specify resources use or
	 * capacities.
	 *
	 * @param maxVMPerHost
	 *          max number of VM per hoster, only checked if >0.
	 * @param maxCfgSize
	 *          maximum size of the configuration, only checked if >0. see
	 *          {@link #getSize(IConfiguration)}.
	 * @return a new stream containing configurations which only have VM waiting
	 */
	public static Stream<IConfiguration> streamElements(int maxVMPerHost, int maxCfgSize) {

		Spliterator<IConfiguration> ret = new Spliterator<IConfiguration>() {

			@Override
			public int characteristics() {
				return DISTINCT | NONNULL | IMMUTABLE | CONCURRENT;
			}

			int nbHosters = 0;
			int nbVMs = 0;
			int nbExterns = 0;

			public IConfiguration makeCfg() {
				IConfiguration ret = new Configuration();
				for (int ei = 0; ei < nbExterns; ei++) {
					ret.addExtern("e_" + ei);
				}
				for (int ni = 0; ni + nbExterns < nbHosters; ni++) {
					ret.addOnline("n_" + ni);
				}
				for (int vi = 0; vi < nbVMs; vi++) {
					ret.addVM("v_" + vi, null);
				}
				return ret;
			}

			@Override
			public boolean tryAdvance(Consumer<? super IConfiguration> action) {
				nbVMs++;
				if (maxVMPerHost > 0 && nbVMs > nbHosters * maxVMPerHost
		        || maxCfgSize > 0 && getSize(nbHosters, nbVMs) > maxCfgSize) {
					nbVMs = 1;
					nbExterns++;
					if (nbExterns > nbHosters) {
						nbExterns = 0;
						nbHosters++;
					}
					if (maxCfgSize > 0 && getSize(nbHosters, nbVMs) > maxCfgSize) {
						return false;
					}
				}
				action.accept(makeCfg());
				return true;
			}

			@Override
			public Spliterator<IConfiguration> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return Long.MAX_VALUE;
			}

		};

		return StreamSupport.stream(ret, false);
	}

	public static long getSize(IConfiguration cfg) {
		return getSize(cfg.nbNodes() + cfg.nbExterns(), cfg.nbVMs());
	}

	public static long getSize(long nbHosters, long nbVMs) {
		return nbHosters + nbVMs + (long) Math.pow(nbHosters, nbVMs);
	}

	/**
	 * creates a stream of ResourceSpecification to add to a configuration.
	 *
	 * @param cfg
	 *          the configuration to make the resources for
	 * @param resName
	 *          the name of the resource specification
	 * @param maxNodeRes
	 *          the maximum total resource of the nodes
	 * @param maxVmLoadPct
	 *          maximum load of the nodes (ie max 100*vmtotaluse/nodetotalUse),
	 *          limited from 0 to 100.
	 * @return a new Stream.
	 */
	public static Stream<ResourceSpecification> streamResource(IConfiguration cfg, String resName, int maxNodeRes,
	    int maxVmLoadPct) {
		if (maxNodeRes < cfg.nbNodes() || maxNodeRes < cfg.nbVMs()) {
			return Stream.empty();
		}
		Solver s = new Solver();

		Node[] nodes = cfg.getNodes().collect(Collectors.toList()).toArray(new Node[] {});
		IntVar[] nodeCaps = new IntVar[nodes.length];
		// sum of the node capas from 0 to i
		IntVar[] nodeCapCumuls = new IntVar[nodes.length];
		VM[] vms = cfg.getVMs().collect(Collectors.toList()).toArray(new VM[] {});
		IntVar[] vmUses = new IntVar[vms.length];
		IntVar[] vmUseCumuls = new IntVar[vms.length];
		Extern[] externs = cfg.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
		IntVar[] externCaps = new IntVar[externs.length];
		for (int i = 0; i < nodes.length; i++) {
			nodeCaps[i] = VF.bounded("n_" + nodes[i].getName(), 1, maxNodeRes, s);
			if (i != 0) {
				// node caps are decreasing
				s.post(ICF.arithm(nodeCaps[i - 1], ">=", nodeCaps[i]));
				// cumul Node caps
				nodeCapCumuls[i] = VF.bounded("nc_" + nodes[i].getName(), 1, maxNodeRes, s);
				s.post(ICF.sum(new IntVar[] {
				    nodeCaps[i], nodeCapCumuls[i - 1]
				}, nodeCapCumuls[i]));
			} else {
				nodeCapCumuls[0] = nodeCaps[0];
			}
		}

		for (int i = 0; i < vms.length; i++) {
			vmUses[i] = VF.bounded("v_" + vms[i].getName(), 1, maxNodeRes, s);
			if (i != 0) {
				// vm uses are decreasing
				s.post(ICF.arithm(vmUses[i - 1], ">=", vmUses[i]));
				// cumul vm uses
				vmUseCumuls[i] = VF.bounded("vc_" + vms[i].getName(), 1, maxNodeRes, s);
				s.post(ICF.sum(new IntVar[] {
				    vmUses[i], vmUseCumuls[i - 1]
				}, vmUseCumuls[i]));
			} else {
				vmUseCumuls[0] = vmUses[0];
			}
			s.post(ICF.arithm(vmUseCumuls[i], "<=", nodeCapCumuls[i < nodes.length ? i : nodes.length - 1]));
		}
		s.post(ICF.arithm(VF.scale(vmUseCumuls[vmUseCumuls.length - 1], 100), "<=",
		    VF.scale(nodeCapCumuls[nodeCapCumuls.length - 1], Math.min(Math.max(maxVmLoadPct, 0), 100))));

		SetVar vmsUses = VF.set("vmUses", 1, maxNodeRes, s);
		s.post(SCF.int_values_union(vmUses, vmsUses));
		for (int i = 0; i < externs.length; i++) {
			externCaps[i] = VF.bounded("e_" + externs[i].getName(), 1, maxNodeRes, s);
			s.post(SCF.member(externCaps[i], vmsUses));
			if (i != 0) {
				// extern caps are decreasing
				s.post(ICF.arithm(externCaps[i - 1], ">=", externCaps[i]));
			}
		}
		return nextSolutions(s).map(sol -> {
			ResourceSpecification ret = new MappedResourceSpecification(resName);
			for (int i = 0; i < nodes.length; i++) {
				ret.capacity(nodes[i], sol.getIntVal(nodeCaps[i]));
			}
			if (externs.length > 0) {
				for (int i = 0; i < externs.length; i++) {
					ret.capacity(externs[i], sol.getIntVal(externCaps[i]));
				}
			}
			for (int i = 0; i < vms.length; i++) {
				ret.use(vms[i], sol.getIntVal(vmUses[i]));
			}
			return ret;
		});
	}

	/**
	 * generate a stream of configuration
	 *
	 * @param maxVMPerHost
	 *          max number of VM per hoster, only checked if >0.
	 * @param maxCfgSize
	 *          maximum size of the configuration, only checked if >0. see
	 *          {@link #getSize(IConfiguration)}.
	 * @param resName
	 *          the name of the resource specification
	 * @param maxResFunc
	 *          a function that give, according to the elements, the max capacity
	 *          of the resource.
	 * @param maxVmLoadPct
	 *          maximum load of the nodes (ie max 100*vmtotaluse/nodetotalUse),
	 *          limited from 0 to 100.
	 * @return
	 */
	@SafeVarargs
	public static Stream<IConfiguration> streamConfigurations(int maxVMPerHost, int maxCfgSize, String resName,
	    Function<IConfiguration, Integer> maxResFunc, int maxVmLoadPct, Predicate<IConfiguration>... elementCheckers) {
		Predicate<IConfiguration> checker = c -> true;
		if (elementCheckers != null) {
			for (Predicate<IConfiguration> p : elementCheckers) {
				checker = checker.and(p);
			}
		}
		return streamElements(maxVMPerHost, maxCfgSize).filter(checker)
		    .flatMap(c -> streamResource(c, resName, maxResFunc.apply(c), maxVmLoadPct).map(s ->
		{
			    IConfiguration c2 = c.clone();
			    c2.resources().put(s.getType(), s);
			    return c2;
		    }));
	}
}

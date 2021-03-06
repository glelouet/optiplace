
package fr.emn.optiplace.eval;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
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
	public static Stream<Solution> nextSolutions(Model s) {
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
				Solution ret = s.getSolver().findSolution();
				if (ret != null) {
					action.accept(ret);
				}
				return ret != null;
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
	public static <T> Stream<T> nextSolutions(Model s, Function<Solution, T> mapper) {
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
					ret.addComputer("n_" + ni);
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
		return getSize(cfg.nbComputers() + cfg.nbExterns(), cfg.nbVMs());
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
	 * @param maxComputerRes
	 *          the maximum total resource of the computers
	 * @param maxVmLoadPct
	 *          maximum load of the computers (ie max
	 *          100*vmtotaluse/computertotalUse), limited from 0 to 100.
	 * @return a new Stream.
	 */
	public static Stream<ResourceSpecification> streamResource(IConfiguration cfg, String resName, int maxComputerRes,
			int maxVmLoadPct, int nbTotalResValues) {
		if (maxComputerRes < cfg.nbComputers() || maxComputerRes < cfg.nbVMs()) {
			return Stream.empty();
		}
		Model s = new Model();

		Computer[] computers = cfg.getComputers().collect(Collectors.toList()).toArray(new Computer[] {});
		IntVar[] computerCaps = new IntVar[computers.length];
		// sum of the computer capas from 0 to i
		IntVar[] computerCapCumuls = new IntVar[computers.length];
		VM[] vms = cfg.getVMs().collect(Collectors.toList()).toArray(new VM[] {});
		IntVar[] vmUses = new IntVar[vms.length];
		IntVar[] vmUseCumuls = new IntVar[vms.length];
		Extern[] externs = cfg.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
		IntVar[] externCaps = new IntVar[externs.length];
		for (int i = 0; i < computers.length; i++) {
			computerCaps[i] = s.intVar("n_" + computers[i].getName(), 1, maxComputerRes, true);
			if (i != 0) {
				// computer caps are decreasing
				s.post(s.arithm(computerCaps[i - 1], ">=", computerCaps[i]));
				// cumul Computer caps
				computerCapCumuls[i] = s.intVar("nc_" + computers[i].getName(), 1, maxComputerRes, true);
				s.post(s.sum(new IntVar[] {
						computerCaps[i], computerCapCumuls[i - 1] }, "=", computerCapCumuls[i]));
			} else {
				computerCapCumuls[0] = computerCaps[0];
			}
		}

		for (int i = 0; i < vms.length; i++) {
			vmUses[i] = s.intVar("v_" + vms[i].getName(), 1, maxComputerRes, true);
			if (i != 0) {
				// vm uses are decreasing
				s.post(s.arithm(vmUses[i - 1], ">=", vmUses[i]));
				// cumul vm uses
				vmUseCumuls[i] = s.intVar("vc_" + vms[i].getName(), 1, maxComputerRes, true);
				s.post(s.sum(new IntVar[] {
						vmUses[i], vmUseCumuls[i - 1]
				}, "=", vmUseCumuls[i]));
			} else {
				vmUseCumuls[0] = vmUses[0];
			}
			s.post(s.arithm(vmUseCumuls[i], "<=", computerCapCumuls[i < computers.length ? i : computers.length - 1]));
		}
		s.post(s.arithm(s.intScaleView(vmUseCumuls[vmUseCumuls.length - 1], 100), "<=",
				s.intScaleView(computerCapCumuls[computerCapCumuls.length - 1], Math.min(Math.max(maxVmLoadPct, 0), 100))));

		// do we need to have a limited number of values for cumul VM/Computer load?
		if (nbTotalResValues > 1) {
			int minVMLoad = vms.length, maxVMLoad = maxComputerRes * maxVmLoadPct / 100;
			int[] allowedVMCumul = new int[nbTotalResValues];
			for (int i = 0; i < nbTotalResValues; i++) {
				allowedVMCumul[i] = minVMLoad + (maxVMLoad - minVMLoad) * i / (nbTotalResValues - 1);
			}
			IntVar vmTotalCumul = s.intVar("vmTotalUse", allowedVMCumul);
			s.post(s.arithm(vmTotalCumul, "=", vmUseCumuls[vms.length - 1]));

			int minComputerCumul = Math.max(computers.length, vms.length);
			int maxComputerCumul = maxComputerRes;
			int[] allowedComputerCumul = new int[nbTotalResValues];
			for (int i = 0; i < nbTotalResValues; i++) {
				allowedComputerCumul[i] = minComputerCumul + (maxComputerCumul - minComputerCumul) * i / (nbTotalResValues - 1);
			}
			IntVar computerTotalCumul = s.intVar("computerTotalUse", allowedComputerCumul);
			s.post(s.sum(computerCaps, "=", computerTotalCumul));
		}

		// set the extern values
		SetVar vmsUses = s.setVar("vmUses", IntStream.range(1, maxComputerRes + 1).toArray());
		s.post(s.union(vmUses, vmsUses));
		for (int i = 0; i < externs.length; i++) {
			externCaps[i] = s.intVar("e_" + externs[i].getName(), 1, maxComputerRes, true);
			s.post(s.member(externCaps[i], vmsUses));
			if (i != 0) {
				// extern caps are decreasing
				s.post(s.arithm(externCaps[i - 1], ">=", externCaps[i]));
			}
		}
		return nextSolutions(s).map(sol -> {
			ResourceSpecification ret = new MappedResourceSpecification(resName);
			for (int i = 0; i < computers.length; i++) {
				ret.capacity(computers[i], sol.getIntVal(computerCaps[i]));
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
	 *          maximum load of the computers (ie max
	 *          100*vmtotaluse/computertotalUse), limited from 0 to 100.
	 * @return
	 */
	@SafeVarargs
	public static Stream<IConfiguration> streamConfigurations(int maxVMPerHost, int maxCfgSize, String resName,
			Function<IConfiguration, Integer> maxResFunc, int maxVmLoadPct, int nbTotalResValues,
			Predicate<IConfiguration>... elementCheckers) {
		Predicate<IConfiguration> checker = c -> true;
		if (elementCheckers != null) {
			for (Predicate<IConfiguration> p : elementCheckers) {
				checker = checker.and(p);
			}
		}
		return streamElements(maxVMPerHost, maxCfgSize).filter(checker)
				.flatMap(c -> streamResource(c, resName, maxResFunc.apply(c), maxVmLoadPct, nbTotalResValues).map(s ->
				{
					IConfiguration c2 = c.clone();
					c2.resources().put(s.getType(), s);
					return c2;
				}));
	}
}

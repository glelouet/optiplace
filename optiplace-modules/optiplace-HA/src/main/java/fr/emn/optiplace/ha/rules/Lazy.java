package fr.emn.optiplace.ha.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.resources.ResourceLoad;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * prevents a set of {@link Computer}s to run at a maximum load.
 *
 * @author guillaume Le Louët
 */
public class Lazy implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Lazy.class);

	public static final Pattern pat = Pattern.compile("lazy\\[(.*)\\]\\((.*)\\)\\((.*)\\)");

	public static Lazy parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<Computer> nodes = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new Computer(n)).collect(Collectors.toSet());
		return new Lazy(m.group(2), Integer.parseInt(m.group(3)), nodes);
	}

	public static final Parser PARSER = def -> Lazy.parse(def);

	protected Set<Computer> nodes;

	private String resName;

	/** the max percentage of load a specified node can be used at */
	private int maxPCLoad;

	/**
	 * construct a constraint with internal parameters
	 *
	 * @param maxPCLoad
	 *          the max percentage load (integer) of the host's CPU.
	 * @param nodes
	 *          the set of nodes to apply this constraint on
	 */
	public Lazy(String resName, int maxPCLoad, Set<Computer> nodes) {
		this.nodes = nodes;
		this.resName = resName;
		this.maxPCLoad = maxPCLoad;
	}

	/**
	 * easier to use constructor that produces an internal vjobset
	 *
	 * @param nodes
	 *          the array of nodes to convert to a {@link VJobSet}
	 * @see #LazyComputer(int, VJobSet)
	 */
	public Lazy(String resName, int maxPCLoad, Computer... nodes) {
		this(resName, maxPCLoad, new HashSet<>(Arrays.asList(nodes)));
	}

	/** @return the maxPCLoad */
	public int getMaxPCLoad() {
		return maxPCLoad;
	}

	/**
	 * @param maxPCLoad
	 *          the maxPCLoad to set
	 */
	public void setMaxPCLoad(int maxPCLoad) {
		this.maxPCLoad = maxPCLoad;
	}

	public void setResName(String name) {
		resName = name;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		List<Computer> nodes = new ArrayList<>(this.nodes);
		nodes.removeIf(n -> !core.getSourceConfiguration().hasComputer(n));
		if (nodes.size() == 0) {
			return;
		}
		ResourceSpecification spec = core.getResourceSpecification(resName);
		if (spec == null) {
			logger.warn("no resource handler for " + resName + " on " + this);
			return;
		}
		ResourceLoad uses = core.getUse(spec.getType());
		for (Computer n : nodes) {
			int capa = spec.getCapacity(n) * maxPCLoad / 100;
			IntVar use = uses.getComputersLoad()[core.b().location(n)];
			core.post(core.getModel().arithm(use, "<=", capa));
		}
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		ResourceSpecification res = cfg.resources().get(resName);
		return !nodes.stream().filter(cfg::hasComputer)
				.filter(n -> 100 * res.getUse(cfg, n) > res.getCapacity(n) * maxPCLoad)
				.findAny().isPresent();
	}

	@Override
	public String toString() {
		return "lazy" + nodes + "(" + resName + ")(" + getMaxPCLoad() + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != Lazy.class) {
			return false;
		}
		Lazy other = (Lazy) obj;
		return other.maxPCLoad == maxPCLoad && other.resName.equals(resName) && other.nodes.equals(nodes);
	}
}

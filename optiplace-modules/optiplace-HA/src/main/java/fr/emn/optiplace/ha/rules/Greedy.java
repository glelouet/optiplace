package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * a placement constraint that requires VMs to be placed on hosts with a given
 * free amount of CPU.
 *
 * @author guillaume
 */
public class Greedy implements Rule {

  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
      .getLogger(Greedy.class);

	public static final Pattern pat = Pattern
.compile("greedy\\[(.*)\\]\\((.*)\\)\\((.*)\\)");

	public static Greedy parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream()
				.map(n -> new VM(n)).collect(Collectors.toSet());
		return new Greedy(Integer.parseInt(m.group(2)), m.group(3), vms);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public Greedy parse(String def) {
			return Greedy.parse(def);
		}
	};

	protected int maxHostLoad;

	protected Set<VM> vms;

	protected String resource = "CPU";

	public Greedy(int maxHostLoad, String resource, Set<VM> vms) {
    this.maxHostLoad = maxHostLoad;
		this.resource = resource;
		this.vms = vms;
  }

	public Greedy(int maxHostLoad, String resource, VM... vms) {
		this(maxHostLoad, resource, new HashSet<>(Arrays.asList(vms)));
  }

  /** @return the max load allowed for the VMs' host */
  public double getMaxHostLoad() {
    return maxHostLoad;
  }

  @Override
  public void inject(IReconfigurationProblem core) {
    Solver s = core.getSolver();
		vms.stream().filter(core.getSourceConfiguration()::hasVM).forEach(vm -> {
			int vmi = core.b().vm(vm);
			IntVar times100load = core.v().mult(core.getHostUse(resource, vmi), 100);
			IntVar timesPCcapa = core.v().mult(core.getHostCapa(resource, vmi), maxHostLoad);
      s.post(ICF.arithm(times100load, "<=", timesPCcapa));
		});
  }

  @Override
  public boolean isSatisfied(IConfiguration cfg) {
		ResourceSpecification specs = cfg.resources().get(resource);
		if (vms.size() == 0) {
      logger.debug("No virtual machines was specified");
      return true;
    }
		for (VM vm : vms) {
      if (cfg.isRunning(vm)) {
				Node n = cfg.getNodeHost(vm);
        int nodeCapa = specs.getCapacity(n);
        if (specs.getUse(cfg, n) / nodeCapa > getMaxHostLoad()) {
          return false;
        }
      }
    }
    return true;
  }

	@Override
	public String toString() {
		return "greedy" + vms + "(" + maxHostLoad + ")(" + resource + ")";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.getClass() != Greedy.class) {
			return false;
		}
		Greedy other = (Greedy) obj;
		return other.maxHostLoad == maxHostLoad && other.vms.equals(vms) && other.resource.equals(resource);
	}
}

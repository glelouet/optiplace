package fr.emn.optiplace.ha.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * a Rule to request a given set of VM are all hosted on a different site
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class Far implements Rule {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Far.class);

	public static final Pattern pat = Pattern.compile("far\\[(.*)\\]");

	public static Far parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		return new Far(vms);
	}

	public static final Parser PARSER = def -> Far.parse(def);

	public static final String SUPPORT_TAG = "support:ha/far";

	protected Set<VM> vms;

	public Far(Set<VM> vms) {
		this.vms = vms;
	}

	public Far(VM... virtualMachines) {
		this(new HashSet<>(Arrays.asList(virtualMachines)));
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		return vms.stream().filter(cfg::hasVM).map(v -> cfg.getSite(cfg.getNodeHost(v))).distinct().count() == vms.stream()
				.filter(cfg::hasVM).count();
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		ArrayList<IntVar> sites = new ArrayList<>();
		for (VM v : vms) {
			if (core.getSourceConfiguration().hasVM(v)) {
				sites.add(core.getSite(v));
			}
		}
		// TODO check for the support_tag
		core.post(ICF.alldifferent(sites.toArray(new IntVar[] {})));
	}

	@Override
	public String toString() {
		return "far" + vms;
	}
}

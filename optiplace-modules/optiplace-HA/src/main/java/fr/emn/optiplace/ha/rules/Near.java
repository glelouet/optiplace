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
 * a Rule to request a given set of VM are all hosted on the same site
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class Near implements Rule {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Near.class);

	public static final Pattern pat = Pattern.compile("near\\[(.*)\\]");

	public static Near parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		return new Near(vms);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public Near parse(String def) {
			return Near.parse(def);
		}
	};

	protected Set<VM> vms;

	public Near(Set<VM> vms) {
		this.vms = vms;
	}

	public Near(VM... vms) {
		this(new HashSet<>(Arrays.asList(vms)));
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		return vms.stream().filter(cfg::hasVM).map(v -> cfg.getSite(cfg.getNodeHost(v))).distinct().count() == 1;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		ArrayList<IntVar> sites = new ArrayList<IntVar>();
		for (VM v : vms) {
			if (core.getSourceConfiguration().hasVM(v)) {
				sites.add(core.getSite(v));
			}
		}
		for (int i = 1; i < sites.size(); i++) {
			core.post(ICF.arithm(sites.get(0), "=", sites.get(i)));
		}
	}

	@Override
	public String toString() {
		return "near" + vms;
	}
}

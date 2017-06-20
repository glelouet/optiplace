package fr.emn.optiplace.ha.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
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

	public static final Parser PARSER = def -> Near.parse(def);

	public static final String SUPPORT_TAG = "support:ha/near";

	protected Set<VM> vms;

	public Near(Set<VM> vms) {
		this.vms = vms;
	}

	public Near(VM... vms) {
		this(new HashSet<>(Arrays.asList(vms)));
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		return vms.stream().filter(cfg::hasVM).map(v -> cfg.getSite(cfg.getComputerHost(v))).distinct().count() == 1;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		ArrayList<IntVar> vmSites = new ArrayList<>();
		for (VM v : vms) {
			if (core.getSourceConfiguration().hasVM(v)) {
				vmSites.add(core.getVMSite(v));
			}
		}
		for (int i = 1; i < vmSites.size(); i++) {
			core.post(core.getModel().arithm(vmSites.get(0), "=", vmSites.get(i)));
		}
		// manage the support_tag : the group of VMs should not be placed on an
		// extern that does not support this rule.
		int[] forbiddenExterns = core.c().getExterns().filter(e -> !core.c().isTagged(e, SUPPORT_TAG))
				.mapToInt(core.b()::location).toArray();
		for(VM v : vms){
			IntVar externPosition = core.getVMLocation(v);
			for(int e : forbiddenExterns) {
				try {
					externPosition.removeValue(e, Cause.Null);
				} catch (ContradictionException e1) {
					logger.warn("", e1);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "near" + vms;
	}
}

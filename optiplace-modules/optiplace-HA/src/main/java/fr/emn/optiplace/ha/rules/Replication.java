package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * Enforce VM are always being replicated to another node. We consider a VM is
 * replicating if it is migrating, as the replication is a property of the
 * migration.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class Replication implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Replication.class);

	public static final Pattern pat = Pattern.compile("replicating\\[(.*)\\]");

	public static Replication parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		return new Replication(vms);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public Replication parse(String def) {
			return Replication.parse(def);
		}
	};

	protected Set<VM> vms;

	/**
	 * @param nodes
	 * @param VMs
	 */
	public Replication(Set<VM> vms) {
		this.vms = vms;
	}

	/**
	 * create an HAVM on at least one VM.
	 *
	 * @param vm
	 *          the first VM or null iff we directly want to create from an
	 *          existing array
	 * @param othervms
	 *          the potentieal remaining VMs
	 */
	public Replication(VM... vms) {
		this(new HashSet<>(Arrays.asList(vms)));
	}

	public Stream<VM> getVMs() {
		return vms.stream();
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		for (VM v : vms) {
			if (cfg.hasVM(v) && !cfg.isMigrating(v)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		vms.stream().filter(core.c()::hasVM).filter(v -> !core.c().isMigrating(v)).forEach(v -> {
			try {
				core.isMigrated(v).instantiateTo(1, Cause.Null);
			} catch (ContradictionException e) {
				logger.warn("error : can't set VM " + v + " to migrating");
			}
		});
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + vms;
	}
}

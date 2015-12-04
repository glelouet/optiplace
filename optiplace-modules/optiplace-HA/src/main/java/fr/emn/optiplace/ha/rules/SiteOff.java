
package fr.emn.optiplace.ha.rules;

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
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;


/**
 * given VMs are not placed on nodes from given site.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class SiteOff implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SiteOff.class);

	public static final Pattern pat = Pattern.compile("siteoff\\[(.*)\\]\\((.*)\\)");

	public static SiteOff parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Site site = new Site(m.group(1));
		Set<VM> vms = Arrays.asList(m.group(2).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		return new SiteOff(site, vms);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public SiteOff parse(String def) {
			return SiteOff.parse(def);
		}
	};

	protected Site site;

	protected Set<VM> vms;

	public SiteOff(Site site, VM... vms) {
		this(site, new HashSet<>(Arrays.asList(vms)));
	}

	public SiteOff(Site site, Set<VM> vms) {
		this.site = site;
		this.vms = vms;
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		for (VM v : vms) {
			if (!cfg.hasVM(v)) {
				continue;
			}
			VMHoster location = cfg.getFutureLocation(v);
			if (site == null && cfg.getSite(location) == null) {
				return false;
			}
			if (site != null && site.equals(cfg.getSite(location))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		int siteIdx = core.b().site(site);
		IConfiguration cfg = core.getSourceConfiguration();
		for (VM v : vms) {
			if (!cfg.hasVM(v)) {
				continue;
			}
			try {
				IntVar vmSite = core.getSite(v);
				vmSite.removeValue(siteIdx, Cause.Null);
			}
			catch (ContradictionException e) {
				logger.warn("while injecting " + this);
				throw new UnsupportedOperationException(e);
			}
		}
	}

	@Override
	public String toString() {
		return "siteoff[" + site + "]" + vms;
	}
}

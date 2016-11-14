/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.ha.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * Place a set of nodes/site/extern under quarantine. In this settings, no new
 * VMs can be hosted on those. Hosted VMs can not leave their nodes/extern. The
 * only solution will be to terminate them. TODO: What if these nodes become
 * saturated ? set the VM consumption to 1 on these nodes ?
 *
 * @author Fabien Hermenier
 */
public class Quarantine implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Quarantine.class);

	public static final Pattern pat = Pattern.compile("quarantine\\[(.*)\\]");

	public static Quarantine parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<String> hosters = Arrays.asList(m.group(1).split(", ")).stream().collect(Collectors.toSet());
		return new Quarantine(hosters);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public Quarantine parse(String def) {
			return Quarantine.parse(def);
		}
	};

	protected Set<String> locations;

	public Quarantine(Set<String> locations) {
		this.locations = locations;
	}

	public Quarantine(String... locations) {
		this(new HashSet<>(Arrays.asList(locations)));
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		// root for the VMs already hosted on the specified hosts
		// ban the other VMs
		IConfiguration cfg = core.getSourceConfiguration();
		List<Integer> nodeIdx = new ArrayList<>();
		List<Integer> siteIdx = new ArrayList<>();
		List<Integer> externIdx = new ArrayList<>();
		for (String name : locations) {
			ManagedElement e = cfg.getElementByName(name);
			if (e instanceof Node) {
				nodeIdx.add(core.b().node((Node) e));
			} else if (e instanceof Extern) {
				externIdx.add(core.b().extern((Extern) e));
			} else if (e instanceof Site) {
				siteIdx.add(core.b().site((Site) e));
			} else {
				logger.warn("can't quarantine element with name " + name + " as its type" + e.getClass() + " is not in switch");
			}
		}
		cfg.getVMs().forEach(vm -> {
			VMHoster h = cfg.getFutureLocation(vm);
			Site site = cfg.getSite(h);
			try {
				if (h != null && (locations.contains(h.getName()) || site != null && locations.contains(site.getName()))) {
					core.isMigrated(vm).instantiateTo(0, Cause.Null);
				} else {
					int idx = core.b().vm(vm);
					if (!nodeIdx.isEmpty()) {
						IntVar nv = core.getNode(idx);
						for (int ni : nodeIdx) {
							nv.removeValue(ni, Cause.Null);
						}
					}
					if (!externIdx.isEmpty()) {
						IntVar ev = core.getExtern(idx);
						for (int ei : externIdx) {
							ev.removeValue(ei, Cause.Null);
						}
					}
					if (!siteIdx.isEmpty()) {
						IntVar sv = core.getVMSite(idx);
						for (int si : siteIdx) {
							sv.removeValue(si, Cause.Null);
						}
					}
				}
			} catch (ContradictionException e) {
				logger.error(e.getMessage(), e);
			}
		});
	}

	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Quarantine q = (Quarantine) o;

		return locations.equals(q.locations);
	}

	@Override
	public int hashCode() {
		return "quarantine".hashCode() * 31 + locations.hashCode();
	}

	@Override
	public String toString() {
		return "quarantine" + locations;
	}

}

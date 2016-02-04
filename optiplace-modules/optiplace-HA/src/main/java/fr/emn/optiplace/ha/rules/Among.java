/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VF;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * A constraint to enforce a set of virtual machines to be hosted on a single
 * group of physical elements among those given in parameters.
 *
 * @author Fabien Hermenier
 */
public class Among implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Among.class);

	public static final Pattern pat = Pattern.compile("among\\[(.*)\\]\\[(.*)\\]");

	public static Among parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		Set<Set<String>> nodess = new HashSet<>();
		String sgroups = m.group(2);
		if (sgroups != null && sgroups.length() > 1) {
			for (String hosterName : sgroups.substring(1, sgroups.length() - 1).split("\\], \\[")) {
				Set<String> nodes = Stream.of(hosterName.split(", ")).collect(Collectors.toSet());
				nodess.add(nodes);
			}
		}
		return new Among(vms, nodess);
	}

	public static final Parser PARSER = def -> Among.parse(def);

	/** The set of possible groups of nodes. */
	private final Set<Set<String>> groups;

	private final Set<VM> vms;

	/**
	 * Make a new Rule that enforce all the virtual machines to be hosted on a
	 * single group of node among those given in parameters.
	 *
	 * @param vms
	 *          the set of VMs to assign.
	 * @param groups
	 *          the list of possible groups of nodes. If empty, the group of vm
	 *          must all be on the same node.
	 */
	public Among(Set<VM> vms, Set<Set<String>> groups) {
		this.groups = groups;
		this.vms = vms;
	}

	public Among(Set<VM> vms, String[]... groups) {
		this.vms = vms;
		this.groups = new HashSet<>();
		if (groups != null) {
			for (String[] ar : groups) {
				HashSet<String> set = new HashSet<>();
				for (String s : ar) {
					set.add(s);
				}
				this.groups.add(set);
			}
		}
	}

	public Among(String hostername, VM... vms) {
		this(new HashSet<>(Arrays.asList(vms)), hostername == null ? null : new String[] { hostername });
	}

	/**
	 * Get the different groups of nodes involved in the constraint.
	 *
	 * @return a set of groups. May be empty
	 */
	public Set<Set<String>> getGroups() {
		return groups;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("among").append(vms).append(groups);
		return buffer.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Among that = (Among) o;
		return groups.equals(that.groups) && vms.equals(that.vms);
	}

	@Override
	public int hashCode() {
		int result = vms.hashCode();
		result = 31 * result + groups.hashCode();
		result = 31 * result + "among".hashCode();
		return result;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		Set<Set<String>> groups = this.groups;
		if (groups.isEmpty()) {
			groups = core.getSourceConfiguration().getOnlines().map(n -> Collections.singleton(n.getName()))
					.collect(Collectors.toSet());
		}
		// Get the nodes of the VMs
		IConfiguration cfg = core.getSourceConfiguration();
		List<IntVar> hostersl = vms.stream().filter(cfg::hasVM).map(core::getHoster).collect(Collectors.toList());
		// we transform them to a SetVar
		SetVar hosters = core.v().toSet(hostersl.toArray(new IntVar[] {}));
		SetVar[] possibleLocations = new SetVar[groups.size()];
		int i = 0;
		for (Set<String> s : groups) {
			List<Integer> l = s.stream().map(n -> core.b().vmHoster((VMHoster) cfg.getElementByName(n)))
					.collect(Collectors.toList());
			int[] env = new int[l.size()];
			for (int j = 0; j < l.size(); j++) {
				env[j] = l.get(j);
			}
			possibleLocations[i] = VF.set("among_nodeset_" + i, env, core.getSolver());
			i++;
		}
		core.post(SCF.member(possibleLocations, hosters));
	}

	/**
	 * Check that the constraint is satisfied in a configuration.
	 *
	 * @param cfg
	 *          the configuration to check
	 * @return true if the running VMs are hosted on more than one group
	 */
	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		Set<String> locations = vms.stream().filter(cfg::isRunning).map(cfg::getLocation).map(VMHoster::getName)
				.collect(Collectors.toSet());
		if (groups.isEmpty() && locations.size() != vms.stream().filter(cfg::isRunning).count()) {
			return false;
		}
		List<Set<String>> matches = groups.stream().filter(s -> s.containsAll(locations)).collect(Collectors.toList());
		if (matches.size() != 1) {
			logger.debug("among " + this + " is not satisfied because matching groups should be 1 and are " + matches);
			return false;
		}
		return true;
	}
}
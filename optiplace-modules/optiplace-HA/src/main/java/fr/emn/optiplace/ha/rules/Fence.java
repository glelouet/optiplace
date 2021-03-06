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

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.set.hash.TIntHashSet;

/**
 * A constraint to enforce a set of virtual machines to be hosted on a group of
 * physical elements. Reduces the potential locations of the VM
 *
 * @author Fabien Hermenier
 */
public class Fence implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Fence.class);

	public static final Pattern pat = Pattern.compile("fence\\[(.*)\\]\\[(.*)\\]");

	public static Fence parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		Set<String> locations = Arrays.asList(m.group(2).split(", ")).stream().collect(Collectors.toSet());
		return new Fence(vms, locations);
	}

	public static final Parser PARSER = def -> Fence.parse(def);

	protected Set<VM> vms;

	protected Set<String> locations;

	/**
	 * Make a new constraint that enforce all the virtual machines to be hosted on
	 * a group of nodes.
	 *
	 * @param vms
	 *          the set of VMs to assign.
	 * @param group
	 *          the group of nodes.
	 */
	public Fence(Set<VM> vms, Set<String> group) {
		this.vms = vms;
		locations = group;
	}

	public Fence(String location, VM... vms) {
		this(new HashSet<>(Arrays.asList(vms)), Collections.singleton(location));
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();
		buffer.append("fence").append(vms).append(locations);
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
		Fence that = (Fence) o;
		return locations.equals(that.locations) && vms.equals(that.vms);
	}

	@Override
	public int hashCode() {
		int result = vms.hashCode();
		result = 31 * result + locations.hashCode();
		result = 31 * result + "fence".hashCode();
		return result;
	}

	@Override
	public void inject(IReconfigurationProblem core) {
		List<VM> runnings = vms.stream().filter(core.getSourceConfiguration()::hasVM).collect(Collectors.toList());
		if (runnings.isEmpty()) {
			return;
		}
		TIntArrayList iExclude = new TIntArrayList();
		TIntHashSet toKeep = new TIntHashSet(locations.size());
		for (String n : locations) {
			int idx = core.b().location((VMLocation) core.c().getElementByName(n));
			if (idx != -1) {
				toKeep.add(idx);
			}
		}
		core.getSourceConfiguration().getLocations().mapToInt(core.b()::location).filter(ni -> !toKeep.contains(ni))
		.forEach(ni -> iExclude.add(ni));

		// Domain restriction. Remove all the non-involved nodes
		for (VM vm : runnings) {
			IntVar hoster = core.getVMLocation(vm);
			iExclude.forEach(ni -> {
				try {
					hoster.removeValue(ni, Cause.Null);
				} catch (Exception e) {
					logger.warn("", e);
				}
				return true;
			});
		}
	}

	/**
	 * Check that the constraint is satified in a configuration.
	 *
	 * @param cfg
	 *          the configuration to check
	 * @return true if the running VMs are hosted on more than one group
	 */
	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		if (locations.isEmpty()) {
			logger.error("No group of nodes was specified");
			return false;
		}
		for (VM vm : vms) {
			if (cfg.isRunning(vm) && !locations.contains(cfg.getLocation(vm).getName())) {
				return false;
			}
		}
		return true;
	}
}

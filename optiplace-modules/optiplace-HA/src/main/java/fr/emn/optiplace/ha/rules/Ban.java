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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * A constraint to enforce a set of virtual machines to avoid to be hosted on a
 * group of VMHosters.
 *
 * @author Fabien Hermenier
 */
public class Ban implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Ban.class);

	public static final Pattern pat = Pattern.compile("ban\\[(.*)\\]\\[(.*)\\]");

	public static Ban parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		Set<String> hosters = Arrays.asList(m.group(2).split(", ")).stream().collect(Collectors.toSet());
		return new Ban(vms, hosters);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public Ban parse(String def) {
			return Ban.parse(def);
		}
	};

	private Set<VM> vms;
	private Set<String> hosters;

	/**
	 * Make a new constraint.
	 *
	 * @param vms
	 *          the VMs to assign
	 * @param nodes
	 *          the nodes to exclude
	 */
	public Ban(Set<VM> vms, Set<String> hosters) {
		this.vms = vms;
		this.hosters = hosters;
	}

	public Ban(Set<VM> vms, String... hosters) {
		this(vms, new HashSet<String>(Arrays.asList(hosters)));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Ban that = (Ban) o;

		return hosters.equals(that.hosters) && vms.equals(that.vms);
	}

	@Override
	public int hashCode() {
		int result = vms.hashCode();
		result = 31 * result + hosters.hashCode();
		result = 31 * result + "ban".hashCode();
		return result;
	}

	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder();

		buffer.append("ban").append(vms).append(hosters);
		return buffer.toString();
	}

	/**
	 * Apply the constraint to the plan to all the VMs in a future running state.
	 * FIXME: What about running VMs that will be suspended ?
	 *
	 * @param core
	 *          the plan to customize. Must implement
	 *          {@link entropy.solver.choco.ChocoCustomRP}
	 */
	@Override
	public void inject(IReconfigurationProblem core) {
		ArrayList<Node> nodes = new ArrayList<>();
		ArrayList<Extern> externs = new ArrayList<>();
		for (String s : hosters) {
			ManagedElement e = core.getSourceConfiguration().getElementByName(s);
			if (e instanceof Node)
				nodes.add((Node) e);
			else if (e instanceof Extern)
				externs.add((Extern) e);
		}

		List<Integer> nodesidx = nodes.stream().map(core.b()::node).collect(Collectors.toList());
		List<Integer> externsidx = externs.stream().map(core.b()::extern).collect(Collectors.toList());

		for (VM vm : vms) {
			if (core.getSourceConfiguration().hasVM(vm)) {
				IntVar node = core.getLocation(core.b().vm(vm));
				IntVar extern = core.getExtern(core.b().vm(vm));
				try {
					for (int i : nodesidx) {
						node.removeValue(i, Cause.Null);
					}
					for (int i : externsidx) {
						extern.removeValue(i, Cause.Null);
					}
				} catch (Exception e) {
					logger.warn("", e);
				}
			}
		}
	}

	/**
	 * Check that the constraint is satified in a configuration.
	 *
	 * @param cfg
	 *          the configuration to check
	 * @return true if the VMs are not running on the banned nodes.
	 */
	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		return !vms.stream().filter(cfg::isRunning).map(v -> cfg.getLocation(v).getName()).filter(hosters::contains)
				.findAny().isPresent();
	}
}

/* Copyright (c) 2010 Fabien Hermenier This file is part of Entropy. Entropy is
 * free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.ha.rules;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * A constraint to restrict the number of virtual machines a set of nodes can
 * host simultaneously.
 * <p/>
 *
 * @author Fabien Hermenier
 */
public class Capacity implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Capacity.class);

	public static final Pattern pat = Pattern.compile("capacity\\[(.*)\\]\\((.*)\\)");

	public static Capacity parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<Node> nodes = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new Node(n)).collect(Collectors.toSet());
		int max = Integer.parseInt(m.group(2));
		return new Capacity(nodes, max);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public Capacity parse(String def) {
			return Capacity.parse(def);
		}
	};

	private final int maxvms;

	private Set<Node> nodes;

	/**
	 * Make a new Rule.
	 *
	 * @param ns
	 *          the nodes to consider
	 * @param m
	 *          the maximum hosting capacity of all the nodes.
	 */
	public Capacity(Set<Node> ns, int m) {
		maxvms = m;
		this.nodes = ns;
	}

	@Override
	public String toString() {
		return new StringBuilder("capacity").append(nodes).append("(").append(maxvms).append(")").toString();
	}

	@Override
	public void inject(IReconfigurationProblem core) {

		IntVar[] hosteds = nodes.stream().filter(core.getSourceConfiguration()::hasNode).map(core::nbVMs)
				.collect(Collectors.toList()).toArray(new IntVar[] {});
		switch (hosteds.length) {
		case 0:
			break;
		case 1:
			try {
				hosteds[0].updateUpperBound(maxvms, Cause.Null);
			} catch (ContradictionException e) {
				throw new UnsupportedOperationException("can't reduce the number of VMs on " + hosteds[0] + " under " + maxvms);
			}
			break;
		default:
			IntVar sum = VF.bounded("sum of " + nodes + "#nbVMs", 0, Integer.MAX_VALUE, core.getSolver());
			core.getSolver().post(ICF.sum(hosteds, sum));
		}
	}

	/**
	 * Check that the nodes does not host a number of VMs greater than the maximum
	 * specified
	 *
	 * @param configuration
	 *          the configuration to check
	 * @return {@code true} if the constraint is satisfied.
	 */
	@Override
	public boolean isSatisfied(IConfiguration configuration) {
		int nb = 0;
		for (Node n : nodes) {
			nb += configuration.nbHosted(n);
		}
		if (nb > maxvms) {
			logger.debug(nodes + " host " + nb + " virtual machines but maximum allowed is " + maxvms);
			return false;
		}
		return true;
	}

	/**
	 * Get the maximum number of virtual machines the set of nodes can host
	 * simultaneously
	 *
	 * @return a positive integer
	 */
	public int getMaximumCapacity() {
		return maxvms;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		Capacity capacity = (Capacity) o;

		if (maxvms != capacity.maxvms) {
			return false;
		}
		if (nodes != null ? !nodes.equals(capacity.nodes) : capacity.nodes != null) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode() {
		int result = maxvms;
		result = 31 * result + nodes.hashCode();
		result = 31 * result + "capacity".hashCode();
		return result;
	}
}

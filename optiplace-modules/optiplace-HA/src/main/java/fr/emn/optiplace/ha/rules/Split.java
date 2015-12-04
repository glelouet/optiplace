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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.chocosolver.solver.constraints.set.SCF;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * prevent two sets of VM from being hosted on common Node. If the second set is
 * empty or null, then we consider it to be all the remaining VMs of a
 * configuration.
 *
 * @author Guillaume Le Louët
 */
public class Split implements Rule {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Split.class);

	public static final Pattern pat = Pattern.compile("split\\[(.*)\\]\\[(.*)\\]");

	public static Split parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vm1s = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		Set<VM> vm2s = Arrays.asList(m.group(2).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		return new Split(vm1s, vm2s);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public Split parse(String def) {
			return Split.parse(def);
		}
	};

	/** The first vmset. */
	protected Set<VM> set1;

	/** The second vmset. */
	protected Set<VM> set2;

	/**
	 * Make a new constraint.
	 *
	 * @param vmset1
	 *          the first set of virtual machines
	 * @param vmset2
	 *          the second set of virtual machines, or null to consider all the
	 *          other VM of a problem.
	 */
	public Split(Set<VM> vmset1, Set<VM> vmset2) {
		set1 = vmset1.isEmpty() ? null : vmset1;
		set2 = vmset2.isEmpty() ? null : vmset2;
		if (set1 == null) {
			set1 = set2;
			set2 = null;
		}
		// if set2!=null then no common element.
		assert set2 == null || !set1.parallelStream().filter(set2::contains).findAny().isPresent();
	}

	public Split(VM... vms) {
		this(new HashSet<>(Arrays.asList(vms)), null);
	}

	/**
	 * Get the first set of virtual machines.
	 *
	 * @return a set of VMs. Should not be empty
	 */
	public final Set<VM> getFirstSet() {
		return set1;
	}

	/**
	 * Get the second set of virtual machines.
	 *
	 * @return a set of VMs. Should not be empty
	 */
	public final Set<VM> getSecondSet() {
		return set2;
	}

	@Override
	public String toString() {
		return new StringBuilder("split").append(set1).append(set2).toString();
	}

	/**
	 * Check that the constraint is satified in a configuration.
	 *
	 * @param cfg
	 *          the configuration to check
	 * @return true if the VMs are hosted on distinct group of nodes
	 */
	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		Set<VMHoster> nodes1, nodes2;
		nodes1 = getStream1(cfg).map(cfg::getFutureLocation).collect(Collectors.toSet());
		nodes2 = getStream2(cfg).map(cfg::getFutureLocation).collect(Collectors.toSet());
		nodes1.retainAll(nodes2);
		return nodes1.isEmpty();
	}

	/**
	 * TODO documentation
	 *
	 * @param core
	 */
	@Override
	public void inject(IReconfigurationProblem core) {
		SetVar minusOneSet = core.v().createEnumSetVar("{-1}", -1);
		List<IntVar> l1 = getStream1(core.getSourceConfiguration()).map(core::getHoster).collect(Collectors.toList());
		SetVar s1 = core.v().toSet(l1.toArray(new IntVar[] {}));
		// s1b = s1 \ {-1}
		SetVar s1b = s1.duplicate();
		core.post(SetConstraintsFactory.partition(new SetVar[] { minusOneSet, s1b }, s1));
		List<IntVar> l2 = getStream2(core.getSourceConfiguration()).map(core::getHoster).collect(Collectors.toList());
		SetVar s2 = core.v().toSet(l2.toArray(new IntVar[] {}));
		// s2b = s2 \ {-1}
		SetVar s2b = s2.duplicate();
		core.post(SetConstraintsFactory.partition(new SetVar[] { minusOneSet, s2b }, s2));
		core.post(SCF.disjoint(s1b, s2b));
	}

	/**
	 * @param core
	 *          a reconfiguration problem
	 * @return a Stream of the group 1's VM location in the problem
	 */
	public Stream<VM> getStream1(IConfiguration cfg) {
		return set1.stream().filter(cfg::hasVM);
	}

	/**
	 *
	 * @param core
	 *          a reconfiguration problem
	 * @return a Stream of the group 2's VM location in the problem
	 */
	public Stream<VM> getStream2(IConfiguration cfg) {
		return set2 != null ? set2.stream().filter(cfg::hasVM) : cfg.getVMs().filter(v -> !set1.contains(v));
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Split that = (Split) o;
		return getFirstSet().equals(that.getFirstSet()) && getSecondSet().equals(that.getSecondSet());
	}

	@Override
	public int hashCode() {
		int result = getFirstSet().hashCode();
		result = 31 * result + getSecondSet().hashCode();
		result = 31 * result + "lazySplit".hashCode();
		return result;
	}
}

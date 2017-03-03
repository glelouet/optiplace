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

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.nary.alldifferent.conditions.Condition;
import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/**
 * A constraint to ensure a set of VMs will be hosted on different hosters.
 *
 * @author Fabien Hermenier
 */
public class Spread implements Rule {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Spread.class);

	public static final Pattern pat = Pattern.compile("spread\\[(.*)\\]");

	public static Spread parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream().map(n -> new VM(n)).collect(Collectors.toSet());
		return new Spread(vms);
	}

	public static final Parser PARSER = def -> Spread.parse(def);

	public static final String SUPPORT_TAG = "support:ha/spread";

	protected Set<VM> vms;

	/**
	 * Make a new constraint.
	 *
	 * @param vms
	 *          the involved virtual machines
	 */
	public Spread(Set<VM> vms) {
		this.vms = vms;
	}

	public Spread(VM... vms) {
		this(new HashSet<>(Arrays.asList(vms)));
	}

	/**
	 * Check that the constraint is satified in a configuration.
	 *
	 * @param cfg
	 *          the configuration to check
	 * @return true if the running VMs are hosted on distinct nodes
	 */
	@Override
	public boolean isSatisfied(IConfiguration cfg) {
		Set<VMLocation> hosters = new HashSet<>();
		for (VM vm : vms) {
			VMLocation h = cfg.getFutureLocation(vm);
			if (h != null) {
				if (!hosters.add(h)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Apply the constraint to the plan if the VM must be in the running state.
	 * The constraint is applied on all the future running virtual machines given
	 * at instantiation. Others are ignored.
	 *
	 * @param core
	 *          the plan to customize. Must implement
	 *          {@link entropy.solver.choco.ChocoCustomRP}
	 */
	@Override
	public void inject(IReconfigurationProblem core) {

		// Get only the future running VMS
		IConfiguration cfg = core.getSourceConfiguration();
		List<IntVar> locations = vms.stream().filter(cfg::hasVM).map(core::getVMLocation).collect(Collectors.toList());
		if (locations.isEmpty()) {
			logger.debug(this + " is entailed. No VMs are running");
		} else {
			Condition noWait = a -> a.getLB() < core.b().waitIdx();
			if (!locations.isEmpty()) {
				Constraint adc = ICF.alldifferent_conditionnal(locations.toArray(new IntVar[] {}), noWait);
				core.getSolver().post(adc);
			}
// if (!extHosts.isEmpty()) {
// int[] withSupportTags = IntStream.concat(IntStream.of(-1),
// cfg.getExterns().filter(e -> cfg.isTagged(e, SUPPORT_TAG)).mapToInt(e -> core.b().location(e))).toArray();
// Condition noNegAndNoSupport = a -> {
// for (int i : withSupportTags) {
// if(a.contains(i)) {
// return false;
// }
// }
// return true;
// };
// Constraint adc = ICF.alldifferent_conditionnal(extHosts.toArray(new IntVar[] {}), noNegAndNoSupport);
// core.getSolver().post(adc);
// }
		}
	}

	@Override
	public String toString() {
		return new StringBuilder("spread").append(vms).toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		Spread that = (Spread) o;
		return vms.equals(that.vms);
	}

	@Override
	public int hashCode() {
		return vms.hashCode() + "Spread".hashCode() * 31;
	}
}

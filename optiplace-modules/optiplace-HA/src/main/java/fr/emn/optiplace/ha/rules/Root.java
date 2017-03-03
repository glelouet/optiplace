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
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.Rule;

/** A rule to avoid relocation. Any running VMs given in parameters will be
 * disallowed to be moved to another host. Other VMs are ignored.
 * <p/>
 * @author Fabien Hermenier */
public class Root implements Rule {

  private static final Logger logger = LoggerFactory.getLogger(Root.class);

	public static final Pattern pat = Pattern.compile("root\\[(.*)\\]");

	public static Root parse(String s) {
		Matcher m = pat.matcher(s);
		if (!m.matches()) {
			return null;
		}
		Set<VM> vms = Arrays.asList(m.group(1).split(", ")).stream()
				.map(n -> new VM(n)).collect(Collectors.toSet());
		return new Root(vms);
	}

	public static final Parser PARSER = new Parser() {

		@Override
		public Root parse(String def) {
			return Root.parse(def);
		}
	};

	protected Set<VM> vms;

  /**
	 * Make a new Rule.
	 *
	 * @param vms
	 *          the VMs to keep on their node
	 */
	public Root(Set<VM> vms) {
		this.vms = vms;
  }

	public Root(VM... vms) {
		this(new HashSet<>(Arrays.asList(vms)));
	}

  @Override
  public void inject(IReconfigurationProblem core) {
    for (VM vm : vms) {
			if (core.getSourceConfiguration().isRunning(vm)) {
				try {
					core.getLocation(vm).instantiateTo(core.b().getCurrentLocation(core.b().vm(vm)),
				      Cause.Null);
				} catch (ContradictionException e) {
				  logger.warn("", e);
				}
			}
    }
  }

  /**
   * Entailed method
   *
   * @param configuration
   *            the configuration to check
   * @return {@code true}
   */
  @Override
  public boolean isSatisfied(IConfiguration configuration) {
    return true;
  }

  @Override
  public String toString() {
		return new StringBuilder("root").append(vms).toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Root root = (Root) o;

    return vms.equals(root.vms);
  }

  @Override
  public int hashCode() {
    return vms.hashCode() + 31 * "root".hashCode();
  }
}

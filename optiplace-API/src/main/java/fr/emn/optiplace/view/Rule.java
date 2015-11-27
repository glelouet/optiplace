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
package fr.emn.optiplace.view;

import java.util.Arrays;
import java.util.List;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.solver.ReconfigurationResult;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/** An interface to specify some constraints related to the final state of the
 * vms and pms.
 * @author Fabien Hermenier
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013 */
public interface Rule {

  /** A parser translates a String to a rule, or to null if this string does not
   * match the rule format known by this translater */
  public static interface Parser {
    /** should always return either null if doesn't support the definition fo the
     * rule, or a Rule equal to the one which provided thisStrign using
     * Rule.toString()
     * @param def a textual definition of a String
     * @return null if not supported ; a Rule corresponding to the textual
     * description eitherway. */
    Rule parse(String def);
  }

  /** A Parser which as a list of parsers to call.
   * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
  public static class ChainedParser implements Parser {

		public List<Parser> parsers;

    @Override
    public Rule parse(String def) {
      for (Parser p : parsers) {
        Rule ret = p.parse(def);
        if (ret != null) {
          return ret;
        }
      }
      return null;
    }

		public ChainedParser(Parser... parsers) {
			this.parsers = Arrays.asList(parsers);
		}

  }

  /**
   * Textual representation of the rule.
   *
   * @return a String
   */
  @Override
  String toString();

  /**
   * Check that the rule is satisfied in a configuration.
   *
   * @param cfg
   *            the configuration to check
   * @return true if the constraint is satisfied
   */
  boolean isSatisfied(IConfiguration cfg);

  public void inject(IReconfigurationProblem core);

  /**
   * should call {@link #isSatisfied(plan.getDestination())}
   */
  default boolean isSatisfied(ReconfigurationResult plan) {
    return isSatisfied(plan.getDestination());
  }
}

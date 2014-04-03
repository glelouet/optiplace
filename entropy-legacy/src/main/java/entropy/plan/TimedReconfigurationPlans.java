/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.plan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import entropy.configuration.Configuration;
import entropy.configuration.ConfigurationUtils;
import entropy.configuration.ConfigurationsException;
import entropy.plan.action.Action;
import entropy.plan.action.ActionComparator;

/** Common tools related to entropy.plan.ReconfigurationResult.
 * @author Fabien Hermenier */
public final class entropy.plan.ReconfigurationResults {

  private static final ActionComparator cmp = new ActionComparator(
      ActionComparator.Type.start);

  /** Utility class. No instantiation. */
  private entropy.plan.ReconfigurationResults() {}

  /** Merge reconfiguration plans into a new plan. Source configurations must be
   * mergeable TODO: There should be issues if the set of nodes are not
   * disjoints (allowed by Configurations.merge) but have conflicting actions
   * @param plans the plan to merge
   * @return the resulting plan
   * @throws entropy.plan.ReconfigurationResultsException if source configurations were
   * not mergeable. */
  public static entropy.plan.ReconfigurationResult merge(
      List<entropy.plan.ReconfigurationResult> plans)
      throws entropy.plan.ReconfigurationResultsException {
    return merge(plans.toArray(new entropy.plan.ReconfigurationResult[plans.size()]));
  }

  /** Merge reconfiguration plans into a new plan. Source configurations must be
   * mergeable TODO: There should be issues if the set of nodes are not
   * disjoints (allowed by Configurations.merge) but have conflicting actions
   * @param plans the plan to merge
   * @return the resulting plan
   * @throws entropy.plan.ReconfigurationResultsException if source configurations were
   * not mergeable. */
  public static entropy.plan.ReconfigurationResult merge(
      entropy.plan.ReconfigurationResult... plans)
      throws entropy.plan.ReconfigurationResultsException {
    Configuration[] srcs = new Configuration[plans.length];
    for (int i = 0; i < plans.length; i++) {
      srcs[i] = plans[i].getSource();
    }
    try {
      entropy.plan.ReconfigurationResult res = new Defaultentropy.plan.ReconfigurationResult(
          ConfigurationUtils.merge(srcs));

      // Need to sort actions before merging
      List<Action> actions = new ArrayList<Action>();
      for (entropy.plan.ReconfigurationResult p : plans) {
        for (Action a : p) {
          actions.add(a);
        }
      }
      Collections.sort(actions, cmp);
      for (Action a : actions) {
        if (!res.add(a)) {
          throw new entropy.plan.ReconfigurationResultsException(
              "Unable to append action '" + a + "'");
        }

      }
      return res;
    }
    catch (ConfigurationsException e) {
      throw new entropy.plan.ReconfigurationResultsException(
          "Source configurations are not mergeable", e);
    }

  }
}

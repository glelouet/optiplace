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

import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.plan.parser.PlainTextTimedReconfigurationPlanSerializer;

/** Unit tests for entropy.plan.ReconfigurationResults
 * @author Fabien Hermenier */
@Test(groups = { "unit" })
public class Testentropy.plan.ReconfigurationResults {

  private static final String RESOURCE_ROOT = "src/test/resources/entropy/plan/Testentropy.plan.ReconfigurationResults.";

  /** A cheap test. */
  public void testCheap() {
    try {
      entropy.plan.ReconfigurationResult p1 = PlainTextTimedReconfigurationPlanSerializer
          .getInstance().read(RESOURCE_ROOT + "testMerge1.txt");
      entropy.plan.ReconfigurationResult p2 = PlainTextTimedReconfigurationPlanSerializer
          .getInstance().read(RESOURCE_ROOT + "testMerge2.txt");
      entropy.plan.ReconfigurationResult expected = PlainTextTimedReconfigurationPlanSerializer
          .getInstance().read(RESOURCE_ROOT + "testMerge-expected.txt");
      entropy.plan.ReconfigurationResult res = entropy.plan.ReconfigurationResults.merge(p1, p2);
      Assert.assertEquals(res, expected);
      List<entropy.plan.ReconfigurationResult> plans = new LinkedList<entropy.plan.ReconfigurationResult>();
      plans.add(p1);
      plans.add(p2);
      Assert.assertEquals(entropy.plan.ReconfigurationResults.merge(plans), expected);
      System.err.println(res);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage(), e);
    }
  }
}

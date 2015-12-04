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

package entropy.plan.choco.actionModel.slice;

import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.TimedReconfigurationPlanModelHelper;
import entropy.view.scheduling.actionModel.slice.ConsumingSlice;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Unit tests for ConsumingSlice.
 * 
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestConsumingSlice {

	public void testInstantiation() {
		Configuration src = new SimpleConfiguration();
		Node n = new SimpleNode("n1", 1, 1, 1);
		src.addOnline(n);
		ReconfigurationProblem m = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, src);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(m);
		ConsumingSlice slice = new ConsumingSlice(view, "myName", n, 1, 2);
		Assert.assertEquals(slice.start(), view.getStart());
		Assert.assertNotNull(slice.end());
		Assert.assertNotNull(slice.duration());
		Assert.assertEquals(slice.hoster().getDomainSize(), 1);
		Assert.assertEquals(slice.hoster().getLB(), m.node(n));
	}

	public void testFixEnd() {
		Configuration src = new SimpleConfiguration();
		Node n = new SimpleNode("n1", 1, 1, 1);
		src.addOnline(n);
		ReconfigurationProblem m = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, src);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(m);
		ConsumingSlice slice = new ConsumingSlice(view, "myName", n, 1, 2);
		slice.fixEnd(2);
		Assert.assertEquals(slice.end().getDomainSize(), 1);
		Assert.assertEquals(slice.end().getLB(), 2);
		Assert.assertEquals(slice.end().getUB(), 2);
	}
}

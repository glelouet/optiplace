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

import common.util.iterators.DisposableIntIterator;
import org.chocosolver.solver.variables.IntVar;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.actionModel.TimedReconfigurationPlanModelHelper;
import entropy.view.scheduling.actionModel.slice.DemandingSlice;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Unit tests for DemandingSlice.
 * 
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestDemandingSlice {

	public void testInstantiation() {
		Configuration src = new SimpleConfiguration();
		Node n = new SimpleNode("n1", 1, 1, 1);
		src.addOnline(n);
		ReconfigurationProblem m = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, src);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(m);
		DemandingSlice slice = new DemandingSlice(view, "myName", 1, 2);
		Assert.assertEquals(slice.end(), view.getEnd());
		Assert.assertNotNull(slice.start());
		Assert.assertNotNull(slice.duration());
		Assert.assertNotNull(slice.hoster());
	}

	public void testFixStart() {
		Configuration src = new SimpleConfiguration();
		Node n = new SimpleNode("n1", 1, 1, 1);
		src.addOnline(n);
		ReconfigurationProblem m = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, src);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(m);
		DemandingSlice slice = new DemandingSlice(view, "myName", 1, 2);
		slice.fixStart(0);
		Assert.assertTrue(slice.start().isInstantiated());
		Assert.assertEquals(slice.start().getVal(), 0);
	}

	public void testFixHoster() {
		Configuration src = new SimpleConfiguration();
		Node n = new SimpleNode("n1", 1, 1, 1);
		src.addOnline(n);
		ReconfigurationProblem m = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, src);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(m);
		DemandingSlice slice = new DemandingSlice(view, "myName", 1, 2);
		slice.fixHoster(0);
		Assert.assertTrue(slice.hoster().isInstantiated());
		Assert.assertEquals(slice.hoster().getVal(), 0);
	}

	public void testHosterBounds() {
		Configuration src = new SimpleConfiguration();
		for (int i = 0; i < 21; i++) {
			if (i % 3 == 0) {
				src.addOnline(new SimpleNode("N" + i, 1, 1, 1));
			} else {
				src.addOffline(new SimpleNode("N" + i, 1, 1, 1));
			}
		}
		ReconfigurationProblem m = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, src);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(m);
		DemandingSlice slice = new DemandingSlice(view, "myName", 1, 2);
		IntVar v = slice.hoster();
		DisposableIntIterator ite = v.getDomain().getIterator();
		while (ite.hasNext()) {
			int idx = ite.next();
			Assert.assertNotNull(m.getNode(idx));
		}
		ite.dispose();

	}
}

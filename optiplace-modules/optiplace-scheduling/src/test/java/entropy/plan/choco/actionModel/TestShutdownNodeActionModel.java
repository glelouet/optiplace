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

package entropy.plan.choco.actionModel;

import org.testng.Assert;

import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.action.Shutdown;
import entropy.view.scheduling.actionModel.ShutdownNodeActionModel;
import entropy.view.scheduling.actionModel.TimedReconfigurationPlanModelHelper;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Unit tests for ShutdownActionModel.
 * 
 * @author Fabien Hermenier
 */
// TODO make REAL unit tests
public class TestShutdownNodeActionModel {

	public void testActionDetectionAndCreation() {
		Configuration src = new SimpleConfiguration();
		Configuration dst = new SimpleConfiguration();
		Node n = new SimpleNode("N1", 1, 1, 1);
		src.addOnline(n);
		dst.addOffline(n);
		ReconfigurationProblem m = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, dst);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(m);
		ShutdownNodeActionModel a = (ShutdownNodeActionModel) view
				.getAssociatedAction(n);
		Assert.assertEquals(a.getNode(), n);
		Assert.assertEquals(a.getDuration().getVal(), 8);
		Assert.assertNotNull(a.getDemandingSlice());
		Assert.assertEquals(a.getDemandingSlice().getCPUheight(), a.getNode()
				.getCoreCapacity() * a.getNode().getNbOfCores());
		Assert.assertEquals(a.getDemandingSlice().getMemoryheight(), a
				.getNode().getMemoryCapacity());
		Assert.assertTrue(m.solve());
		Shutdown st = (Shutdown) a.getDefinedAction(m).get(0);
		Assert.assertEquals(st.getNode(), n);
		Assert.assertEquals(st.getStartMoment(), 0);
		Assert.assertEquals(st.getFinishMoment(), 8);
		Assert.assertEquals(a.getDuration().getVal(), 8);
	}
}

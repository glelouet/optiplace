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
import org.testng.annotations.Test;

import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.action.Startup;
import entropy.view.scheduling.actionModel.BootNodeActionModel;
import entropy.view.scheduling.actionModel.TimedReconfigurationPlanModelHelper;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Unit tests for BootNodeActionModel.
 * 
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestBootNodeActionModel {

	public void testActionDetectionAndCreation() {
		// ChocoLogging.setVerbosity(Verbosity.SEARCH);
		Configuration src = new SimpleConfiguration();
		Configuration dst = new SimpleConfiguration();
		Node n = new SimpleNode("N1", 1, 1, 1);
		src.addOffline(n);
		dst.addOnline(n);
		ReconfigurationProblem m = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, dst);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(m);
		BootNodeActionModel a = (BootNodeActionModel) view
				.getAssociatedAction(n);
		Assert.assertEquals(a.getNode(), n);
		Assert.assertNotNull(a.getConsumingSlice());
		Assert.assertEquals(a.getConsumingSlice().getCPUheight(), a.getNode()
				.getCoreCapacity() * a.getNode().getNbOfCores());
		Assert.assertEquals(a.getConsumingSlice().getMemoryheight(), a
				.getNode().getMemoryCapacity());
		Assert.assertEquals(a.getDuration().getVal(), 7);
		Assert.assertTrue(m.solve());
		Startup st = (Startup) a.getDefinedAction(m).get(0);
		Assert.assertEquals(st.getNode(), n);
		Assert.assertEquals(st.getStartMoment(), 0);
		Assert.assertEquals(st.getFinishMoment(), 7);
		Assert.assertEquals(a.getDuration().getVal(), 7);
	}
}

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

import org.chocosolver.solver.exception.ContradictionException;
import entropy.view.scheduling.SchedulingView;
import entropy.view.scheduling.action.Instantiate;
import entropy.view.scheduling.action.Run;
import entropy.view.scheduling.actionModel.RunActionModel;
import entropy.view.scheduling.actionModel.TimedReconfigurationPlanModelHelper;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.SimpleConfiguration;
import fr.emn.optiplace.configuration.SimpleNode;
import fr.emn.optiplace.configuration.SimpleVirtualMachine;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * Unit tests for RunActionModel.
 * 
 * @author Fabien Hermenier
 */
@Test(groups = {"unit", "RP-core"})
public class TestRunActionModel {

	/** Test the creation of a plan composed by a run action. */
	public void testRunActionCreationAndSolve() {
		Configuration src = new SimpleConfiguration();
		Node n = new SimpleNode("n", 1, 1, 1);
		VirtualMachine vm = new SimpleVirtualMachine("VM1", 1, 1, 1);
		src.addWaiting(vm);
		src.addOnline(n);
		Configuration dst = src.clone();
		dst.setRunOn(vm, n);
		ReconfigurationProblem model = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, dst);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(model);
		RunActionModel r = (RunActionModel) view.getAssociatedAction(vm);
		Assert.assertEquals(r.getDuration().getVal(), 3);
		Assert.assertNotNull(r.getDemandingSlice());
		Assert.assertEquals(r.getDemandingSlice().getCPUheight(), r
				.getVirtualMachine().getCPUDemand());
		Assert.assertEquals(r.getDemandingSlice().getMemoryheight(), r
				.getVirtualMachine().getMemoryDemand());
		Assert.assertEquals(r.getVirtualMachine(), vm);
		Assert.assertNotNull(r.end());

		Assert.assertTrue(model.solve(false));
		Run s = (Run) r.getDefinedAction(model).get(0);
		Assert.assertEquals(s.getVirtualMachine(), vm);
		Assert.assertEquals(s.getHost(), n);
		Assert.assertEquals(s.getStartMoment(), 0);
		Assert.assertEquals(s.getFinishMoment(), 3);
		Assert.assertEquals(r.getDuration().getVal(), 3);
	}

	/**
	 * Test with a slice bigger than the duration of the action. The action thus
	 * ends before the end of the slice.
	 */
	public void testWithBiggerSlice() {
		Configuration src = new SimpleConfiguration();
		Node n = new SimpleNode("N1", 1, 1, 1);
		VirtualMachine vm = new SimpleVirtualMachine("VM1", 1, 1, 1);
		src.addWaiting(vm);
		src.addOnline(n);
		Configuration dst = src.clone();
		dst.setRunOn(vm, n);
		ReconfigurationProblem model = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, dst);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(model);
		RunActionModel r = (RunActionModel) view.getAssociatedAction(vm);

		// Here, we increase the duration of the slice
		try {
			r.getDemandingSlice().duration().setInf(20);
		} catch (ContradictionException e) {
			Assert.fail(e.getMessage());
		}
		Assert.assertNotNull(r.getDemandingSlice());
		Assert.assertEquals(r.getVirtualMachine(), vm);
		Assert.assertNotNull(r.end());

		Assert.assertTrue(model.solve(false));
		Run s = (Run) r.getDefinedAction(model).get(0);
		Assert.assertEquals(s.getVirtualMachine(), vm);
		Assert.assertEquals(s.getHost(), n);
		Assert.assertEquals(s.getStartMoment(), 0);
		Assert.assertEquals(s.getFinishMoment(), 3);
	}

	/**
	 * Test the solving process with a VM that was unknown then running, so it
	 * has to result an instantiation and a run.
	 */
	public void testWithAutomaticInstantiation() {
		Configuration src = new SimpleConfiguration();
		Node n = new SimpleNode("N1", 1, 1, 1);
		VirtualMachine vm = new SimpleVirtualMachine("VM1", 1, 1, 1);
		src.addOnline(n);
		Configuration dst = src.clone();
		dst.setRunOn(vm, n);
		ReconfigurationProblem model = TimedReconfigurationPlanModelHelper
				.makeBasicModel(src, dst);
		SchedulingView view = TimedReconfigurationPlanModelHelper
				.makeBasicPlanningView(model);
		RunActionModel r = (RunActionModel) view.getAssociatedAction(vm);

		// Here, we increase the duration of the slice
		Assert.assertTrue(model.solve(false));
		Configuration p = model.extractConfiguration();
		System.out.println(p);
		Instantiate inst = (Instantiate) r.getDefinedAction(model).get(0);
		Run run = (Run) r.getDefinedAction(model).get(1);
		Assert.assertTrue(run.getStartMoment() >= inst.getFinishMoment());
		/*
		 * Assert.assertEquals(s.getVirtualMachine(), vm);
		 * Assert.assertEquals(s.getHost(), n);
		 * Assert.assertEquals(s.getStartMoment(), 0);
		 * Assert.assertEquals(s.getFinishMoment(), 3);
		 */
	}
}

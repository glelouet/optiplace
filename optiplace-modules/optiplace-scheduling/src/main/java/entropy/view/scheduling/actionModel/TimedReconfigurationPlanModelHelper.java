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
package entropy.view.scheduling.actionModel;

import entropy.view.scheduling.DurationEvaluator;
import entropy.view.scheduling.MockDurationEvaluator;
import entropy.view.scheduling.SchedulingView;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.ManagedElementSet;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.configuration.resources.CPUConsSpecification;
import fr.emn.optiplace.configuration.resources.MemConsSpecification;
import fr.emn.optiplace.configuration.resources.ResourceHandler;
import fr.emn.optiplace.solver.PlanException;
import fr.emn.optiplace.solver.choco.DefaultReconfigurationProblem;
import fr.emn.optiplace.solver.choco.ReconfigurationProblem;

/**
 * A helper class to create simple Basicentropy.plan.ReconfigurationResultModel.
 * 
 * @author Fabien Hermenier
 */
public class TimedReconfigurationPlanModelHelper {

	public static MockDurationEvaluator makeBasicEvaluator() {
		return new MockDurationEvaluator(9, 1, 2, 3, 4, 5, 6, 7, 8);
	}

	public static SchedulingView makeBasicPlanningView() {
		return new SchedulingView(makeBasicEvaluator());
	}

	public static SchedulingView makeBasicPlanningView(ReconfigurationProblem pb) {
		SchedulingView ret = makeBasicPlanningView();
		ret.associate(pb);
		return ret;
	}

	public static SchedulingView makeBasicPlanningView(
			ReconfigurationProblem pb, DurationEvaluator ev) {
		SchedulingView ret = new SchedulingView(ev);
		ret.associate(pb);
		return ret;
	}

	/**
	 * Make a model that aims to pass from a source to a destination
	 * configuration.
	 * 
	 * @param src
	 *            The source configuration
	 * @param dst
	 *            The destination configuration
	 * @return the model
	 */
	public static ReconfigurationProblem makeBasicModel(Configuration src,
			Configuration dst) {
		ManagedElementSet<VirtualMachine> toTerminate = src
				.getAllVirtualMachines().clone();
		toTerminate.removeAll(dst.getAllVirtualMachines());
		ReconfigurationProblem pb;
		try {
			pb = new DefaultReconfigurationProblem(src, dst.getRunnings(),
					dst.getWaitings(), dst.getSleepings(), toTerminate,
					src.getAllVirtualMachines());
		} catch (PlanException e) {
			throw new UnsupportedOperationException(e);
		}
		pb.addResourceHandler(new ResourceHandler(CPUConsSpecification.INSTANCE));
		pb.addResourceHandler(new ResourceHandler(MemConsSpecification.INSTANCE));
		return pb;
	}
}

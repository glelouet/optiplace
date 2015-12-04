/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.view.scheduling;

import entropy.view.scheduling.action.Deploy;
import entropy.view.scheduling.action.Instantiate;
import entropy.view.scheduling.action.Migration;
import entropy.view.scheduling.action.Pause;
import entropy.view.scheduling.action.Resume;
import entropy.view.scheduling.action.Run;
import entropy.view.scheduling.action.Startup;
import entropy.view.scheduling.action.Stop;
import entropy.view.scheduling.action.Suspend;
import entropy.view.scheduling.action.UnPause;

/**
 * Interface to specify a visualisation method for a plan.
 * 
 * @author Fabien Hermenier
 */
public interface PlanVisualizer {

	/**
	 * Build the visualization for a given plan.
	 * 
	 * @param plan
	 *            the plan to visualize
	 * @return {@code true} if the creation succeeded
	 */
	boolean buildVisualization(fr.emn.optiplace.solver.ReconfigurationResult plan);

	/**
	 * Inject a migration into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Migration a);

	/**
	 * Inject a run action into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Run a);

	/**
	 * Inject a stop action into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Stop a);

	/**
	 * Inject a startup action into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Startup a);

	/**
	 * Inject a shutdown action into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */

	void inject(entropy.view.scheduling.action.Shutdown a);

	/**
	 * Inject a resume action into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Resume a);

	/**
	 * Inject a suspend action into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Suspend a);

	/**
	 * Inject a pause action into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Pause a);

	/**
	 * Inject a unpause into the visualization.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(UnPause a);

	/**
	 * Inject an instantiate action.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Instantiate a);

	/**
	 * Inject a deploy action.
	 * 
	 * @param a
	 *            the action to add
	 */
	void inject(Deploy a);

}

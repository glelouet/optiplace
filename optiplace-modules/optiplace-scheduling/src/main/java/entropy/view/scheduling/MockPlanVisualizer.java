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

import java.util.LinkedList;
import java.util.List;

import entropy.view.scheduling.action.*;

/** @author Fabien Hermenier */
public class MockPlanVisualizer implements PlanVisualizer {

	private List<Action> injected;

	public MockPlanVisualizer() {
		injected = new LinkedList<Action>();
	}

	@Override
	public boolean buildVisualization(fr.emn.optiplace.solver.ReconfigurationResult plan) {
		return false;
	}

	public boolean isInjected(Action a) {
		return injected.contains(a);
	}

	@Override
	public void inject(Migration a) {
		injected.add(a);
	}

	@Override
	public void inject(Run a) {
		injected.add(a);
	}

	@Override
	public void inject(Stop a) {
		injected.add(a);
	}

	@Override
	public void inject(Startup a) {
		injected.add(a);
	}

	@Override
	public void inject(Shutdown a) {
		injected.add(a);
	}

	@Override
	public void inject(Resume a) {
		injected.add(a);
	}

	@Override
	public void inject(Suspend a) {
		injected.add(a);
	}

	@Override
	public void inject(Pause a) {
		injected.add(a);
	}

	@Override
	public void inject(UnPause a) {
		injected.add(a);
	}

	@Override
	public void inject(Instantiate a) {
		injected.add(a);
	}

	@Override
	public void inject(Deploy a) {
		injected.add(a);
	}
}

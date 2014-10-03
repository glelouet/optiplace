/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.solver;

/**
 * Statistics about the solving process of a RP.
 *
 * @author Fabien Hermenier
 */
public class SolvingStatistics implements Cloneable {

	private static SolvingStatistics noStats = new SolvingStatistics(0, 0, 0,
			false);

	/** The number of opened nodes. */
	private final long nbNodes;

	/** The number of backtracks. */
	private final long nbBacktracks;

	/** The moment the solution was computed. */
	private final long timeCount;

	/** Indicates whether or not the solver has hit the timeout. */
	private final boolean timeout;

	/**
	 * Make new statistics
	 *
	 * @param l
	 * the number of opened nodes
	 * @param m
	 * the number of backtracks
	 * @param f
	 * the timeCount of the solution
	 */
	public SolvingStatistics(long l, long m, long f,
			boolean timeout) {
		this.nbNodes = l;
		this.nbBacktracks = m;
		this.timeCount = f;
		this.timeout = timeout;
	}

	/** @return the number of opened nodes. */
	public long getNbNodes() {
		return nbNodes;
	}

	/** @return the number of backtracks. */
	public long getNbBacktracks() {
		return nbBacktracks;
	}

	/** @return the moment the solution was computed. */
	public long getTimeCount() {
		return timeCount;
	}

	public boolean hasReachedTimeout() {
		return timeout;
	}

	@Override
	public String toString() {
		return new StringBuilder("nodes=").append(nbNodes)
				.append(", backtracks=").append(nbBacktracks).append(", time=")
				.append(timeCount).append(", timeout=").append(timeout)
				.toString();
	}

	/**
	 * Export data to a String that sump up the solution. Parseable data Fields
	 * are separated by space. First is the objective, second the number of
	 * opened nodes, third is the number of backtracks. Last is the time then
	 * the timeout
	 *
	 * @return a String
	 */
	public String toRawData() {
		return new StringBuilder().append(nbNodes).append(' ')
				.append(nbBacktracks).append(' ').append(timeCount).append(' ')
				.append(timeout ? 1 : 0).toString();
	}

	public static String noResults() {
		return "- - - -";
	}

	public static SolvingStatistics getStatisticsForNotSolvingProcess() {
		return noStats;
	}

	@Override
	public SolvingStatistics clone() {
		return new SolvingStatistics(nbNodes, nbBacktracks, timeCount, timeout);
	}
}

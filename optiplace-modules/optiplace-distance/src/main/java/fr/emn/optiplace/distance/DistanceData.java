package fr.emn.optiplace.distance;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 *
 * @author Guillaume Le Louët
 *
 */
public class DistanceData {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DistanceData.class);

	/**
	 * diagonal matrix.
	 *
	 */
	protected Table<String, String, Integer> distances = HashBasedTable.create();

	public void setDist(String name1, String name2, int distance) {
		if (name1.compareTo(name2) < 0) {
			setDist(name2, name1, distance);
		} else {
			distances.put(name1, name2, distance);
		}
	}

	public int getDist(String name1, String name2) {
		if (name1.compareTo(name2) < 0) {
			return getDist(name2, name1);
		} else {
			return distances.get(name1, name2);
		}
	}

	public Stream<String> getNeighbours(String node) {
		return Stream.concat(distances.column(node).keySet().stream(), distances.row(node).keySet().stream());
	}

	/**
	 * check if all nodes can be reached from the first node, ie explore
	 * width-first
	 */
	public boolean isComplete(String[] graphNames) {
		if (graphNames == null || graphNames.length == 0) {
			return true;
		}
		Set<String> remaining = new HashSet<>(Arrays.asList(graphNames));
		Set<String> nextExplore = Collections.singleton(graphNames[0]);
		remaining.removeAll(nextExplore);
		while (!nextExplore.isEmpty()) {
			nextExplore = nextExplore.stream().flatMap(n -> getNeighbours(n)).filter(remaining::contains)
					.collect(Collectors.toSet());
			remaining.removeAll(nextExplore);
		}
		return remaining.isEmpty();
	}

}
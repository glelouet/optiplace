package fr.emn.optiplace.distance;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;

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
		if (distance < 1) {
			delDist(name1, name2);
			return;
		}
		if (name1.compareTo(name2) < 0) {
			setDist(name2, name1, distance);
		} else {
			distances.put(name1, name2, distance);
		}
	}

	/**
	 * get the direct distance between two names.
	 *
	 * @param name1
	 *          first name
	 * @param name2
	 *          second name
	 * @return the stored distance between two names, or -1 if one is null or no
	 *         distance stored.
	 */
	public int getDist(String name1, String name2) {
		if (name1 == null || name2 == null) {
			return -1;
		}
		if (name1.compareTo(name2) < 0) {
			return getDist(name2, name1);
		} else {
			Integer value = distances.get(name1, name2);
			return value != null ? value : -1;
		}
	}

	public void delDist(String name1, String name2) {
		distances.remove(name1, name2);
		distances.remove(name2, name1);
	}

	public Stream<String> getNeighbours(String node) {
		return Stream.concat(distances.column(node).keySet().stream(), distances.row(node).keySet().stream());
	}

	/**
	 * check if all nodes can be reached from the first node, ie explore
	 * width-first
	 */
	public boolean isComplete(String... graphNames) {
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

	/**
	 * @param names
	 *          the names to consider
	 * @return a nex int[names.length][names.length], symetric, containg all the
	 *         distances. IF the distances is not complete wrt names, distance is
	 *         set to -1.
	 */
	public int[][] makeDistancesTable(String... names) {
		if (names == null || names.length == 0) {
			return new int[0][0];
		}
		int[][] ret = new int[names.length][names.length];
		for (int i = 0; i < names.length; i++) {
			for (int j = 0; j < i; j++) {
				int dist = getDist(names[i], names[j]);
				ret[i][j] = ret[j][i] = dist;
			}
		}
		return ret;
	}

	protected HashMap<Set<VM>, Integer> limits = new HashMap<>();

	protected HashMap<VM, Set<VM>> groups = new HashMap<>();

	public void setLimit(int limit, VM... vms) {
		if (vms == null || vms.length == 0) {
			return;
		}
		if (limit < 1) {
			return;
		}
		HashSet<VM> group = new HashSet<>(Arrays.asList(vms));
		limits.put(group, limit);
		for (VM v : group) {
			groups.put(v, group);
		}
	}

	public Set<VM> getGroup(String name) {
		return groups.get(name);
	}

	public int getLimit(Set<String> group) {
		return limits.get(group);
	}

	protected HashMap<Pattern, Integer> patLimits = new HashMap<>();

	public void setPatLimit(Pattern pattern, int value) {
		patLimits.put(pattern, value);
	}

	/**
	 * convert a {@link Configuration} to the corresponding stream of grouped VM.
	 *
	 * @param source
	 *          the
	 * @return
	 */
	public void applyGroups(BiConsumer<Stream<VM>, Integer> apply, IConfiguration source) {
		patLimits.entrySet().stream().forEach(e -> {
			apply.accept(source.getVMs().filter(v -> !groups.containsKey(v.getName()))
					.filter(v -> e.getKey().matcher(v.getName()).matches()), e.getValue());
		});
		limits.entrySet().stream().forEach(e -> {
			apply.accept(e.getKey().stream(), e.getValue());
		});
	}

}
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
import fr.emn.optiplace.view.ProvidedDataReader;

/**
 *
 * @author Guillaume Le Louët
 *
 */
public class DistanceData implements ProvidedDataReader {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DistanceData.class);

	/**
	 * diagonal matrix.
	 *
	 */
	protected Table<String, String, Integer> distances = HashBasedTable.create();

	public void setDist(String name1, String name2, int distance) {
		if (distance < 1) {
			distances.remove(name1, name2);
			distances.remove(name2, name1);
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
	 * translate the internal data to a matrix of integer. The matrix is optimized, meaning
	 * {@link #reduceDistances(int[][])} is called on it.
	 *
	 * @param names
	 *          the names to consider
	 * @return a new int[names.length][names.length], symetric, containg all the distances. IF the distances between two
	 *         elements can not be deduced, corresponding cell is set to -1.
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

	/**
	 * reduce the distances based on dist(a-c) &le; dist(a-b)+dist(b-c). For example, if know the distance between a and b
	 * is 1, between b and c is 1, then the distance a-c can be set to 2 if it is higher.
	 *
	 * @param distances
	 *          a symetric table of distances.
	 */
	public static void reduceDistances(int[][] distances) {
		if (distances == null || distances.length < 2 || distances[0].length != distances.length) {
			return;
		}
		boolean modification = false;
		do {
			modification = false;
			for (int i = 0; i < distances.length; i++) {
				for (int k = 0; k < i; k++) {
					int dist = distances[i][k];
					for (int j = 0; j < distances.length; j++) {
						int d1 = distances[i][j], d2 = distances[j][k];
						if (d1 != -1 && d2 != -1) {
							int newdist = d1 + d2;
							if (dist == -1 || newdist < dist) {
								distances[i][k] = distances[k][i] = dist = newdist;
								modification = true;
							}
						}
					}
				}
			}
		} while (modification);

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

	public Set<VM> getGroup(VM vm) {
		return groups.get(vm);
	}

	public int getLimit(Set<VM> group) {
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
			apply.accept(
					source.getVMs().filter(v -> !groups.containsKey(v))
					.filter(v -> e.getKey().matcher(v.getName()).matches()), e.getValue());
		});
		limits.entrySet().stream().forEach(e -> {
			apply.accept(e.getKey().stream(), e.getValue());
		});
	}

	@Override
	public void onNewConfig() {
		ProvidedDataReader.super.onNewConfig();
		groups.clear();
		limits.clear();
		patLimits.clear();
		distances.clear();
	}

	@Override
	public void readLine(String line) {
		if (line.startsWith("distance ")) {
			String[] data = line.substring("distance ".length()).split(" ");
			setDist(data[1], data[2], Integer.parseInt(data[0]));
		} else if (line.startsWith("limit ")) {
			String[] data = line.substring("limit ".length()).replaceAll("\\[|,|\\]", "").split(" ");
			VM[] vms = new VM[data.length - 1];
			for (int i = 0; i < vms.length; i++) {
				vms[i] = new VM(data[i + 1]);
			}
			setLimit(Integer.parseInt(data[0]), vms);
		} else if (line.startsWith("limPat")) {
			String[] data = line.substring("limPat ".length()).split(" ");
			setPatLimit(Pattern.compile(data[1]), Integer.parseInt(data[0]));
		}
	}

	public Stream<String> exportTxt() {
		return Stream.concat(
				distances.columnMap().entrySet().stream()
				.flatMap(e -> e.getValue().entrySet().stream()
						.map(e2 -> "distance " + e2.getValue() + " " + e.getKey() + " " + e2.getKey())),
				Stream.concat(limits.entrySet().stream().map(e -> "limit " + e.getValue() + " " + e.getKey()),
						patLimits.entrySet().stream().map(e -> "limPat " + e.getValue() + " " + e.getKey().pattern())));
	}

	@Override
	public String toString() {
		return exportTxt().collect(Collectors.joining("\n"));
	}

}
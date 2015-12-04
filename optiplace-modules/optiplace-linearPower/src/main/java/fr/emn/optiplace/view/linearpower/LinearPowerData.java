package fr.emn.optiplace.view.linearpower;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.view.linearpower.migrationcost.MigrationCostNull;

/** @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014 */
public class LinearPowerData {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(LinearPowerData.class);

	/** the time slots we consider */
	public int timeSlotSeconds = 1;

	public LinearPowerData withTimeSlot(String time) {
		if (time != null) {
			timeSlotSeconds = convertStringToSeconds(time);
		}
		return this;
	}

	public static Pattern timeMatcher1 = Pattern
			.compile("(\\d+)(?:h|H|\\:)(?:(\\d+)(?:(?:m|\\:)(\\d+)?)?)?");

	public static Pattern timeMatcher2 = Pattern
			.compile("(\\d+)(?:m|M|min)(?:(\\d+)s?)?");

	/**
	 * @param time
	 * @return
	 */
	public static int convertStringToSeconds(String time) {
		if (time == null) {
			return 0;
		}
		Matcher m1 = timeMatcher1.matcher(time);
		int h = 0, m = 0, s = 0;
		if (m1.matches()) {
			h = Integer.parseInt(m1.group(1));
			if (m1.group(2) != null) {
				m = Integer.parseInt(m1.group(2));
				if (m1.group(3) != null) {
					s = Integer.parseInt(m1.group(3));
				}
			}
		} else {
			Matcher m2 = timeMatcher2.matcher(time);
			if (m2.matches()) {
				m = Integer.parseInt(m2.group(1));
				if (m2.group(2) != null) {
					s = Integer.parseInt(m2.group(2));
				}
			} else {
				if (time.matches("\\d+")) {
					s = Integer.parseInt(time);
				} else {
					logger.warn("time " + time
							+ " does not match any known format (X:Y:Z, Xh, YmZs, Z");
				}
			}
		}
		return h * 3600 + m * 60 + s;
	}

	public HashMap<String, LinearPowerModel> namedModels = new HashMap<String, LinearPowerModel>();

	public void addServerModel(String serverName, LinearPowerModel model) {
		namedModels.put(serverName, model);
	}

	public LinkedHashMap<Pattern, LinearPowerModel> filterModels = new LinkedHashMap<Pattern, LinearPowerModel>();

	public void addPatternModel(String namePattern, LinearPowerModel model) {
		filterModels.put(Pattern.compile(namePattern), model);
	}

	public LinearPowerModel getModel(String nodeName) {
		LinearPowerModel ret = namedModels.get(nodeName);
		if (ret != null) {
			return ret;
		}
		for (Entry<Pattern, LinearPowerModel> e : filterModels.entrySet()) {
			if (e.getKey().matcher(nodeName).matches()) {
				return e.getValue();
			}
		}
		return null;
	}

	public LinearPowerModel getModel(Node n) {
		return getModel(n.getName());
	}

	/**
	 * for each node of the configuration, guess the required energy to power this
	 * node alone for a timeslot duration
	 *
	 * @param cfg
	 * the configuration, used to retrieve the Nodes.
	 * @return a new array containing the cost of running each server for the
	 * given {@link #timeSlotSeconds} duration.
	 */
	public int[] makeNodeRunningCost(Node[] nodes) {
		int[] ret = new int[nodes.length];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = (int) (getModel(nodes[i]).base * timeSlotSeconds);
		}
		return ret;
	}

	protected MigrationCostMaker migrationCost = MigrationCostNull.INSTANCE;

	public void setMigrationCostMaker(MigrationCostMaker maker) {
		migrationCost = maker;
	}

	/**
	 * @param cfg
	 * the configuration of the vms on the nodes
	 * @param nodes
	 * the array of the nodes in the configuration. If set to null will be
	 * computed internally. Use it only if you need it on other operations, to
	 * remove unnecessary computes.
	 * @param vms
	 * the array of the vms in the configuration, computed if null.
	 * @param resources
	 * the specification of resources.
	 * @return cost[i][j] the cost of hosting the vm i on the node j for a time
	 * slot.
	 */
	public int[][] makeHostingCost(IConfiguration cfg, Node[] nodes,
 VM[] vms) {
		if (nodes == null) {
			nodes = cfg.getNodes().collect(Collectors.toList())
					.toArray(new Node[] {});
		}
		if (vms == null) {
			vms = cfg.getVMs().collect(Collectors.toList())
					.toArray(new VM[] {});
		}
		int[][] ret = new int[cfg.nbVMs()][nodes.length];
		// first for all vm, add the migration cost to each node different from
		// its hoster
		for (int i = 0; i < ret.length; i++) {
			VM vm = vms[i];
			int cost = migrationCost.migrationCost(vm, cfg.resources());
			int hosterIdx = Arrays.binarySearch(nodes, cfg.getLocation(vm));
			for (int j = 0; j < ret[i].length; j++) {
				if (j != hosterIdx) {
					ret[i][j] = cost;
				} else {
					ret[i][j] = 0;
				}
			}
		}

		// then for all node, for each vm add the cost of running this vm on this
		// node
		for (int j = 0; j < ret[0].length; j++) {
			LinearPowerModel m = getModel(nodes[j]);
			for (int i = 0; i < ret.length; i++) {
				ret[i][j] += m.makePower(vms[i], cfg.resources()) * timeSlotSeconds;
			}
		}

		return ret;
	}

}

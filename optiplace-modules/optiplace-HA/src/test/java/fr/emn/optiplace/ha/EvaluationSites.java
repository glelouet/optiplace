
package fr.emn.optiplace.ha;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.ha.rules.Far;
import fr.emn.optiplace.test.SolvingExample;
import fr.emn.optiplace.view.View;


public class EvaluationSites extends SolvingExample {

	@SuppressWarnings("serial")
	static class Data extends ArrayList<Map<String, String>> {

		LinkedHashMap<String, String> fields = new LinkedHashMap<>();

		LinkedHashMap<String, String> getFields() {
			return fields;
		}

		Map<String, String> currentEntry = new HashMap<>();

		/**
		 * prepare to handle items of a new entry
		 */
		public void newEntry() {
			if (!currentEntry.isEmpty()) {
				add(currentEntry);
				currentEntry = new HashMap<>();
			}
		}

		public void set(String key, String val) {
			if (!fields.containsKey(key)) {
				fields.put(key, key);
			}
			currentEntry.put(key, val);
		}

		@Override
		public String toString() {
			return toString("\t", "\n");
		}

		public String toString(String colSep, String rowSep) {
			StringBuilder sb = new StringBuilder();
			sb.append(fields.values().stream().collect(Collectors.joining(colSep)));
			for (Map<String, String> line : this) {
				sb.append(rowSep).append(fields.keySet().stream().map(line::get).collect(Collectors.joining(colSep)));
			}
			return sb.toString();
		}

		public String toTable(Function<String, String> tableMap, Function<String, String> headerMap,
		    Function<String, String> cellMap,
		    Function<String, String> rowMap) {
			StringBuilder sb = new StringBuilder();
			sb.append(rowMap.apply(fields.values().stream().map(headerMap).collect(Collectors.joining())));
			for (Map<String, String> line : this) {
				sb.append(rowMap.apply(fields.keySet().stream().map(line::get).map(cellMap).collect(Collectors.joining())));
			}
			return tableMap.apply(sb.toString());
		}

		public String toTable() {
			return toTable(s -> "<table>" + s + "</table>", s -> "<th>" + s + "</th>", s -> "<td>" + s + "</td>",
			    s -> "<tr>" + s + "</tr>\n");
		}
	}

	public static void main(String[] args) {
		Data data = new Data();

		for (int i = 10; i < 80; i *= 1.2) {
			EvaluationSites es = new EvaluationSites(i);
			for (boolean addSiteConstraints : new Boolean[] {
			    true, false }) {
				es.addSiteConstraints = addSiteConstraints;
				long minSearchTime = Long.MAX_VALUE;
				long minNbServers = Integer.MAX_VALUE;
				for (int nb = 0; nb < 5; nb++) {
					DeducedTarget res = es.solve(es.src);
					IConfiguration dest = res.getDestination();
					minSearchTime = Math.min(minSearchTime, res.getSearchTime());
					minNbServers = Math.min(minNbServers,
					    res.getDestination().getNodes().filter(n -> dest.nbHosted(n) > 0).count());
				}
				data.set("nbServers", "" + i * 3);
				data.set("addSiteConstraints", "" + addSiteConstraints);
				data.set("minSearchTime", "" + minSearchTime);
				data.set("minNbServers", "" + minNbServers);
				data.newEntry();
				System.err.println(i);
			}
		}
		System.err.println("data:\n" + data);
		System.err.println("data table:\n" + data.toTable());
	}

	/**
	 * we have one center with two sites. site 1 has two servers per centerSize,
	 * while site2 has one server per centerSize
	 */

	int centerSize;
	HAView ha;
	boolean addSiteConstraints = true;

	@Override
	protected void prepare() {
		resources = new String[] {
		    "CPU", "MEM" };
		nodeCapas = new int[] {
		    100, 100 };
		ha = new HAView();
		views = new View[] {
		    ha };
		super.prepare();
		src.addSite("firstSite", Arrays.copyOf(nodes, centerSize * 2));
		src.addSite("secondSite", Arrays.copyOfRange(nodes, centerSize * 2, nodes.length));
	}

	/**
	 * for each center size we add 6 VMs: a group of 3 VM with high CPU use, 2 VM
	 * subject to gather and a clone of the first VM which has a "far" rule
	 */

	@Override
	protected VM[] makeWaitings() {
		ArrayList<VM> waitings = new ArrayList<>();
		for (int i = 0; i < centerSize; i++) {
			VM cpu_main = src.addVM("vm_" + i + "_cpu_main", null, 35, 15);
			VM cpu_gath = src.addVM("vm_" + i + "_cpu_gath", null, 35, 15);
			VM cpu_clon = src.addVM("vm_" + i + "_cpu_clon", null, 35, 15);
			if (addSiteConstraints) {
			ha.addRule(new Far(cpu_main, cpu_clon));
			}

			VM mem_main = src.addVM("vm_" + i + "_mem_main", null, 15, 35);
			VM mem_gath = src.addVM("vm_" + i + "_mem_gath", null, 15, 35);
			VM mem_clon = src.addVM("vm_" + i + "_mem_clon", null, 15, 35);
			if (addSiteConstraints) {
				ha.addRule(new Far(mem_main, mem_clon));
			}

			waitings.addAll(Arrays.asList(cpu_main, cpu_gath, cpu_clon, mem_main, mem_gath, mem_clon));
		}
		return waitings.toArray(new VM[] {});
	}

	public EvaluationSites(int centerSize) {
		this.centerSize = centerSize;
		nbNodes = centerSize * 3;
		nbVMPerNode = 0;
		prepare();
	}

}

package entropy.view.hotspot;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.google.common.collect.HashBasedTable;

import fr.emn.optiplace.configuration.Node;

/**
 * a model where each Node consumption impact the rear of each other Node, in a
 * linear way. basically a matrix of rear impacts.
 *
 * @author guillaume
 */
public class ImpactMatrix {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(ImpactMatrix.class);

	public ImpactMatrix() {
	}

	protected HashBasedTable<String, String, Double> impacts = HashBasedTable
			.create();

	public void setImpact(String from, String to, double val) {
		if (from == null || to == null) {
			return;
		}
		if (val == 0) {
			impacts.remove(from, to);
		}
		impacts.put(from, to, val);
	}

	public double getImpact(String from, String to) {
		if (from == null || to == null) {
			return 0;
		}
		Double ret = impacts.get(from, to);
		return ret == null ? 0 : ret;
	}

	public void deleteImpact(String from, String to) {
		impacts.remove(from, to);
	}

	public void clear() {
		impacts.clear();
	}

	public Map<String, Double> getImpacters(String impacted) {
		return impacts.column(impacted);
	}

	/** get the map of impacts of a server on other servers */
	public Map<String, Double> getImpacted(String impacter) {
		return impacts.row(impacter);
	}

	/** evaluate the impact on a set of values */
	public HashMap<String, Double> impact(HashMap<Node, Double> vals) {
		HashMap<String, Double> ret = new HashMap<>();
		LinkedHashSet<String> names = new LinkedHashSet<>(vals.keySet().stream()
				.map(Node::getName).collect(Collectors.toList()));
		names.addAll(impacts.columnKeySet());
		names.addAll(impacts.rowKeySet());
		for (String receiver : names) {
			double val = 0;
			Map<String, Double> impacters = getImpacters(receiver);

			for (Entry<Node, Double> en : vals.entrySet()) {
				Double d = impacters.get(en.getKey().getName());
				if (d != null && d != 0) {
					val += d * en.getValue();
				}
			}
			ret.put(receiver, val);
		}
		return ret;
	}

}

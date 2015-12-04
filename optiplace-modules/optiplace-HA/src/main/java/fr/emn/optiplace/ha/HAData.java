package fr.emn.optiplace.ha;

import java.util.ArrayList;
import java.util.List;

import fr.emn.optiplace.ha.rules.Among;
import fr.emn.optiplace.ha.rules.Ban;
import fr.emn.optiplace.ha.rules.Capacity;
import fr.emn.optiplace.ha.rules.Far;
import fr.emn.optiplace.ha.rules.Fence;
import fr.emn.optiplace.ha.rules.Greedy;
import fr.emn.optiplace.ha.rules.Lazy;
import fr.emn.optiplace.ha.rules.LoadInc;
import fr.emn.optiplace.ha.rules.Near;
import fr.emn.optiplace.ha.rules.Quarantine;
import fr.emn.optiplace.ha.rules.Replication;
import fr.emn.optiplace.ha.rules.Root;
import fr.emn.optiplace.ha.rules.SiteOff;
import fr.emn.optiplace.ha.rules.SiteOn;
import fr.emn.optiplace.ha.rules.Split;
import fr.emn.optiplace.ha.rules.Spread;
import fr.emn.optiplace.view.ProvidedDataReader;
import fr.emn.optiplace.view.Rule;
import fr.emn.optiplace.view.Rule.ChainedParser;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
public class HAData implements ProvidedDataReader {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HAData.class);

	public static ChainedParser HAPARSER = new ChainedParser(Among.PARSER, Ban.PARSER, Capacity.PARSER, Far.PARSER,
			Fence.PARSER, Greedy.PARSER, Lazy.PARSER, LoadInc.PARSER, Near.PARSER, Quarantine.PARSER, Replication.PARSER,
			Root.PARSER, SiteOff.PARSER, SiteOn.PARSER, Split.PARSER, Spread.PARSER);

	List<Rule> rules = new ArrayList<>();

	public List<Rule> getRules() {
		return rules;
	}

	@Override
	public void onNewConfig() {
		rules.clear();
	}

	@Override
	public void readLine(String line) {
		Rule r = HAPARSER.parse(line);
		if (r != null) {
			rules.add(r);
		} else {
			logger.error("can not parse rule " + line);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = null;
		for (Rule r : rules) {
			if (sb == null) {
				sb = new StringBuilder();
			} else {
				sb.append("\n");
			}
			sb.append(r.toString());
		}
		return sb != null ? sb.toString() : "EmptyHAData";
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj)) {
			return true;
		}
		if (obj == null || obj.getClass() != HAData.class) {
			return false;
		}
		HAData other = (HAData) obj;
		return rules.equals(other.getRules());
	}
}

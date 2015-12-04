package fr.emn.optiplace.thermal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * informations about the impact from one server's consumption to another
 * server's entering air temperature increase.<br />
 * The default value for the impact of two servers is 0. This allows to have
 * holed map inside.
 * 
 * @author guillaume
 */
public class ImpactMap {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ImpactMap.class);

	/**
	 * @param nbServer
	 *            number of servers present
	 * @param value
	 *            impact of each server on itself.
	 * @return an {@link ImpactMap} where each server impacts only itself by
	 *         given value. servers are named s1…s[nbServer]
	 */
	static ImpactMap makeIdentity(int nbServer, double value) {
		ImpactMap ret = new ImpactMap();
		for (int i = 1; i <= nbServer; i++) {
			String name = "s" + i;
			ret.set(name, name, value);
		}
		return ret;
	}

	/**
	 * @param nbServer
	 *            number of servers present
	 * @param value
	 *            impact of each server on all servers.
	 * @return an {@link ImpactMap} where each server impacts all servers by
	 *         given value. servers are named s1…s[nbServer]
	 */
	static ImpactMap makeConnex(int nbServer, double value) {
		ImpactMap ret = new ImpactMap();
		String[] names = new String[nbServer];
		for (int i = 1; i <= nbServer; i++) {
			names[i - 1] = "s" + i;
		}
		for (int i = 0; i < nbServer; i++) {
			for (int j = 0; j < nbServer; j++) {
				ret.set(names[i], names[j], value);
			}
		}
		return ret;
	}

	private HashMap<String, HashMap<String, Double>> impacts = new HashMap<String, HashMap<String, Double>>();

	/**
	 * get the impact of the consumption from one server to the entering air's
	 * temperature increase in another server.
	 * 
	 * @param server1
	 *            the server that consumes energy
	 * @param server2
	 *            the server that has its incoming air increased
	 * @return the value, as of °C/W, of the impact from server1 to server2.
	 */
	public double get(String server1, String server2) {
		HashMap<String, Double> map = impacts.get(server1);
		if (map == null) {
			return 0.0;
		}
		Object ret = map.get(server2);
		if (ret == null) {
			return 0.0;
		}
		return (Double) ret;
	}

	/**
	 * set the impact from one server to another. if set to 0, remove the data
	 * to maintain the map hollow
	 * 
	 * @param server1
	 *            the server impacting
	 * @param server2
	 *            the server impacted
	 * @param impact
	 *            the increased in the impacted server's inlet temperature, in
	 *            °C, per impacting server increase in consumption, in W
	 */
	public void set(String server1, String server2, double impact) {
		if (impact == 0) {
			remove(server1, server2);
			return;
		}
		HashMap<String, Double> map = impacts.get(server1);
		if (map == null) {
			map = new HashMap<String, Double>();
			impacts.put(server1, map);
		}
		map.put(server2, impact);
	}

	/**
	 * remove the impact data from one server to another. Effectiveley remove
	 * the data, meaning the impact becomes 0.
	 * 
	 * @param server1
	 *            the impacting server
	 * @param server2
	 *            the impacetd server
	 */
	public void remove(String server1, String server2) {
		HashMap<String, Double> map = impacts.get(server1);
		if (map == null) {
			return;
		}
		map.remove(server2);
		if (map.isEmpty()) {
			impacts.remove(map);
		}
	}

	/**
	 * convert that map to a String data, that can be later parsed using
	 * {@link #parse(String)}
	 */
	public void write(PrintWriter ps) {
		for (Entry<String, HashMap<String, Double>> perImpacting : impacts
				.entrySet()) {
			if (perImpacting.getValue().isEmpty()) {
				continue;
			}
			ps.print(escape(perImpacting.getKey()) + ' ');
			for (Entry<String, Double> entry : perImpacting.getValue()
					.entrySet()) {
				ps.print(" " + escape(entry.getKey()) + '=' + entry.getValue());
			}
			ps.print('\n');
		}
	}

	protected static String escape(String from) {
		return from.replaceAll("\\\\", "\\\\").replaceAll(":", "\\:")
				.replaceAll("=", "\\=").replaceAll(" ", "\\ ");
	}

	public void parse(BufferedReader br) {
		String line = null;
		do {
			try {
				line = br.readLine();
				if (line != null) {
					parseLine(line);
				}
			} catch (IOException e) {
				logger.warn("", e);
				return;
			}
		} while (line != null);
	}

	protected void parseLine(String line) {
		String from = null;
		String eaten = "";
		HashMap<String, Double> vals = new HashMap<String, Double>();
		boolean escaping = false;
		for (int i = 0; i <= line.length(); i++) {
			char c = i < line.length() ? line.charAt(i) : ' ';
			if (c == '\\' && !escaping) {
				escaping = true;
			} else {
				if (!escaping && c == ' ') {
					if (from == null) {
						from = eaten;
						impacts.put(from, vals);
					} else if (eaten.length() > 2) {
						int pos = eaten.lastIndexOf('=');
						String name = eaten.substring(0, pos);
						Double val = Double.parseDouble(eaten.substring(
								pos + 1, eaten.length()));
						// logger.debug(from + "->" + name + "=" + val);
						vals.put(name, val);
					}
					eaten = "";
				} else {
					eaten += c;
				}
				escaping = false;
			}
		}
	}

	/** remove all internal data */
	public void clear() {
		impacts.clear();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj.getClass() == ImpactMap.class) {
			ImpactMap other = (ImpactMap) obj;
			if (other.impacts.size() == impacts.size()) {
				for (Entry<String, HashMap<String, Double>> entry : impacts
						.entrySet()) {
					HashMap<String, Double> otherVals = other.impacts.get(entry
							.getKey());
					if (otherVals == null
							|| !otherVals.equals(entry.getValue())) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public ImpactMap clone() {
		ImpactMap ret = new ImpactMap();
		for (Entry<String, HashMap<String, Double>> entry : impacts.entrySet()) {
			ret.impacts.put(entry.getKey(),
					new HashMap<String, Double>(entry.getValue()));
		}
		return ret;
	}

	@Override
	public String toString() {
		return "impact" + impacts.toString();
	}

	/** clone the impactmap, with only the specified servers being impacted. */
	public ImpactMap restrictImpacted(Collection<String> serverNames) {
		ImpactMap ret = clone();
		for (HashMap<String, Double> m : ret.impacts.values()) {
			m.keySet().retainAll(serverNames);
		}
		return ret;
	}

	/**
	 * get the map of the servers impacted by one specific, associated to the
	 * impact of that specific to each server.
	 */
	public Map<String, Double> getImpacted(String server) {
		HashMap<String, Double> ret = impacts.get(server);
		if (ret != null) {
			return Collections.unmodifiableMap(ret);
		}
		return Collections.emptyMap();
	}

	/**
	 * get an array of all contained server names. The names are not ordered.
	 * This an be used after to specify an order on the servers, like in
	 * {@link #toPlainMatrix(String[])}
	 */
	public String[] getAllServerNames() {
		HashSet<String> res = new HashSet<String>();
		res.addAll(impacts.keySet());
		for (HashMap<String, Double> m : impacts.values()) {
			res.addAll(m.keySet());
		}
		return res.toArray(new String[]{});
	}

	/**
	 * @param servernames
	 *            the names of the servers in the index they must appear.
	 * @return an array ret such as ret[i][j]={@link #get(servernames[i],
	 *         servernames[j])}
	 */
	public double[][] toPlainMatrix(String[] servernames) {
		double[][] ret = new double[servernames.length][servernames.length];
		for (int i = 0; i < servernames.length; i++) {
			String from = servernames[i];
			for (int j = 0; j < servernames.length; j++) {
				String to = servernames[j];
				ret[i][j] = get(from, to);
			}
		}
		return ret;
	}

	/**
	 * apply this impact map to get the temperature increases of the servers
	 * 
	 * @param serverConsumptions
	 * @param serverMinTemps
	 * @return
	 */
	public HashMap<String, Double> getTempIncreases(
			HashMap<String, Double> serverConsumptions) {
		HashMap<String, Double> ret = new HashMap<String, Double>();
		String[] servers = getAllServerNames();
		for (String target : servers) {
			double increase = 0;
			for (String from : servers) {
				Double cons = serverConsumptions.get(from);
				if (cons != null && cons != 0.0) {
					increase += cons * get(from, target);
				}
			}
			ret.put(target, increase);
		}
		return ret;
	}
}

package fr.emn.optiplace.thermal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

/**
 * allocation of servers dissipation to the cooling systems.<br />
 * for each server :
 * <ul>
 * <li>no repartition specified. The server is dissipated equally by all the
 * known cooling systems</li>
 * <li>repartition specified for some cooling systems. The default value of
 * repartition for each cooling system is 0 if absent from the map.</li>
 * </ul>
 */
public class HeatRepartition {

	@SuppressWarnings("unused")
	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(HeatRepartition.class);

	HashMap<String, DefaultedHashMap> parts = new HashMap<String, DefaultedHashMap>();
	HashSet<CoolingSystem> knowncoolingsystems = new HashSet<CoolingSystem>();

	/**
	 * add a new {@link CoolingSystem}s
	 * 
	 * @param css
	 *            the new cooling systems
	 */
	public void addCoolingSystem(CoolingSystem... css) {
		if (css != null) {
			for (CoolingSystem cs : css) {
				knowncoolingsystems.add(cs);
			}
		}
	}

	/**
	 * set the repartition of dissipation on one {@link CoolingSystem} to one or
	 * more servers.
	 * 
	 * @param cs
	 *            the cooling system
	 * @param serverName
	 *            the name fo the servers
	 * @param part
	 *            the repartition of dissipation.
	 */
	public void setRepartition(CoolingSystem cs, double part,
			String... serverNames) {
		if (serverNames != null) {
			for (String serverName : serverNames) {
				DefaultedHashMap map = parts.get(serverName);
				knowncoolingsystems.add(cs);
				if (map == null) {
					map = new DefaultedHashMap(0.0);
					parts.put(serverName, map);
				}
				if (part == 0) {
					map.remove(cs);
				} else {
					map.put(cs, part);
				}
			}
		}
	}

	public double getRepartition(CoolingSystem cs, String serverName) {
		HashMap<CoolingSystem, Double> map = parts.get(serverName);
		if (map == null) {
			return 1.0 / knowncoolingsystems.size();
		}
		Double ret = map.get(serverName);
		return ret == null ? 0.0 : ret;
	}

	/**
	 * get the repartition map of a server through the cooling systems
	 * 
	 * @param serverName
	 *            the name of the server
	 * @return a new {@link DefaultedHashMap} with value 1/#coolingsystems if no
	 *         repartition specified, or the internal {@link DefaultedHashMap}.
	 */
	public HashMap<CoolingSystem, Double> getRepartition(String serverName) {
		HashMap<CoolingSystem, Double> ret = parts.get(serverName);
		if (ret == null) {
			return new DefaultedHashMap(1.0 / knowncoolingsystems.size());
		}
		return ret;
	}

	protected static class DefaultedHashMap
			extends
				HashMap<CoolingSystem, Double> {

		private static final long serialVersionUID = 1L;
		protected double val;

		public DefaultedHashMap(Double val) {
			this.val = val;
		}

		@Override
		public Double get(Object arg0) {
			Double ret = super.get(arg0);
			if (ret == null) {
				return val;
			} else {
				return ret;
			}
		}
	}

	/** get the list of servers that are dissipated by a coolingsystem. */
	public HashSet<String> getDissipatedServers(CoolingSystem cs) {
		HashSet<String> ret = new HashSet<String>();
		for (Entry<String, DefaultedHashMap> e : parts.entrySet()) {
			if (e.getValue() == null || e.getValue().containsKey(cs)) {
				ret.add(e.getKey());
			}
		}
		return ret;
	}

}

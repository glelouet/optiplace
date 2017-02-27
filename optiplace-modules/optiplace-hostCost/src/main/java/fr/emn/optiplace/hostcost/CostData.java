package fr.emn.optiplace.hostcost;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.view.ProvidedDataReader;

/**
 * For each hoster of the problem, its cost for a VM placed on it. So total cost
 * = sum(hoster h)(cost(h)*nbVMs(h))
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class CostData implements ProvidedDataReader {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CostData.class);

	/**
	 * for each hoster (extern/node) we may have a given cost of its VM hosting
	 */
	protected Map<String, Integer> byHosterName = new LinkedHashMap<>();

	/**
	 * filter on the name of the host
	 */
	protected Map<Pattern, Integer> byHosterLike = new LinkedHashMap<>();

	/**
	 * for each site we may have a given cost of its VM hosting
	 */
	protected Map<String, Integer> bySiteName = new LinkedHashMap<>();

	/**
	 * filter on the name of the site
	 */
	protected Map<Pattern, Integer> bySiteLike = new LinkedHashMap<>();

	/** default cost of Nodes non set */
	protected int defaultCost = 0;

	protected Integer waitingVMCost = null;

	public int getCost(String hostName, String siteName) {
		Integer ret;
		if (hostName != null) {
			ret = byHosterName.get(hostName);
			if (ret != null) {
				return ret;
			}
			for (Entry<Pattern, Integer> e : byHosterLike.entrySet()) {
				if (e.getKey().matcher(hostName).matches()) {
					return e.getValue();
				}
			}
		}

		if (siteName != null) {
			ret = bySiteName.get(siteName);
			if (ret != null) {
				return ret;
			}
			for (Entry<Pattern, Integer> e : bySiteLike.entrySet()) {
				if (e.getKey().matcher(siteName).matches()) {
					return e.getValue();
				}
			}
		}
		return defaultCost;
	}

	/**
	 * get the cost of a waiting VM . If {@link #waitingVMCost} is set, it is
	 * returned, otherwise the max of all hoster costs is computed and added 1
	 * before returned
	 *
	 * @return the cost of letting a VM in waiting state
	 */
	public int getWaitingVMCost() {
		if (waitingVMCost != null) {
			return waitingVMCost;
		}
		int ret = defaultCost;
		for (Integer val : byHosterName.values()) {
			ret = Math.max(ret, val);
		}
		for (Integer val : byHosterLike.values()) {
			ret = Math.max(ret, val);
		}
		for (Integer val : bySiteName.values()) {
			ret = Math.max(ret, val);
		}
		for (Integer val : bySiteLike.values()) {
			ret = Math.max(ret, val);
		}
		return ret + 1;
	}

	public int getCost(VMHoster h, Site site) {
		return getCost(h.getName(), site != null ? site.getName() : null);
	}

	public void setHostCost(String hostname, int val) {
		byHosterName.put(hostname, val);
	}

	public void setHostCost(VMHoster hoster, int val) {
		setHostCost(hoster.getName(), val);
	}

	public void setSiteCost(String sitename, int val) {
		bySiteName.put(sitename, val);
	}

	public void setSiteCost(Site site, int val) {
		setSiteCost(site.getName(), val);
	}

	public void addHostFilter(String filter, int value) {
		byHosterLike.put(Pattern.compile(filter), value);
	}

	public void addSiteFilter(String filter, int value) {
		bySiteLike.put(Pattern.compile(filter), value);
	}

	public void setDefaultCost(int cost) {
		defaultCost = cost;
	}

	/**
	 * set the default cost of a VM waiting. if set to null, then the default cost
	 * is max(hosts costs)+1 so any VM should be set to run instead of waiting.
	 *
	 * @param cost
	 */
	public void setWaitingVMCost(Integer cost) {
		waitingVMCost = cost;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, Integer> e : byHosterName.entrySet()) {
			sb.append("host(").append(e.getKey()).append(")=").append(e.getValue());
		}
		for (Entry<Pattern, Integer> e : byHosterLike.entrySet()) {
			sb.append("hostLike(").append(e.getKey().pattern()).append(")=").append(e.getValue());
		}
		for (Entry<String, Integer> e : bySiteName.entrySet()) {
			sb.append("site(").append(e.getKey()).append(")=").append(e.getValue());
		}
		for (Entry<Pattern, Integer> e : bySiteLike.entrySet()) {
			sb.append("siteLike(").append(e.getKey().pattern()).append(")=").append(e.getValue());
		}
		return sb.toString();
	}

	@Override
	public void onNewConfig() {
		byHosterLike.clear();
		byHosterName.clear();
		bySiteLike.clear();
		bySiteName.clear();
	}

	static final Pattern HOSTNAMEPATTERN = Pattern.compile("host\\((.*)\\)=(.*)");
	static final Pattern HOSTLIKEPATTERN = Pattern.compile("hostLike\\((.*)\\)=(.*)");
	static final Pattern SITENAMEPATTERN = Pattern.compile("site\\((.*)\\)=(.*)");
	static final Pattern SITELIKEPATTERN = Pattern.compile("siteLike\\((.*)\\)=(.*)");

	@Override
	public void readLine(String line) {
		Matcher m;
		m = HOSTNAMEPATTERN.matcher(line);
		if (m.matches()) {
			byHosterName.put(m.group(1), Integer.parseInt(m.group(2)));
			return;
		}
		m = HOSTLIKEPATTERN.matcher(line);
		if (m.matches()) {
			byHosterLike.put(Pattern.compile(m.group(1)), Integer.parseInt(m.group(2)));
			return;
		}
		m = SITENAMEPATTERN.matcher(line);
		if (m.matches()) {
			bySiteName.put(m.group(1), Integer.parseInt(m.group(2)));
			return;
		}
		m = SITELIKEPATTERN.matcher(line);
		if (m.matches()) {
			bySiteLike.put(Pattern.compile(m.group(1)), Integer.parseInt(m.group(2)));
			return;
		}
	}

}

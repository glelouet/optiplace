package fr.emn.optiplace.network;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.solver.choco.Bridge;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class NetworkData {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkData.class);

	//////////////////////////////////////////////
	// VM use part
	//////////////////////////////////////////////

	public static class VMGroup extends ManagedElement {

		public final int use;
		public final int hashcode;

		/**
		 *
		 */
		public VMGroup(String name, int use) {
			super(name);
			this.use = use;
			hashcode = name.toLowerCase().hashCode() + use;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (this == obj) {
				return true;
			}
			if (obj.getClass() == VMGroup.class) {
				VMGroup g2 = (VMGroup) obj;
				return g2.name.equalsIgnoreCase(name) && g2.use == use;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return hashcode;
		}
	}

	protected HashMap<VM, VMGroup> vm2group = new HashMap<>();

	protected HashMap<String, VMGroup> name2group = new HashMap<>();

	public int use(VM vm1, VM vm2) {
		if (vm1 == null || vm2 == null || vm1.equals(vm2)) {
			return 0;
		}
		VMGroup g = vm2group.get(vm1);
		if (g.equals(vm2group.get(vm2))) {
			return g.use;
		}
		return 0;
	}

	/**
	 * create a group if no group with given name exists
	 *
	 * @param name
	 *          the name of the group
	 * @param use
	 *          the use of the elements of the group
	 * @return existing group if already present, a new group if no group with
	 *         that name present, or null if a group with same name but different
	 *         use is present.
	 */
	public VMGroup addGroup(String name, int use) {
		VMGroup ret = name2group.get(name);
		if (ret == null) {
			ret = new VMGroup(name, use);
			name2group.put(name, ret);
			return ret;
		} else {
			return ret.use == use ? ret : null;
		}
	}

	/**
	 * place VMs in given group
	 *
	 * @param group
	 *          the group to place VM in added if not already present.
	 * @param vms
	 *          the vms to add to the group
	 * @return true if the vms have been added to given group.
	 */
	public boolean addVM(VMGroup group, VM... vms) {
		if (vms == null || vms.length == 0 || group == null) {
			return false;
		}
		VMGroup g2 = addGroup(group.name, group.use);
		if (g2 == null) {
			return false;
		}
		for (VM v : vms) {
			vm2group.put(v, g2);
		}
		return true;
	}

	//////////////////////////////////////////////////
	// Links between Node and extern
	//////////////////////////////////////////////////

	public static class Router extends VMHoster {

		public Router(String name) {
			super(name);
		}

	}

	protected HashMap<String, Router> name2router = new HashMap<>();

	public Router addRouter(String name) {
		Router ret = name2router.get(name);
		if (ret != null) {
			return ret;
		}
		ret = new Router(name);
		name2router.put(name, ret);
		return ret;
	}

	public class Link {

		public final String v0, v1;
		public final int hashCode;

		public Link(String v0, String v1) {
			if (v0.compareToIgnoreCase(v1) < 0) {
				String t = v0;
				v0 = v1;
				v1 = t;
			}
			this.v0 = v0;
			this.v1 = v1;
			hashCode = v0.hashCode() + v1.hashCode();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (obj == this) {
				return true;
			}
			if (obj.getClass() == this.getClass()) {
				Link o = (Link) obj;
				return o.v0.equals(v0) && o.v1.equals(v1);
			}
			return false;
		}

		@Override
		public String toString() {
			return "Link[" + v0 + "-" + v1 + "]";
		}

	}

	/** for each known hoster the list of links it has */
	protected Map<String, Set<Link>> hoster2links = new HashMap<>();

	/** for each existing link, its capacity */
	protected Map<Link, Integer> link2capa = new HashMap<>();

	/**
	 * add capacity between two elements
	 * 
	 * @param h1
	 *          the first element
	 * @param h2
	 *          the second element
	 * @param capa
	 *          the capacity to add to the link
	 * @return null if elements are null,
	 */
	public Link addLink(String h1, String h2, int capa) {
		if (h1 == null || h2 == null || h1.equals(h2)) {
			return null;
		}
		if (h1.compareToIgnoreCase(h2) < 0) {
			String t = h1;
			h1 = h2;
			h2 = t;
		}
		Link link = null;
		Set<Link> set1 = hoster2links.get(h1);
		Set<Link> set2 = hoster2links.get(h2);
		if (set1 == null || set2 == null) {
			if (set1 == null) {
				set1 = new HashSet<>();
				hoster2links.put(h1, set1);
			}
			if (set2 == null) {
				set2 = new HashSet<>();
				hoster2links.put(h2, set2);
			}
		} else {
			for (Link l : set1)
				if (l.v0.equals(h1) || l.v1.equals(h1)) {
					link = l;
					break;
				}
		}
		if (link == null) {
			link = new Link(h1, h2);
			set1.add(link);
			set2.add(link);
			link2capa.put(link, capa);
		} else {
			int newCapa = link2capa.get(link) + capa;
			if (newCapa != 0)
				link2capa.put(link, newCapa);
			else
				link2capa.remove(link);
		}
		return link;
	}

	public Link addLink(VMHoster h1, VMHoster h2, int capa) {
		if (h2 == null | h1 == null)
			return null;
		return addLink(h1.getName(), h2.getName(), capa);
	}

	//////////////////////////////////////////////////
	// Path-finding
	//////////////////////////////////////////////////

	/**
	 * deep-first exploration to find the first path from an hoster to another
	 * 
	 * @param from
	 *          the initial hoster
	 * @param to
	 *          the destination hoster
	 * @return null if no solution, an element is null, or both elements are the
	 *         same.
	 */
	public List<Link> findPath(String from, String to) {
		if (from == null || to == null || from.equals(to))
			return null;
		return findPath(from, to, new HashSet<>(Arrays.asList(from)));
	}

	public List<Link> findPath(VMHoster fromhost, VMHoster tohost) {
		if (fromhost == null || tohost == null)
			return null;
		return findPath(fromhost.getName(), tohost.getName());
	}

	/** recurring deep-first exploration */
	protected List<Link> findPath(String from, String to, Set<String> avoid) {
		for (Link l : hoster2links.get(from)) {
			String target = l.v0.equals(from) ? l.v1 : l.v0;
			if (target.equals(to))
				return new LinkedList<>(Arrays.asList(l));
			if (!avoid.contains(target)) {
				avoid.add(target);
				List<Link> list = findPath(target, to, avoid);
				if (list != null) {
					list.add(0, l);
					return list;
				}
			}
		}
		return null;
	}

	///////////////////////////////////////////////
	// link from objects to indexes
	///////////////////////////////////////////////

	/**
	 * create a new bridge that associates Links to indexes
	 * 
	 * @return a new bridge. This
	 */
	public DataBridge bridge(Bridge b) {
		return new DataBridge(b);
	}

	public class DataBridge {

		final int[] NO_LINK = {};

		protected Link[] linksByIndex = new Link[link2capa.size()];
		protected TObjectIntHashMap<Link> revLinks = new TObjectIntHashMap<>(link2capa.size(),
				Constants.DEFAULT_LOAD_FACTOR, -1);
		protected int[] linkCapa = new int[link2capa.size()];

		// matrix of links index to go from hoster i to hoster j
		protected int[][][] hoster2hoster2links;

		protected final Bridge b;

		public DataBridge(Bridge b) {
			this.b = b;

			// map link <=> index
			int idx = 0;
			for (Entry<Link, Integer> e : link2capa.entrySet()) {
				linksByIndex[idx] = e.getKey();
				revLinks.put(e.getKey(), idx);
				linkCapa[idx] = e.getValue();
				idx++;
			}

			// matrice of links index to go from hoster i to hoster j
			hoster2hoster2links = new int[b.nbHosters()][b.nbHosters()][];
			// first lower left diag : i<j
			for (int i = 0; i < b.nbHosters(); i++) {
				VMHoster from = b.vmHoster(i);
				if (hoster2links.containsKey(from)) {
					hoster2hoster2links[i] = new int[b.nbHosters()][];
					for (int j = 0; j < i; j++) {
						VMHoster to = b.vmHoster(j);
						if (hoster2links.containsKey(to)) {
							List<Integer> l = findPath(from.getName(), to.getName()).stream().map(this::link)
									.collect(Collectors.toList());
							hoster2hoster2links[i][j] = new int[l.size()];
							for (int k = 0; k < l.size(); k++) {
								hoster2hoster2links[i][j][k] = l.get(k);
							}
						} else {
							hoster2hoster2links[i][j] = NO_LINK;
						}
					}
					hoster2hoster2links[i][i] = NO_LINK;
				} else {
					hoster2hoster2links[i] = null;
				}

			}
			// then upper right diag : i>j
			for (int i = 0; i < b.nbHosters(); i++) {
				if (hoster2hoster2links[i] != null)
					for (int j = i + 1; j < b.nbHosters(); j++) {
						hoster2hoster2links[i][j] = hoster2hoster2links[j] == null ? NO_LINK : hoster2hoster2links[j][i];
					}

			}
		}

		public int link(Link l) {
			return revLinks.get(l);
		}

		public Link link(int idx) {
			return (idx < 0 || idx >= linksByIndex.length) ? null : linksByIndex[idx];
		}

		/**
		 * 
		 * @param h1
		 *          the hoster
		 * @param h2
		 * @return
		 */
		public int[] links(VMHoster h1, VMHoster h2) {
			int idxFrom = b.vmHoster(h1), idxto = b.vmHoster(h2);
			if (idxFrom == -1 || idxto == -1)
				return NO_LINK;
			int[][] line = hoster2hoster2links[idxFrom];
			if (line == null)
				return NO_LINK;
			else
				return line[idxto];
		}

	}

}

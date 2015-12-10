package fr.emn.optiplace.network;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.core.ReconfigurationProblem;
import fr.emn.optiplace.network.data.Link;
import fr.emn.optiplace.network.data.Router;
import fr.emn.optiplace.network.data.VMGroup;
import fr.emn.optiplace.solver.choco.Bridge;
import fr.emn.optiplace.view.ProvidedDataReader;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * store data related to a
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class NetworkData implements ProvidedDataReader {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkData.class);

	//////////////////////////////////////////////
	// VM use part
	/////////////////////////////////////////////// **
	/**
	 * * couple of VM. The vms are not final because modifying them reduces memory
	 * management overhead in Set based collections.
	 *
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
	 *
	 */
	public final class VMCouple {

		// for easier test we ensure at construction that v0.hascode<v1.hascode
		// and v0.hashcode==v1.hashcode =>v0.name.compareToIgnoreCase(v1.name) >=0
		// (so v0<v1)
		VM v0, v1;
		int hashcode;
		String toString = null;

		public VMCouple(VM v0, VM v1) {
			update(v0, v1);
		}

		/**
		 * protected copy constructor that does not check the data.
		 *
		 * @param other
		 */
		protected VMCouple(VMCouple other) {
			v0 = other.v0;
			v1 = other.v1;
			hashcode = other.hashcode;
			toString = other.toString;
		}

		@Override
		public int hashCode() {
			return hashcode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			if (obj == null || obj.getClass() != VMCouple.class) {
				return false;
			}
			VMCouple o = (VMCouple) obj;
			return o.v0.equals(v0) && o.v1.equals(v1);
		}

		@Override
		public String toString() {
			if (toString == null) {
				toString = "couple{" + v0 + ", " + v1 + ")";
			}
			return toString;
		}

		@Override
		public VMCouple clone() {
			return new VMCouple(this);
		}

		/** update the internal VM value */
		public void update(VM v0, VM v1) {
			if (v1.hashCode() < v0.hashCode() || v1.hashCode() == v0.hashCode() && v0.name.compareToIgnoreCase(v1.name) > 0) {
				this.v1 = v0;
				this.v0 = v1;
			} else {
				this.v0 = v0;
				this.v1 = v1;
			}
			hashcode = v0.hashCode() + v1.hashCode();
			toString = null;
		}
	}

	protected HashMap<VM, VMGroup> vm2group = new HashMap<>();

	protected HashMap<String, VMGroup> name2group = new HashMap<>();

	protected HashMap<VMGroup, Set<VM>> group2vms = new HashMap<>();

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
			group2vms.put(ret, new HashSet<>());
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
		Set<VM> groupSet = group2vms.get(g2);
		for (VM v : vms) {
			vm2group.put(v, g2);
			groupSet.add(v);
		}
		return true;
	}

	protected TObjectIntHashMap<VMCouple> couple2use = new TObjectIntHashMap<VMCouple>();

	// instead of using a new VMCouple every time we want to check/remove we
	// instead modify this couple.
	private final VMCouple internalCouple = new VMCouple(new VM(""), new VM(""));

	public void setUse(VM v0, VM v1, int use) {
		couple2use.put(new VMCouple(v0, v1), use);
	}

	public void delUse(VM v0, VM v1) {
		internalCouple.update(v0, v1);
		couple2use.remove(internalCouple);
	}

	/**
	 * get the network use between two VM. If there is a direct use specified,
	 * this value is returned ; if the two VM belong to a group , the group value
	 * is returned ; else 0 is returned
	 *
	 * @param vm1
	 *          a VM
	 * @param vm2
	 *          another VM
	 * @return the known connection use of the couple of VM
	 */
	public int use(VM vm1, VM vm2) {
		if (vm1 == null || vm2 == null || vm1.equals(vm2)) {
			return 0;
		}
		internalCouple.update(vm1, vm2);
		if (couple2use.contains(internalCouple)) {
			return couple2use.get(internalCouple);
		}
		VMGroup g = vm2group.get(vm1);
		if (g != null && g.equals(vm2group.get(vm2))) {
			return g.use;
		}
		return 0;
	}

	//////////////////////////////////////////////////
	// Links between Node and extern
	//////////////////////////////////////////////////

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
			for (Link l : set1) {
				if (l.v0.equals(h1) || l.v1.equals(h1)) {
					link = l;
					break;
				}
			}
		}
		if (link == null) {
			link = new Link(h1, h2);
			set1.add(link);
			set2.add(link);
			link2capa.put(link, capa);
		} else {
			int newCapa = link2capa.get(link) + capa;
			if (newCapa != 0) {
				link2capa.put(link, newCapa);
			} else {
				link2capa.remove(link);
			}
		}
		return link;
	}

	public Link addLink(VMHoster h1, VMHoster h2, int capa) {
		if (h2 == null | h1 == null) {
			return null;
		}
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
		if (from == null || to == null || from.equals(to)) {
			return null;
		}
		return findPath(from, to, new HashSet<>(Arrays.asList(from)));
	}

	public List<Link> findPath(VMHoster fromhost, VMHoster tohost) {
		if (fromhost == null || tohost == null) {
			return null;
		}
		return findPath(fromhost.getName(), tohost.getName());
	}

	/** recurring deep-first exploration */
	protected List<Link> findPath(String from, String to, Set<String> avoid) {
		for (Link l : hoster2links.get(from)) {
			String target = l.v0.equals(from) ? l.v1 : l.v0;
			if (target.equals(to)) {
				return new LinkedList<>(Arrays.asList(l));
			}
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
	public NetworkDataBridge bridge(Bridge b) {
		return new NetworkDataBridge(b);
	}

	/**
	 * Bridge between a {@link NetworkData} and a {@link ReconfigurationProblem}.
	 *
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
	 *
	 */
	public class NetworkDataBridge {

		final int[] NO_LINK = {};

		protected Link[] linksByIndex;
		protected TObjectIntHashMap<Link> revLinks;
		protected int[] linkCapaByIndex;

		protected VMCouple[] couplesByIndex;
		protected TObjectIntHashMap<VMCouple> revCouples;
		protected int[] coupleUseByIndex;

		// matrix of links index to go from hoster i to hoster j
		protected int[][][] hoster2hoster2links;

		protected final Bridge b;

		public NetworkDataBridge(Bridge b) {
			this.b = b;

			// map link <=> index
			linksByIndex = new Link[link2capa.size()];
			revLinks = new TObjectIntHashMap<>(link2capa.size(), Constants.DEFAULT_LOAD_FACTOR, -1);
			linkCapaByIndex = new int[link2capa.size()];
			int idx = 0;
			for (Entry<Link, Integer> e : link2capa.entrySet()) {
				linksByIndex[idx] = e.getKey();
				revLinks.put(e.getKey(), idx);
				linkCapaByIndex[idx] = e.getValue();
				idx++;
			}

			// map VM couples <=> index
			// first we get all the couples with non-null use
			LinkedHashSet<VMCouple> couplesl = new LinkedHashSet<>();
			Set<VMCouple> removedCouples = new HashSet<>();
			for (VMCouple c : couple2use.keySet()) {
				if (couple2use.get(c) == 0) {
					removedCouples.add(c);
				} else {
					couplesl.add(c);
				}
			}
			for (Set<VM> set : group2vms.values()) {
				for (VM v1 : set) {
					for (VM v2 : set) {
						if (!v1.equals(v2)) {
							couplesl.add(new VMCouple(v1, v2));
						}
					}
				}
			}
			couplesl.removeAll(removedCouples);
			IConfiguration src = b.source();
			couplesl.removeIf(c -> !(src.hasVM(c.v0) && src.hasVM(c.v1)));
			// then put them in the arrays
			couplesByIndex = couplesl.toArray(new VMCouple[] {});
			revCouples = new TObjectIntHashMap<>();
			coupleUseByIndex = new int[couplesByIndex.length];
			for(int i=0;i<couplesByIndex.length;i++) {
				VMCouple c = couplesByIndex[i];
				revCouples.put(c, i);
				coupleUseByIndex[i] = use(c.v0, c.v1);
			}

			// matrix from hoster i to hoster j => indexes of links to use
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
				if (hoster2hoster2links[i] != null) {
					for (int j = i + 1; j < b.nbHosters(); j++) {
						hoster2hoster2links[i][j] = hoster2hoster2links[j] == null ? NO_LINK : hoster2hoster2links[j][i];
					}
				}

			}
		}

		public int link(Link l) {
			return revLinks.get(l);
		}

		public Link link(int idx) {
			return idx < 0 || idx >= linksByIndex.length ? null : linksByIndex[idx];
		}

		public int nbLinks() {
			return linksByIndex.length;
		}

		/**
		 * get the indexes of the links required to go from one hoster to another
		 *
		 * @param h1
		 *          the first hoster
		 * @param h2
		 *          the second hoster
		 * @return an empty array if an hoster is null, or there is no path from an
		 *         hoster to another ; or an internal array containing the indexes
		 *         of the links if there is a path linking those two hosters
		 */
		public int[] links(VMHoster h1, VMHoster h2) {
			int idxFrom = b.vmHoster(h1), idxto = b.vmHoster(h2);
			if (idxFrom == -1 || idxto == -1) {
				return NO_LINK;
			}
			int[][] line = hoster2hoster2links[idxFrom];
			if (line == null) {
				return NO_LINK;
			} else {
				return line[idxto];
			}
		}

		public int vmcouple(VMCouple c) {
			return revCouples.get(c);
		}

		public VMCouple vmcCouple(int idx) {
			return idx < 0 || idx >= couplesByIndex.length ? null : couplesByIndex[idx];
		}

		public int nbCouples() {
			return couplesByIndex.length;
		}

	}

	@Override
	public void readLine(String line) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("implement this");
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}

}

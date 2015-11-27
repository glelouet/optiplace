package fr.emn.optiplace.solver.choco;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import gnu.trove.map.hash.TObjectIntHashMap;

public class Bridge {

	public static final Logger logger = LoggerFactory.getLogger(Bridge.class);

	private IConfiguration source;

	private VM[] vms;
	private TObjectIntHashMap<VM> revVMs;
	private Node[] nodes;
	private TObjectIntHashMap<Node> revNodes;
	private Extern[] externs;
	private TObjectIntHashMap<Extern> revExterns;
	private Site[] sites;
	private TObjectIntHashMap<Site> revSites;

	/** The current location of the placed VMs. */
	private int[] vmsSourceNode;

	/** node i is in site nodeSites[i] */
	protected int[] nodesSite;

	/**
	 * Bridge between the base configuration and the reconfiguration problem.
	 */
	public Bridge(IConfiguration source) {

		this.source = source;

		vms = source.getVMs().collect(Collectors.toSet()).toArray(new VM[] {});
		revVMs = new TObjectIntHashMap<>(vms.length);
		for (int i = 0; i < vms.length; i++) {
			revVMs.put(vms[i], i);
		}

		nodes = source.getNodes().collect(Collectors.toList()).toArray(new Node[] {});
		revNodes = new TObjectIntHashMap<>(nodes.length);
		for (int i = 0; i < nodes.length; i++) {
			revNodes.put(nodes[i], i);
		}

		externs = source.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
		revExterns = new TObjectIntHashMap<>(externs.length);
		for (int i = 0; i < externs.length; i++) {
			revExterns.put(externs[i], i);
		}

		sites = source.getSites().collect(Collectors.toList()).toArray(new Site[] {});
		revSites = new TObjectIntHashMap<>(sites.length);
		for (int i = 0; i < sites.length; i++) {
			revSites.put(sites[i], i);
		}

		vmsSourceNode = new int[vms.length];
		for (VM vm : vms) {
			vmsSourceNode[vm(vm)] = !source.isRunning(vm) ? -1 : node(source.getNodeHost(vm));
		}

		nodesSite = new int[nodes().length];
		for (int i = 0; i < nodesSite.length; i++) {
			Site site = source.getSite(nodes[i]);
			nodesSite[i] = site == null ? -1 : site(source.getSite(nodes[i]));
		}
	}

	public IConfiguration source() {
		return source;
	}

	/**
	 * @return an array of the nodes of the problem, each at its index position
	 */
	public Node[] nodes() {
		return nodes;
	}

	/**
	 * @param n
	 *          a node of the problem
	 * @return the index of the node in the problem, or -1
	 */
	public int node(Node n) {
		int v = revNodes.get(n);
		if (v == 0 && !nodes[0].equals(n)) {
			return -1;
		}
		return v;
	}

	/**
	 * @param idx
	 * @return the node at given position, or null
	 */
	public Node node(int idx) {
		if (idx < 0) {
			return null;
		}
		Node[] t = nodes();
		return idx >= t.length ? null : t[idx];
	}

	/**
	 * @return the array of vm of this problem, each vm being on its index,
	 *         meaning vm(i)= vms[i]
	 */
	public VM[] vms() {
		return vms;
	}

	/**
	 * converts an array of vms to an array of index of those vms in the problem.
	 *
	 * @param vms
	 *          the vms to convert, all of them must belong to the problem
	 * @return a new array of those vms.
	 */
	public int[] vms(VM... vms) {
		if (vms == null || source.nbVMs() == 0) {
			return null;
		}
		int[] ret = new int[source.nbVMs()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = vm(vms[i]);
		}
		return ret;
	}

	/**
	 * @param vm
	 *          a virtual machine of this problem
	 * @return the internal index for this vm, or -1 if not known
	 */
	public int vm(VM vm) {
		int v = revVMs.get(vm);
		if (v == 0 && !vms[0].equals(vm)) {
			return -1;
		}
		return v;
	}

	/**
	 * @param idx
	 * @return the vm at given pos, or null
	 */
	public VM vm(int idx) {
		if (idx < 0) {
			return null;
		}
		VM[] t = vms();
		return idx >= t.length ? null : t[idx];
	}

	/**
	 *
	 * @return the array of known externs
	 */
	public Extern[] externs() {
		return externs;
	}

	/**
	 *
	 * @param e
	 *          the extern
	 * @return the index of the extern in {@link #externs()} array or -1
	 */
	public int extern(Extern e) {
		int v = revExterns.get(e);
		if (v == 0 && !externs[0].equals(e)) {
			return -1;
		}
		return v;
	}

	/**
	 *
	 * @param idx
	 *          the index of the extern in the {@link #externs()} array
	 * @return the corresponding extern
	 */
	public Extern extern(int idx) {
		if (idx < 0) {
			return null;
		}
		Extern[] t = externs();
		return idx >= t.length ? null : t[idx];
	}

	/**
	 * 
	 * @param h
	 *          an hoster of the configuration
	 * @return the index of the node if a node is given in parameters, the index
	 *         of the extern+#nodes if an exetern is given in parameter, or -1 if
	 *         h not known.
	 */
	public int vmHoster(VMHoster h) {
		if (h == null) {
			return -1;
		}
		if (h instanceof Node) {
			return node((Node) h);
		} else if (h instanceof Extern) {
			return extern((Extern) h) + nodes.length;
		} else {
			logger.warn("incorrect class " + h.getClass());
			return -1;
		}
	}

	public VMHoster vmHoster(int i) {
		if (i < 0 || i >= nodes.length + externs.length) {
			return null;
		}
		return i < nodes.length ? node(i) : extern(i - nodes.length);
	}

	public Site[] sites() {
		return sites;
	}

	public int site(Site site) {
		int v = revSites.get(site);
		if (v == 0 && !sites[0].equals(site)) {
			return -1;
		}
		return v;
	}

	public Site site(int idx) {
		if (idx < 0) {
			return null;
		}
		Site[] t = sites();
		return idx >= t.length ? null : t[idx];
	}

	/**
	 * Get the current location of a VM.
	 *
	 * @param vmIdx
	 *          the index of the virtual machine
	 * @return the node index if exists or -1 if the VM is not already placed
	 */
	public int getCurrentLocation(int vmIdx) {
		if (vmIdx >= 0 && vmIdx < vms.length) {
			return vmsSourceNode[vmIdx];
		}
		return -1;
	}

	public int[] nodesSites() {
		return nodesSite;
	}

}

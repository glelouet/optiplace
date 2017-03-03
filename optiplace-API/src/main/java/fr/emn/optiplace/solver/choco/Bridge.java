package fr.emn.optiplace.solver.choco;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;
import gnu.trove.impl.Constants;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * bridge between the elements and their corresponding arrays
 *
 * <p>
 * nodes and externs are merged in the same "locations" array. also the index of
 * location given as locations.size is return for null, that is if a VM is
 * hosted on a location of index 'location.size()' then it is hosted nowhere
 * </p>
 *
 * @author Guillaume Le Louët
 *
 */
public class Bridge {

	public static final Logger logger = LoggerFactory.getLogger(Bridge.class);

	private final IConfiguration source;

	private VM[] vms;
	private TObjectIntHashMap<VM> revVMs;
	private Node[] nodes;
	private Extern[] externs;
	private VMLocation[] locations;
	private TObjectIntHashMap<VMLocation> revLocations;
	private Site[] sites;
	private TObjectIntHashMap<Site> revSites;

	/** The current location of the placed VMs. */
	private int[] vmSourceLoc;

	/** node i is in site nodeSites[i] */
	protected int[] locationSites;

	/**
	 * Bridge between the base configuration and the reconfiguration problem.
	 */
	public Bridge(IConfiguration source) {

		this.source = source;
		vms = source.getVMs().collect(Collectors.toSet()).toArray(new VM[] {});
		revVMs = new TObjectIntHashMap<>(vms.length, Constants.DEFAULT_LOAD_FACTOR, -1);
		for (int i = 0; i < vms.length; i++) {
			revVMs.put(vms[i], i);
		}

		locations = Stream.concat(source.getNodes(), source.getExterns()).toArray(VMLocation[]::new);
		revLocations = new TObjectIntHashMap<>(locations.length, Constants.DEFAULT_LOAD_FACTOR, locations.length);
		for (int i = 0; i < locations.length; i++) {
			revLocations.put(locations[i], i);
		}
		nodes = source.getNodes().toArray(Node[]::new);
		externs = source.getExterns().toArray(Extern[]::new);

		sites = source.getSites().collect(Collectors.toList()).toArray(new Site[] {});
		revSites = new TObjectIntHashMap<>(sites.length, Constants.DEFAULT_LOAD_FACTOR, -1);
		for (int i = 0; i < sites.length; i++) {
			revSites.put(sites[i], i);
		}

		vmSourceLoc = new int[vms.length];
		for (VM vm : vms) {
			vmSourceLoc[vm(vm)] = !source.isRunning(vm) ? -1 : location(source.getLocation(vm));
		}

		locationSites = new int[locations.length];
		for (int i = 0; i < locationSites.length; i++) {
			Site nodeSite = source.getSite(locations[i]);
			locationSites[i] = nodeSite == null ? -1 : site(nodeSite);
		}
	}

	public IConfiguration source() {
		return source;
	}

	/**
	 * @return an array of the nodes of the problem, each at its index position
	 */
	public VMLocation[] locations() {
		return locations;
	}

	public Node[] nodes() {
		return nodes;
	}

	/**
	 *
	 * @return an internal array of the externs of the problem. Node that this
	 *         array does not correspond to the indexes of the externs
	 */
	public Extern[] externs() {
		return externs;
	}

	/**
	 *
	 * @param locationIndex
	 *          an index of a location
	 * @return true iff this index stands for a node
	 */
	public boolean isNode(int locationIndex) {
		return locationIndex < nodes.length;
	}

	/**
	 * @param n
	 *          a node of the problem
	 * @return the index of the node in the problem, or -1
	 */
	public int location(VMLocation l) {
		return revLocations.get(l);
	}

	/**
	 * @param idx
	 * @return the node at given position, or null
	 */
	public VMLocation location(int idx) {
		if (idx < 0) {
			return null;
		}
		return idx >= locations.length ? null : locations[idx];
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
		return revVMs.get(vm);
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

	public Site[] sites() {
		return sites;
	}

	public int site(Site site) {
		return revSites.get(site);
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
			return vmSourceLoc[vmIdx];
		}
		return -1;
	}

	public int[] locationSites() {
		return locationSites;
	}

}

package entropy.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * modifies a configuration. <br />
 * Each newly added element is named using the number of already added nodes or
 * VMs. ie, if there already are 20 nodes, next node will be named "node21".<br />
 * This default behavior can be modified using
 * <ul>
 * <li>to set the nodes names :<br />
 * <ul>
 * <li>{@link #resetNodeIdx()} reset the index of nodes to 0</li>
 * <li>{@link #setNodeConverter(IdxConverter)} change the conversion of index to
 * string</li>
 * <li>{@link #setNodePrefix(String)} changes the prefix of the nodes</li>
 * </ul>
 * </li>
 * <li>to set the VMs names :<br />
 * <ul>
 * <li>{@link #resetVMIdx()} reset the index of vms to 0</li>
 * <li>{@link #setVMConverter(IdxConverter)} change the conversion of index to
 * string</li>
 * <li>{@link #setVmPrefix(String)} changes the prefix of the vms</li>
 * </ul>
 * </li>
 * </ul>
 * 
 * @author guillaume
 */
public class ConfigurationGenerator {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ConfigurationGenerator.class);

	/** convert int to String using {@link #convert(int)} */
	public static class IdxConverter {

		final int minIdxSize;

		/**
		 * don't call this to build, use {@link #BASIC_CONVERTER} instead which
		 * is a singleton
		 */
		public IdxConverter() {
			this(0);
		}

		/**
		 * construct a converter which ensure the returned converted value will
		 * be at least minIdxSize long; by appending "0"s before.
		 * 
		 * @param minIdxSize
		 *            minimum size of the converted String
		 */
		public IdxConverter(int minIdxSize) {
			this.minIdxSize = minIdxSize;
		}

		/**
		 * convert an int to a String, using internal methods. Override this to
		 * have your own behavior
		 * 
		 * @param idx
		 *            the int to convert
		 * @return a nex String.
		 */
		public String convert(int idx) {
			String ret = "" + idx;
			if (minIdxSize > 0) {
				while (ret.length() < minIdxSize) {
					ret = "0" + ret;
				}
			}
			return ret;
		}

		/** default converter that simply prints the int to a string */
		public static final IdxConverter BASIC_CONVERTER = new IdxConverter();
	}

	/**
   *
   */
	public ConfigurationGenerator() {
		this(new SimpleConfiguration());
	}

	/** @param baseConfiguration */
	public ConfigurationGenerator(Configuration baseConfiguration) {
		setTarget(baseConfiguration);
	}

	private Configuration target;

	/**
	 * @return the target
	 */
	public Configuration getTarget() {
		return target;
	}

	/**
	 * set a target, and updates the VMs and Nodes indexes tothe number of VMs
	 * and Nodes in the target.
	 * 
	 * @param target
	 *            the target to add next VMs and Nodes into
	 */
	public void setTarget(Configuration target) {
		this.target = target;
		vmIdx = target.getAllVirtualMachines().size();
		nodeIdx = target.getAllNodes().size();
	}

	protected int vmIdx = 0;

	/**
	 * reset the internal vm index, used to name the VMs using {@link #vmPrefix}
	 * and {@link #vmIdxConverter}
	 * 
	 * @return this
	 */
	public ConfigurationGenerator resetVMIdx() {
		vmIdx = 0;
		return this;
	}

	protected int nodeIdx = 0;

	/**
	 * reset the internal node index, used to name the Nodes using
	 * {@link #nodePrefix} and {@link #nodeIdxConverter}
	 * 
	 * @return this
	 */
	public ConfigurationGenerator resetNodeIdx() {
		nodeIdx = 0;
		return this;
	}

	/** the prefix to add to nodes */
	public String nodePrefix = "n";

	/**
	 * @param nodePrefix
	 *            the nodePrefix to set
	 */
	public ConfigurationGenerator setNodePrefix(String nodePrefix) {
		this.nodePrefix = nodePrefix;
		return this;
	}

	/**
	 * the converter to convert the indexes to concatenate them on the prefix of
	 * the nodes
	 */
	public IdxConverter nodeIdxConverter = IdxConverter.BASIC_CONVERTER;

	/**
	 * @param converter
	 *            the {@link #nodeIdxConverter} to set
	 * @return this
	 */
	public ConfigurationGenerator setNodeConverter(IdxConverter converter) {
		nodeIdxConverter = converter;
		return this;
	}

	protected ArrayList<Node> lastAddedNodes = new ArrayList<Node>();

	public ConfigurationGenerator selectNodes(Collection<Node> nodes) {
		lastAddedNodes.clear();
		lastAddedNodes.addAll(nodes);
		return this;
	}

	/** @return the nodes which were added with last node adding method. */
	public ArrayList<Node> getLastAddedNodes() {
		return lastAddedNodes;
	}

	/**
	 * add online servers to the target
	 * 
	 * @param num
	 *            number of server to add
	 * @param memSize
	 *            memory capacity of the servers in MB
	 * @param cpuCapa
	 *            the capacity CPU of the servers, in MHz
	 * @return this.
	 */
	public ConfigurationGenerator addNodes(int num, int cpuCapa, int memSize) {
		return addNodes(num, cpuCapa, 1, memSize);
	}

	/**
	 * add online nodes to the target
	 * 
	 * @param num
	 *            number of node to add
	 * @param memSize
	 *            memory capacity of the node in MB
	 * @param coreCapa
	 *            the capacity of each core, in MHz
	 * @param cpunum
	 *            number of CPU per node
	 * @return this.
	 */
	public ConfigurationGenerator addNodes(int num, int coreCapa, int cpunum,
			int memSize) {
		lastAddedNodes.clear();
		for (int i = 1; i <= num; i++) {
			Node added = new SimpleNode(nodePrefix
					+ nodeIdxConverter.convert(nodeIdx), cpunum, coreCapa,
					memSize);
			nodeIdx++;
			lastAddedNodes.add(added);
			target.addOnline(added);
		}
		return this;
	}

	/**
	 * add and select a collection of nodes
	 * 
	 * @param nodes
	 *            the nodes to add as running
	 * @return this
	 */
	public ConfigurationGenerator addNodes(Collection<Node> nodes) {
		for (Node n : nodes) {
			target.addOnline(n);
		}
		lastAddedNodes.clear();
		lastAddedNodes.addAll(nodes);
		return this;
	}

	/** the prefix of next generated VMs */
	public String vmPrefix = "vm";

	/**
	 * @param vmPrefix
	 *            the {@link #vmPrefix} to set
	 * @return this
	 */
	public ConfigurationGenerator setVmPrefix(String vmPrefix) {
		this.vmPrefix = vmPrefix;
		return this;
	}

	/** the converter to append indices to the vm prefixes. */
	public IdxConverter vmIdxConverter = IdxConverter.BASIC_CONVERTER;

	/**
	 * @param converter
	 *            the {@link #vmIdxConverter} to use
	 * @return this
	 */
	public ConfigurationGenerator setVMConverter(IdxConverter converter) {
		vmIdxConverter = converter;
		return this;
	}

	protected ArrayList<VirtualMachine> lastAddedVMs = new ArrayList<VirtualMachine>();

	/** @return the lastAddedVMs */
	public ArrayList<VirtualMachine> getLastAddedVMs() {
		return lastAddedVMs;
	}

	public ConfigurationGenerator selectVMs(Collection<VirtualMachine> vms) {
		lastAddedVMs.clear();
		lastAddedVMs.addAll(vms);
		return this;
	}

	/**
	 * add VMs on last added nodes. Each VM uses 1 CPU.
	 * 
	 * @param num
	 *            number of VM per node
	 * @param memSize
	 *            the size of the memory used by each VM
	 * @param cpuUsage
	 *            the MHz usage of CPU each VM consumes
	 * @param running
	 *            true to set the VMs running on the node, or false to set it
	 *            waiting.
	 * @return this
	 */
	public ConfigurationGenerator addVMs(int num, int cpuUsage, int memSize,
			boolean running) {
		lastAddedVMs.clear();
		for (Node n : lastAddedNodes) {
			for (int i = 1; i <= num; i++) {
				VirtualMachine added = new SimpleVirtualMachine(vmPrefix
						+ vmIdxConverter.convert(vmIdx), 1, cpuUsage, memSize);
				vmIdx++;
				if (running) {
					target.setRunOn(added, n);
				} else {
					target.setSleepOn(added, n);
				}
				lastAddedVMs.add(added);
			}
		}
		return this;
	}

	/**
	 * add a given number of running VMs on last added nodes.<br />
	 * short for {@link #addVMs(int, int, int, boolean)} with last to true;
	 */
	public ConfigurationGenerator addRunnings(int num, int cpuUsage, int memSize) {
		return addVMs(num, cpuUsage, memSize, true);
	}

	public ConfigurationGenerator addWaitings(int num, int cpuUsage, int memSize) {
		lastAddedVMs.clear();
		for (int i = 1; i <= num; i++) {
			VirtualMachine added = new SimpleVirtualMachine(vmPrefix
					+ vmIdxConverter.convert(vmIdx), 1, cpuUsage, memSize);
			target.addWaiting(added);
			lastAddedVMs.add(added);
			vmIdx++;
		}
		return this;
	}

	/**
	 * allocates a collection of VMs on the last added nodes in round-robin. if
	 * we added N nodes, then vms[i] will be running on node N%i
	 * 
	 * @param vms
	 * @return
	 */
	public ConfigurationGenerator distributeVMs(Collection<VirtualMachine> vms) {
		int idx = 0;
		int nodesnum = lastAddedNodes.size();
		for (VirtualMachine vm : vms) {
			Node n = lastAddedNodes.get(idx % nodesnum);
			target.setRunOn(vm, n);
			idx++;
		}
		lastAddedVMs.clear();
		lastAddedVMs.addAll(vms);
		return this;
	}

	/**
	 * add a cluster of Servers and VMs to the configuration.<br />
	 * the nodes and VMs added to the Configuration are also put in
	 * {@link #getLastAddedNodes()} and {@link #getLastAddedVMs()}
	 * 
	 * @param nodeModels
	 *            the models to add to the cfg
	 * @param nodeNames
	 *            for each node model, the names of corresponding nodes
	 * @param vmModels
	 *            the models of the vms that can be added to the nodes
	 * @param vmNames
	 *            for each model i , node name [i][j], vm model[k], the list of
	 *            vms of that model to add to the node named j
	 * @return this
	 */
	public ConfigurationGenerator addCluster(Node[] nodeModels,
			String[][] nodeNames, VirtualMachine[] vmModels,
			String[][][][] vmNames) {
		if (nodeModels == null || nodeModels.length == 0) {
			return this;
		}
		SimpleManagedElementSet<Node> nodes = new SimpleManagedElementSet<Node>();
		SimpleManagedElementSet<VirtualMachine> vms = new SimpleManagedElementSet<VirtualMachine>();
		for (int i = 0; i < nodeModels.length; i++) {
			String[] names = nodeNames[i];
			Node nodeModel = nodeModels[i];
			if (names != null && names.length != 0 && nodeModel != null) {
				for (int j = 0; j < names.length; j++) {
					String nodeName = names[j];
					if (nodeName != null) {
						Node n = nodeModel.clone();
						n.rename(nodeName);
						target.addOnline(n);
						nodes.add(n);
						if (vmModels != null && vmNames != null
								&& vmNames.length > i && vmNames[i] != null
								&& vmNames[i].length > j
								&& vmNames[i][j] != null) {
							for (int k = 0; k < vmModels.length
									&& k < vmNames[i][j].length; k++) {
								if (vmNames[i][j][k] != null) {
									VirtualMachine vmModel = vmModels[k];
									String[] vmIds = vmNames[i][j][k];
									for (String id : vmIds) {
										VirtualMachine vm = vmModel.clone();
										vm.rename(id);
										vms.add(vm);
										target.setRunOn(vm, n);
									}
								}
							}
						}
					}
				}
			}
		}
		lastAddedVMs = vms;
		lastAddedNodes = nodes;
		return this;
	}

	/**
	 * creates several VMS, with the avg CPU of the group given, as well as the
	 * variation around this average
	 * 
	 * @param prefix
	 *            the prefix of the names of he VMS. Each VM is named
	 *            prefix_[idx], idx from 0 to number-1
	 * @param number
	 *            the number of VMs to create
	 * @param RAM
	 *            the memory size of the VMs
	 * @param avgCPU
	 *            the average CPU utilization of the VMs
	 * @param variation
	 *            the max distance to avg a VM's CPU can be. eg, if 1, then the
	 *            lowest CPU VM will have 0 used CPU, while the highest CPU VM
	 *            will have 2*avgCPU
	 * @return a new modifiable arraylist of VMS
	 */
	public static ArrayList<VirtualMachine> bunchOfVMs(String prefix,
			int number, int RAM, int avgCPU, double variation) {
		if (prefix == null) {
			prefix = "vm";
		}
		int idxSize = (int) (Math.log10(number) + 1);
		VirtualMachine[] ret = new VirtualMachine[number];
		double multPerIdx = 2 * variation / (number - 1);
		double minmult = 1 - variation;
		for (int i = 0; i < number; i++) {
			String name = makeName(prefix, i, idxSize);
			double mult = minmult + i * multPerIdx;
			// System.err.println("idx:" + i + " mult:" + mult);
			int cpu = (int) Math.round(mult * avgCPU);
			ret[i] = new SimpleVirtualMachine(name, 1, cpu, RAM);
		}
		return new ArrayList<VirtualMachine>(Arrays.asList(ret));
	}

	/**
	 * make a name prefix_num, such as the num part is of given length
	 * 
	 * @param prefix
	 *            the prefix of the string
	 * @param num
	 *            the number to append to the prefix
	 * @param idxSize
	 *            the number of digits the num part of the name must reach.
	 * @return a new String composed of prefix, a set of "0", and num, with the
	 *         number of "0" and digits of num being idxSize.
	 */
	protected static String makeName(String prefix, int num, int idxSize) {
		String ret = "" + num;
		while (ret.length() < idxSize) {
			ret = "0" + ret;
		}
		return prefix + ret;
	}
}

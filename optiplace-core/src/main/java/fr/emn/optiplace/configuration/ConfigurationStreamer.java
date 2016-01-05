package fr.emn.optiplace.configuration;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fr.emn.optiplace.configuration.resources.MappedResourceSpecification;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * tooling class aiming at mùaking streams of configurations.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class ConfigurationStreamer {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationStreamer.class);

	/**
	 * generates a stream of Configurations which do not specify resources use or
	 * capacities.
	 * 
	 * @param maxVMPerHost
	 *          max number of VM per hoster, only checked if >0.
	 * @param maxCfgSize
	 *          maximum size of the configuration, only checked if >0. see
	 *          {@link #getSize(IConfiguration)}.
	 * @return a new stream containing configurations which only have VM waiting
	 */
	public static Stream<IConfiguration> noResource(int maxVMPerHost, int maxCfgSize) {
		Spliterator<IConfiguration> ret = new Spliterator<IConfiguration>() {

			@Override
			public int characteristics() {
				return DISTINCT | NONNULL | IMMUTABLE | CONCURRENT;
			}

			int nbHosters = 0;
			int nbVMs = 0;
			int nbExterns = 0;

			public IConfiguration makeCfg() {
				IConfiguration ret = new Configuration();
				for (int ei = 0; ei < nbExterns; ei++) {
					ret.addExtern("e_" + ei);
				}
				for (int ni = 0; ni + nbExterns < nbHosters; ni++) {
					ret.addOnline("n_" + ni);
				}
				for (int vi = 0; vi < nbVMs; vi++) {
					ret.addVM("v_" + vi, null);
				}
				return ret;
			}

			@Override
			public boolean tryAdvance(Consumer<? super IConfiguration> action) {
				nbVMs++;
				if (maxVMPerHost > 0 && nbVMs > nbHosters * maxVMPerHost
						|| maxCfgSize > 0 && getSize(nbHosters, nbVMs) > maxCfgSize) {
					nbVMs = 1;
					nbExterns++;
					if (nbExterns > nbHosters) {
						nbExterns = 0;
						nbHosters++;
					}
					if (maxCfgSize > 0 && getSize(nbHosters, nbVMs) > maxCfgSize) {
						return false;
					}
				}
				action.accept(makeCfg());
				return true;
			}

			@Override
			public Spliterator<IConfiguration> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return 0;
			}

		};

		return StreamSupport.stream(ret, false);
	}

	public static long getSize(IConfiguration cfg) {
		return getSize(cfg.nbNodes() + cfg.nbExterns(), cfg.nbVMs());
	}

	public static long getSize(long nbHosters, long nbVMs) {
		return nbHosters + nbVMs + ((long) Math.pow(nbHosters, nbVMs));
	}

	/**
	 * creates a stream of ResourceSpecification to add to a configuration.
	 * 
	 * @param cfg
	 *          the configuration to make the resources for
	 * @param resName
	 *          the name of the resource specification
	 * @param maxNodeRes
	 *          the maximum total resource of the nodes
	 * @param maxVMMult
	 *          maximum total resource use of the VM for each node resource capa.
	 * @return a new Stream.
	 */
	public static Stream<ResourceSpecification> addResource(IConfiguration cfg, String resName, int maxNodeRes,
			float maxVMMult) {
		Spliterator<ResourceSpecification> ret = new Spliterator<ResourceSpecification>() {

			@Override
			public int characteristics() {
				return DISTINCT | NONNULL | IMMUTABLE | CONCURRENT;
			}

			@Override
			public Spliterator<ResourceSpecification> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return 0;
			}

			boolean stop = false;

			Node[] nodes = cfg.getNodes().collect(Collectors.toList()).toArray(new Node[] {});
			int[] nodesCapas = new int[nodes.length];
			int nodesMinCap, nodesMaxCap;
			int maxVMTotalUse = 0;

			VM[] vms = cfg.getVMs().collect(Collectors.toList()).toArray(new VM[] {});
			int[] vmsUses = new int[vms.length];
			// at most as many different values than we have vms.
			int[] uniqVMsUses = new int[vms.length];
			int nbuniqVMs = 0;
			// the index of the last VM which use == same index node capa.
			int lastMaxVMIdx = -1;
			int totalVMUse = 0;

			Extern[] externs = cfg.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
			// the index of the extern on the uniq VM use array.
			// we must ensure they are decreasing
			int[] externsCapasIndex = new int[externs.length];

			{
				for (int i = 0; i < nodesCapas.length; i++)
					nodesCapas[i] = 1;
				updateNodesStats();
				resetVMs();
				if (externs.length > 0) {
					resetExterns();
					externsCapasIndex[externsCapasIndex.length - 1] = -1;
				} else {
					vmsUses[vmsUses.length - 1] = 0;
				}
			}

			/** extract the specification from the arrays data */
			public ResourceSpecification makeSpec() {
				ResourceSpecification ret = new MappedResourceSpecification(resName);
				for (int i = 0; i < nodes.length; i++)
					ret.capacity(nodes[i], nodesCapas[i]);
				if (externs.length > 0)
					for (int i = 0; i < externs.length; i++)
						ret.capacity(externs[i], uniqVMsUses[externsCapasIndex[i]]);
				for (int i = 0; i < vms.length; i++)
					ret.use(vms[i], vmsUses[i]);
				return ret;
			}

			@Override
			public boolean tryAdvance(Consumer<? super ResourceSpecification> action) {
				if (stop)
					return false;
				if (externs.length > 0)
					nextExtern();
				else
					nextVM();
				if (!stop) {
					action.accept(makeSpec());
				}
				return !stop;
			}

			/**
			 * find the next extern with given vms and nodes.
			 * 
			 * @return true if an extern was found, false if no more extern could be
			 *         found. If false, then we must advance to next vm specifications
			 *         and reset the externs.
			 */
			protected boolean nextExtern() {
				for (int i = externs.length - 1; i >= 0; i--) {
					if ((i == 0 || externsCapasIndex[i] < externsCapasIndex[i - 1]) && externsCapasIndex[i] < nbuniqVMs) {
						externsCapasIndex[i]++;
						for (int j = i + 1; j < externsCapasIndex.length; j++) {
							externsCapasIndex[j] = 0;
						}
						return true;
					}
				}
				return false;
			}

			protected void resetExterns() {
				for (int i = 0; i < externsCapasIndex.length; i++) {
					externsCapasIndex[i] = 0;
				}
			}

			protected boolean nextVM() {
				// we must remove at least one res from a VM to increase the previous VM
				if (totalVMUse == maxVMTotalUse) {
					int removable = 0;
					for (int i = vms.length - 1; i >= 0; i--) {
						removable += (vmsUses[i] - 1);

					}
				} else
					for (int i = vms.length - 1; i >= 0; i--) {
						// we could not modify the last i.. n VM
						// and the VMs from 0 to i have
						// same use as their node capacity.
						if (isVMOverNode(i))
							return false;
						if ((i == 0 || vmsUses[i] < vmsUses[i - 1])) {
							increaseVM(i);
							return true;
						}
					}
				return false;
			}

			protected boolean isVMOverNode(int vmidx) {
				if (lastMaxVMIdx >= vmidx - 1 && vmidx < nodes.length && vmsUses[vmidx] == nodesCapas[vmidx])
					return true;
				return false;
			}

			/**
			 * increase the load of a vm at given index. set next Vm load to 1.
			 * 
			 * @param vmidx
			 */
			protected void increaseVM(int vmidx) {
				vmsUses[vmidx]++;
				totalVMUse += 1;
				if (lastMaxVMIdx == vmidx - 1 && vmidx < nodes.length && vmsUses[vmidx] == nodesCapas[vmidx])
					lastMaxVMIdx = vmidx;
				for (int i = vmidx + 1; i < vms.length; i++) {
					totalVMUse -= (vmsUses[i] - 1);
					vmsUses[i] = 1;
				}
			}

			protected void resetVMs() {
				for (int i = 0; i < vmsUses.length; i++) {
					vmsUses[i] = 1;
				}
				updateVMsStats();
				lastMaxVMIdx = -1;
				for (int i = 0; i < vmsUses.length; i++) {
					if (lastMaxVMIdx == i - 1 && i < nodes.length && vmsUses[i] == nodesCapas[i])
						lastMaxVMIdx = i;
				}
				totalVMUse = vms.length;
			}

			protected void updateVMsStats() {
				nbuniqVMs = 0;
				totalVMUse = 0;
				for (int i = 0; i < vmsUses.length; i++) {
					if (i == 0 || vmsUses[i] != vmsUses[i - 1]) {
						uniqVMsUses[nbuniqVMs] = vmsUses[i];
						nbuniqVMs++;
					}
				}
			}

			protected void updateNodesStats() {
				nodesMaxCap = 0;
				nodesMinCap = Integer.MAX_VALUE;
				int totalNodeCapa = 0;
				for (int i = 0; i < nodesCapas.length; i++) {
					nodesMaxCap = Math.max(nodesMaxCap, nodesCapas[i]);
					nodesMinCap = Math.min(nodesMinCap, nodesCapas[i]);
					totalNodeCapa += nodesCapas[i];
				}
				maxVMTotalUse = (int) (totalNodeCapa * maxVMMult);
			}

		};

		return StreamSupport.stream(ret, false);
	}

	public static void main(String[] args) {
		System.err.println(noResource(-1, 1000).count());
	}
}

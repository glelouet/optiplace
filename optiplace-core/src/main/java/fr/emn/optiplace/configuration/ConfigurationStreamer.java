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

	public static Stream<ResourceSpecification> addResource(IConfiguration cfg, String resName, int maxRes) {
		Spliterator<ResourceSpecification> ret = new Spliterator<ResourceSpecification>() {

			Node[] nodes = cfg.getNodes().collect(Collectors.toList()).toArray(new Node[] {});
			int[] nodesCapas = new int[nodes.length];
			VM[] vms = cfg.getVMs().collect(Collectors.toList()).toArray(new VM[] {});
			int[] vmsUses = new int[vms.length];
			int totalVMUse = 0;
			Extern[] externs = cfg.getExterns().collect(Collectors.toList()).toArray(new Extern[] {});
			int[] externsCapas = new int[externs.length];


			@Override
			public int characteristics() {
				return DISTINCT | NONNULL | IMMUTABLE | CONCURRENT;
			}

			public ResourceSpecification makeSpec() {
				ResourceSpecification ret = new MappedResourceSpecification(resName);
				for (int i = 0; i < nodes.length; i++)
					ret.capacity(nodes[i], nodesCapas[i]);
				for (int i = 0; i < externs.length; i++)
					ret.capacity(externs[i], externsCapas[i]);
				for (int i = 0; i < vms.length; i++)
					ret.use(vms[i], vmsUses[i]);
				return ret;
			}

			@Override
			public boolean tryAdvance(Consumer<? super ResourceSpecification> action) {
				// try to increase vms' use
				if (!steepVMsUse()) {
					totalVMUse += 1;
					if (!reinitVMUses()) {
						// too much vms uses : set it back to 1 and increase the externs
						return false;
					}
				}

				action.accept(makeSpec());
				return true;
			}

			// try to lower the use of a VM and increase the use of the previous vm
			protected boolean steepVMsUse() {
				for (int i = 0; i < vms.length - 1; i++) {
					if (vmsUses[i + 1] > vmsUses[i] + 1) {
						vmsUses[i] += 1;
						vmsUses[i + 1] -= 1;
						return true;
					}
				}
				return false;
			}

			protected boolean reinitVMUses() {
					for (int i = 0; i < vmsUses.length; i++)
						vmsUses[i] = 0;
					vmsUses[vmsUses.length] = totalVMUse;
					int maxUse = Math.max(nodesCapas[nodesCapas.length], externsCapas[externsCapas.length]);
					for (int i = vmsUses.length - 1; i > 1; i++)
						if (vmsUses[i] > maxUse) {
							vmsUses[i - 1] += (vmsUses[i] - maxUse);
							vmsUses[i] = maxUse;
						}
				return vmsUses[0] <= maxUse;
			}

			@Override
			public Spliterator<ResourceSpecification> trySplit() {
				return null;
			}

			@Override
			public long estimateSize() {
				return 0;
			}

		};

		return StreamSupport.stream(ret, false);
	}

	public static void main(String[] args) {
		System.err.println(noResource(-1, 1000).count());
	}
}

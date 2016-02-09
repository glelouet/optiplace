
package fr.emn.optiplace.ha;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.emn.optiplace.actions.ActionGraph;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.ha.actions.MigrateHA;
import fr.emn.optiplace.ha.goals.MigrationReducerGoal;
import fr.emn.optiplace.ha.rules.Replication;
import fr.emn.optiplace.ha.rules.Root;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.Rule;
import fr.emn.optiplace.view.ViewDataProvider;
import fr.emn.optiplace.view.annotations.Goal;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

/**
 * <p>
 * The HA view add some rules to the placement of VMs.
 * </p>
 * <p>
 * Those rules modify the allowed locations of the VMs on the nodes
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 */
@ViewDesc
public class HAView extends EmptyView {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HAView.class);

	@Parameter(confName = "ha")
	protected HAData data = new HAData();

	public HAData getData() {
		return data;
	}

	// vm that has to be replicated -> shadow VM on its location
	protected HashMap<VM, VM> tempReplicateVMs = new HashMap<>();
	// the rules we had to simulate the HAVM rules.
	protected List<Rule> tempReplicateRules = new ArrayList<>();

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
	}

	@Override
	public boolean setConfs(ViewDataProvider prv) {
		if (super.setConfs(prv)) {
			updateRules();
			return true;
		} else {
			return false;
		}
	}

	/** update the internal rules fom the HADATA */
	public void updateRules() {
		if (!requestedRules.isEmpty()) {
			logger.warn("discarding rules " + requestedRules + " from " + getClass().getSimpleName()
					+ " to load those provided by its data. Consider empty it to remove this message or adding the rules to the data provider instead of the view");
		}
		requestedRules = new ArrayList<>(data.getRules());
	}

	@Override
	public void preProcessConfig(IConfiguration config) {
		tempReplicateRules.clear();
		tempReplicateVMs.clear();
		List<VM> replicateVM = getRequestedRules().filter(r -> r instanceof Replication)
				.map(r -> ((Replication) r).getVMs()).flatMap(e -> e).distinct().collect(Collectors.toList());
		if (!replicateVM.isEmpty()) {
			String prefix = "HA";
			// find a prefix which no VM starts with. just add '_' after the prefix
			boolean VMWithPrefixFound = false;
			do {
				prefix = prefix + '_';
				String finalPrefix = prefix;
				VMWithPrefixFound = config.getVMs().parallel().map(VM::getName).filter(s -> s.startsWith(finalPrefix)).findAny()
						.isPresent();
			} while (VMWithPrefixFound);
			logger.trace("HAVM clones prefix is " + prefix);
			for (VM vm : replicateVM) {
				// we only make move the VMs that are hosted on a node and not
				// migrating.
				if (!config.isMigrating(vm) && config.getLocation(vm) != null) {
					VM shadow = config.addVM(prefix + vm.getName(), config.getLocation(vm));
					for (ResourceSpecification r : config.resources().values()) {
						r.use(shadow, r.getUse(vm));
					}
					tempReplicateVMs.put(vm, shadow);
					Root root = new Root(shadow);
					tempReplicateRules.add(root);
					// the VM is migrated in the replicate injection. the view only
					// creates a shadow VM for each replicating VM.
				}
			}
		}
		requestedRules.addAll(tempReplicateRules);
	}

	@Override
	public void postProcessConfig(IConfiguration config) {
		// we need to clean both the src and dest configurations
		IConfiguration src = pb.getSourceConfiguration();
		// each HAVM must be migrating on the dest config
		for (Entry<VM, VM> e : tempReplicateVMs.entrySet()) {
			VM v = e.getKey();
			VM cloned = e.getValue();
			VMHoster target = config.getLocation(v);
			config.setHost(v, config.getLocation(cloned));
			config.remove(cloned);
			src.remove(cloned);
			config.setMigTarget(v, target);
		}
		requestedRules.removeAll(tempReplicateRules);
	}

	@Override
	public void extractActions(ActionGraph a, IConfiguration dest) {
		// for each vm previously HAVM and not migrating, we add an migrateHA order
		for (VM v : tempReplicateVMs.keySet()) {
			a.add(new MigrateHA(v, dest.getLocation(v), dest.getMigTarget(v)));
		}
	}

	@Goal
	public MigrationReducerGoal migrationCost() {
		if (pb.knownResources().isEmpty()) {
			return null;
		}
		String resName = null;
		for (String s : new String[] { "mem", "ram", "cpu" }) {
			for (String key : pb.knownResources()) {
				if (s.equals(key.toLowerCase())) {
					resName = key;
				}
				break;
			}
			if (resName != null) {
				break;
			}
		}
		if (resName == null) {
			resName = pb.knownResources().iterator().next();
		}
		return new MigrationReducerGoal(resName);
	}

}

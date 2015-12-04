package fr.emn.optiplace.view.linearpower.migrationcost;

import java.util.Map;

import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.view.linearpower.MigrationCostMaker;

/**
 * cost of migration is null.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class MigrationCostNull implements MigrationCostMaker {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MigrationCostNull.class);

	public static final MigrationCostNull INSTANCE = new MigrationCostNull();

	@Override
	public int migrationCost(VM vm, Map<String, ResourceSpecification> resources) {
		return 0;
	}

	@Override
	public int[] migrationCosts(VM[] vms, Map<String, ResourceSpecification> resources) {
		return new int[vms.length];
	}

}

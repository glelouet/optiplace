/**
 *
 */
package fr.emn.optiplace.view.linearpower.migrationcost;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.view.linearpower.MigrationCostMaker;
import fr.emn.optiplace.view.linearpower.migrationcost.MigrationCostFactory.MigrationCostParser;

/**
 * multilinear model of energy consumption to migrate a vm. the cost is
 * multilinear with each considered resource.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class MigrationCostModelMemory implements MigrationCostMaker {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
	.getLogger(MigrationCostModelMemory.class);

	/** one J per MB */
	protected double mmemMult = 1;

	public MigrationCostModelMemory withMemoryMult(double mult) {
		mmemMult = mult;
		return this;
	}

	/**
	 * @param vm
	 *            the vm to migrate
	 * @param resources
	 *            the resources to know what vm consumes
	 * @return
	 */
	@Override
	public int migrationCost(VM vm,
			Map<String, ResourceSpecification> resources) {
		ResourceSpecification mem = resources.get("MEM");
		return (int) (mmemMult * mem.getUse(vm));
	}

	public static final class Parser implements MigrationCostParser {

		/** */
		public static final Pattern memLinear = Pattern
				.compile("memLinear(?:\\(\\d*\\))?");

		@Override
		public MigrationCostMaker parse(String s) {
			Matcher m = memLinear.matcher(s);
			if (m.matches()) {
				String p = m.group(1);
				MigrationCostModelMemory ret = new MigrationCostModelMemory();
				if (p != null) {
					ret.mmemMult = Integer.parseInt(p);
				}
				return ret;
			}
			return null;
		}

		public static final Parser INSTANCE = new Parser();
	}
}

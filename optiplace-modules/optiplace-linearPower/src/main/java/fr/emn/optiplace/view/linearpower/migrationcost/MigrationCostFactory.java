/**
 *
 */
package fr.emn.optiplace.view.linearpower.migrationcost;

import fr.emn.optiplace.view.linearpower.MigrationCostMaker;

/**
 * translates string to migration cost makers.<br />
 * uses several {@link #parsers}, each tried after the other in order to parse a
 * string.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class MigrationCostFactory {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(MigrationCostFactory.class);

	/** an interface to parse String to migrationCostMaker */
	public static interface MigrationCostParser {

		public MigrationCostMaker parse(String s);

	}

	protected MigrationCostParser[] parsers = new MigrationCostParser[]{};

	public MigrationCostFactory(MigrationCostParser... parsers) {
		this.parsers = parsers;
	}

	public MigrationCostMaker makeMigrationCost(String param) {
		for (MigrationCostParser m : parsers) {
			MigrationCostMaker r = m.parse(param);
			if (r != null) {
				return r;
			}
		}
		logger.warn("could not translate the string " + param
				+ " in a correct " + MigrationCostMaker.class.getSimpleName());
		return null;
	}

	public static final MigrationCostFactory DEFAULT = new MigrationCostFactory(
			MigrationCostModelMemory.Parser.INSTANCE);
}

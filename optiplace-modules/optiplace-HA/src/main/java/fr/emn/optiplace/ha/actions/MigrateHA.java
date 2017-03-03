package fr.emn.optiplace.ha.actions;

import fr.emn.optiplace.actions.Migrate;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMLocation;

public class MigrateHA extends Migrate {

	public MigrateHA(VM vm, VMLocation from, VMLocation to) {
		super(vm, from, to);
	}

	public MigrateHA(Migrate origin) {
		this(origin.getVM(), origin.getFrom(), origin.getTo());
	}

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MigrateHA.class);

}

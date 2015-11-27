/**
 *
 */

package fr.emn.optiplace.actions;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;


/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class Migrate implements Action {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Migrate.class);

	public static void extractMigrations(IConfiguration from, IConfiguration to, ActionGraph actions) {
		from.getRunnings().filter(to::isRunning).filter(e -> !from.getLocation(e).equals(to.getLocation(e)))
		    .forEach(vm -> actions.add(new Migrate(vm, from.getLocation(vm), to.getLocation(vm))));
	}

	VM vm;
	VMHoster from;
	VMHoster to;

	/**
	 * @param vm
	 * @param from
	 * @param to
	 */
	public Migrate(VM vm, VMHoster from, VMHoster to) {
		super();
		this.vm = vm;
		this.from = from;
		this.to = to;
	}

	public VM getVM() {
		return vm;
	}

	public VMHoster getFrom() {
		return from;
	}

	public VMHoster getTo() {
		return to;
	}

	@Override
	public boolean canApply(IConfiguration cfg) {
		if (!cfg.getLocation(vm).equals(from)) {
			return false;
		}
		for (ResourceSpecification r : cfg.resources().values()) {
			if (!r.canHost(cfg, to, vm)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean apply(IConfiguration cfg) {
		if (!canApply(cfg)) {
			return false;
		}
		return cfg.setHost(vm, to);
	}

	@Override
	public boolean isRelated(ManagedElement me) {
		return vm.equals(me) || from.equals(me) || to.equals(me);
	}

	@Override
	public String toString() {
		return "migrate[" + vm.getName() + " from " + from.getName() + " to " + to.getName() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj) || obj != null && obj.getClass() == getClass() && equalsMigrate((Migrate) obj);
	}

	public boolean equalsMigrate(Migrate other) {
		return vm.equals(other.vm) && from.equals(other.vm) && to.equals(other.to);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}

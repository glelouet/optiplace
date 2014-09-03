/**
 *
 */
package fr.emn.optiplace.actions;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public class Migrate implements Action {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(Migrate.class);

	VirtualMachine vm;
	Node from;
	Node to;

	/**
	 * @param vm
	 * @param from
	 * @param to
	 */
	public Migrate(VirtualMachine vm, Node from, Node to) {
		super();
		this.vm = vm;
		this.from = from;
		this.to = to;
	}

	@Override
	public boolean canApply(Configuration cfg) {
		if (!cfg.getRunnings(from).contains(vm)) {
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
	public boolean apply(Configuration cfg) {
		if (!canApply(cfg)) {
			return false;
		}
		return cfg.setRunOn(vm, to);
	}

	@Override
	public boolean isRelated(ManagedElement me) {
		return vm.equals(me) || from.equals(me) || to.equals(me);
	}

	public static void addMigrations(Configuration from, Configuration to,
			ActionGraph actions) {
		for (VirtualMachine vm : from.getRunnings()) {
			Node n1 = from.getLocation(vm);
			Node n2 = to.getLocation(vm);
			if (n2 != null && !n2.equals(n1)) {
				actions.add(new Migrate(vm, n1, n2));
			}
		}
	}

	@Override
	public String toString() {
		return "migrate[" + vm.getName() + " from " + from.getName() + " to "
				+ to.getName() + "]";
	}
}

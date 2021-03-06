/**
 *
 */

package fr.emn.optiplace.actions;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;


/**
 * Allocate a VM (not running) on a Server
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class Allocate implements Action {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Allocate.class);

	public static void extractAllocates(IConfiguration from, IConfiguration to, ActionGraph actions) {
		from.getWaitings().filter(to::isRunning).forEach(vm -> actions.add(new Allocate(vm, to.getComputerHost(vm))));
	}

	protected VM vm;
	protected Computer node;

	/**
	 *
	 */
	public Allocate(VM vm, Computer node) {
		this.vm = vm;
		this.node = node;
	}

	public VM getVM() {
		return vm;
	}

	public Computer getComputer() {
		return node;
	}

	@Override
	public boolean canApply(IConfiguration cfg) {
		if (!cfg.isWaiting(vm)) {
			return false;
		}

		for (ResourceSpecification r : cfg.resources().values()) {
			if (!r.canHost(cfg, node, vm)) {
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
		return cfg.setHost(vm, node);
	}

	@Override
	public boolean isRelated(ManagedElement me) {
		return vm.equals(me) || node.equals(me);
	}

	@Override
	public String toString() {
		return "allocate[" + vm + " on " + node + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj == this) {
			return obj != null;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		Allocate b = (Allocate) obj;
		return b.vm.equals(vm) && b.node.equals(node);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}

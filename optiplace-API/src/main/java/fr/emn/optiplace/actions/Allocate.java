/**
 *
 */

package fr.emn.optiplace.actions;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.Node;
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

	public static void extractAllocates(Configuration from, Configuration to, ActionGraph actions) {
		from.getWaitings().filter(to::isRunning).forEach(vm -> actions.add(new Allocate(vm, to.getNodeHost(vm))));
	}

	protected VM vm;
	protected Node node;

	/**
   *
   */
	public Allocate(VM vm, Node node) {
		this.vm = vm;
		this.node = node;
	}

	public VM getVM() {
		return vm;
	}

	public Node getNode() {
		return node;
	}

	@Override
	public boolean canApply(Configuration cfg) {
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
	public boolean apply(Configuration cfg) {
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

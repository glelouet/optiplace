/**
 *
 */
package fr.emn.optiplace.actions;

import fr.emn.optiplace.center.configuration.Configuration;
import fr.emn.optiplace.center.configuration.ManagedElement;
import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.center.configuration.VM;
import fr.emn.optiplace.center.configuration.resources.ResourceSpecification;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class Allocate implements Action {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Allocate.class);

    public static void extractAllocates(Configuration from, Configuration to, ActionGraph actions) {
	from.getWaitings().filter(to::isRunning).forEach(vm -> actions.add(new Allocate(vm, to.getLocation(vm))));
    }

    VM vm;
    Node node;

    /**
     *
     */
    public Allocate(VM vm, Node node) {
	this.vm = vm;
	this.node = node;
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
	return "allocate(" + vm + " on " + node + ")";
    }
}

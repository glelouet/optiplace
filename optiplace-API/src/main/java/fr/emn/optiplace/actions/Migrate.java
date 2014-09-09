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
  public boolean apply(Configuration cfg) {
    if (!canApply(cfg)) {
      return false;
    }
    return cfg.setHost(vm, to);
  }

  @Override
  public boolean isRelated(ManagedElement me) {
    return vm.equals(me) || from.equals(me) || to.equals(me);
  }

  public static void extractMigrations(Configuration from, Configuration to,
      ActionGraph actions) {
    from.getRunnings()
        .filter(to::isRunning)
        .filter(e -> !from.getLocation(e).equals(to.getLocation(e)))
        .forEach(
            vm -> actions.add(new Migrate(vm, from.getLocation(vm), to
                .getLocation(vm))));
  }

  @Override
  public String toString() {
    return "migrate[" + vm.getName() + " from " + from.getName() + " to "
        + to.getName() + "]";
  }
}

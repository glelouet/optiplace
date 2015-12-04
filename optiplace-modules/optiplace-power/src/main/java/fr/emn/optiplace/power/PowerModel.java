package fr.emn.optiplace.power;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.chocosolver.solver.variables.IntVar;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * the consumption model for a server, based on the configuration usage. The
 * server is considered running.
 *
 * @author guillaume
 */
public interface PowerModel {

  /**
   * get the consumption of a server, according to the VMs allocated on it
   *
   * @param cfg
   *            the configuration of the VMs on the Node.
   * @param n
   *            the node to evaluate the consumption using this model.
   * @return the consumption of the Node
   */
  default double getConsumption(IConfiguration cfg,
      Map<String, ResourceSpecification> specs, Node n) {
    VM[] vms = cfg.getHosted(n).collect(Collectors.toList())
        .toArray(new VM[] {});
    return getConsumption(specs, n, vms);
  }

  /**
   * @param specs
   *            the specifications of at least the node and its vms
   * @param n
   *            the node to consider
   * @param vms
   *            the vms hosted on the node
   * @return the consumption of the node
   */
  public double getConsumption(Map<String, ResourceSpecification> specs,
      Node n, VM... vms);

  /**
   * computes the best efficiency a set of the same vm can have on the node.
   *
   * @param specs
   *            the resources of at least the node and the vm
   * @param n
   *            the node which follows the model
   * @param vm
   *            the virtual machine the sets contain
   * @return the best #vm/consumption possible for given vm and node, with the
   *         number of VMs varying from 0 to +inf<br />
   *         The simplest way to do that is to use {@link
   *         ConfigurationTools.#getMaxVMs(Map, Node, VirtualMachine)} and
   *         iterate from 0 to this max number of vms (int)
   */
  default double getBestEfficiency(Map<String, ResourceSpecification> specs,
      Node n, VM vm) {
    int maxVMs = specs.values().stream()
.mapToInt(r -> r.getCapacity(n) / r.getUse(vm)).max().getAsInt();
    double maxEff = 0.0;
    for (int i = 1; i <= maxVMs; i++) {
      VM[] vms = new VM[i];
      Arrays.fill(vms, vm);
      double eff = getConsumption(specs, n, vms) / i;
      if (eff > maxEff) {
        maxEff=eff;
      }
    }
    return maxEff;
  }

  /**
   * make a variable and constraint it to the consumption of a server using
   * this model.
   *
   * @param n
   *            the server to get the consumption
   * @param parent
   *            the view to add constraints and vars in
   * @return a new variable constrained to the consumption of the given node
   */
  public IntVar makePower(Node n, PowerView parent);

  /**
   * get the minimum consumption increase due to the allocation of a vm on a
   * node.
   *
   * @param n
   *            the node to allocate the VM
   * @param v
   *            the VM to allocate on the node
   * @return the minimum garantee value of increased consumption of the vm is
   *         allocated on the node
   */
  public double getMinPowerIncrease(Node n,
      Map<String, ResourceSpecification> specs, VM v);

  /**
   * get the maximum cons a node of this model can be asigned
   *
   * @param n
   *            the node
   * @return
   */
  public double maxCons(Node n);

	public static interface Parser {

		/**
		 * parse a String to a power model
		 * 
		 * @param s
		 *          a string describing the model
		 * @return a power model if the parser was able to parse it, null otherwise
		 */
		public PowerModel parse(String s);
	}

}

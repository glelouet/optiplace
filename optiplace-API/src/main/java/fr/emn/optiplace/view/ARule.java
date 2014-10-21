package fr.emn.optiplace.view;

import java.util.*;
import java.util.stream.Collectors;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;

/** @author guillaume */
public abstract class ARule implements Rule {

  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(ARule.class);

	protected static <T> Set<T> makeSet(T[] elems) {
		if (elems.length == 0) {
			return Collections.emptySet();
		}
		if (elems.length == 1) {
			return Collections.singleton(elems[0]);
		}
		return new HashSet<>(Arrays.asList(elems));
	}

  /**
   * @param nodes
   * the nodes concerned by this rule
   * @param VMs
   * the vms concerned by this rule
   */
  public ARule(Set<Node> nodes, Set<VM> VMs) {
    if (nodes != null) {
			this.nodes = nodes;
    }
    if (VMs != null) {
      vms = VMs;
    }
  }

  /**
   * @param nodes
   * the nodes concerned by this rule
   * @param virtualMachines
   * the vms concerned by this rule
   */
  public ARule(Node[] nodes, VM[] virtualMachines) {
    if (nodes != null && nodes.length > 0) {
      this.nodes.addAll(Arrays.asList(nodes));
    }
    if (virtualMachines != null && virtualMachines.length > 0) {
      vms.addAll(Arrays.asList(virtualMachines));
    }
  }

  protected Set<VM> vms = Collections.emptySet();

  @Override
  public Set<VM> getVMs() {
    return vms;
  }

  public Set<VM> setVMs(Set<VM> vms) {
    if (vms == null) {
      vms = Collections.emptySet();
    }

    Set<VM> ret = this.vms;
    this.vms = vms;
    return ret;
  }

  protected Set<Node> nodes = Collections.emptySet();

  @Override
  public Set<Node> getNodes() {
    return nodes;
  }

  public Set<Node> setNodes(Set<Node> node2s) {
    if (node2s == null) {
      node2s = Collections.emptySet();
    }
    Set<Node> ret = nodes;
    nodes = node2s;
    return ret;
  }

  @Override
  public Type getType() {
    return Type.DYNAMIC;
  }

  protected List<Node> keepOnlines(Configuration cfg) {
    return nodes.stream().filter(cfg::isOnline).collect(Collectors.toList());
  }

  protected List<Node> keepNodes(Configuration cfg) {
    return nodes.stream().filter(cfg::hasNode).collect(Collectors.toList());
  }

  protected List<VM> keepVMs(Configuration cfg) {
    return vms.stream().filter(cfg::hasVM).collect(Collectors.toList());
  }

  protected List<VM> otherVMs(Configuration cfg) {
    return cfg.getVMs().filter(v -> !vms.contains(v))
        .collect(Collectors.toList());
  }
}

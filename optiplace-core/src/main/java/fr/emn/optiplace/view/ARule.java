package fr.emn.optiplace.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;

/** @author guillaume */
public abstract class ARule implements Rule {

  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(ARule.class);

  /**
   * @param node2s
   * the nodes concerned by this rule
   * @param vM2s
   * the vms concerned by this rule
   */
  public ARule(Set<Node> node2s, Set<VM> vM2s) {
    if (node2s != null) {
      nodes = node2s;
    }
    if (vM2s != null) {
      vms = vM2s;
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

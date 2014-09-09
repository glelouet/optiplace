/**
 *
 */
package fr.emn.optiplace.actions;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import fr.emn.optiplace.configuration.ManagedElement;

/**
 * a graph of actions. This graph should be created by specifying the
 * dependencies between actions.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class ActionGraph {

  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(ActionGraph.class);

  private final HashMap<Action, HashSet<Action>> dependencies = new HashMap<Action, HashSet<Action>>();

  /**
   * add an action, with no dependency
   *
   * @param a
   *            the action to add
   * @return true if the action was not present.
   */
  public boolean add(Action a) {
    if (dependencies.containsKey(a)) {
      return false;
    }
    dependencies.put(a, new LinkedHashSet<Action>());
    return true;
  }

  /**
   * add a dependency between two actions. if one of the actions did not
   * exist, it is added.
   *
   * @param a
   *            the action which depends on the other
   * @param dep
   *            the action to execute first
   * @return true if the dependency did not exist yet
   */
  public boolean add(Action a, Action dep) {
    add(a);
    add(dep);
    return dependencies.get(a).add(dep);
  }

  /**
   * remove an action, so it ensures a following add will return true.
   *
   * @param a
   *            the action
   * @return true if the action was removed, false if not present.
   */
  public boolean remove(Action a) {
    boolean ret = false;
    if (dependencies.remove(a) != null) {
      ret = true;
    }
    for (HashSet<Action> s : dependencies.values()) {
      ret |= s.remove(a);
    }
    return ret;
  }

  /** shortcut to {@link #add(Action)} for several elements */
  public void adds(Action... a) {
    if (a == null) {
      return;
    }
    for (Action aa : a) {
      add(aa);
    }
  }

  /**
   * @return a set of the actions that can be performed as they don't have any
   *         dependency.
   */
  public LinkedHashSet<Action> getFreeActions() {
    LinkedHashSet<Action> ret = new LinkedHashSet<Action>();
    for (Entry<Action, HashSet<Action>> e : dependencies.entrySet()) {
      if (e.getValue().isEmpty()) {
        ret.add(e.getKey());
      }
    }
    return ret;
  }

  /**
   * @return a new set of the actions that can be performed as they don't have
   *         any dependency. they are added in the order they were in the
   *         dependencies.
   */
  public LinkedHashSet<Action> getActionsrelated(ManagedElement... me) {
    LinkedHashSet<Action> ret = new LinkedHashSet<Action>();
    if (me == null || me.length == 0) {
      return ret;
    }
    for (Action a : dependencies.keySet()) {
      for (ManagedElement m : me) {
        if (a.isRelated(m)) {
          ret.add(a);
          break;
        }
      }
    }
    return ret;
  }

  @Override
  public ActionGraph clone() {
    ActionGraph ret = new ActionGraph();
    for (Entry<Action, HashSet<Action>> e : dependencies.entrySet()) {
      ret.dependencies.put(e.getKey(),
          new LinkedHashSet<Action>(e.getValue()));
    }
    return ret;
  }

  /**
   * @return a new linkedhashet of all the actions added, in the order they
   *         were added
   */
  public LinkedHashSet<Action> getAllActions() {
    return new LinkedHashSet<>(dependencies.keySet());
  }
}

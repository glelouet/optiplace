/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package entropy.vjob.builder.plasma;

import java.util.HashSet;
import java.util.Set;

import entropy.configuration.ManagedElement;
import entropy.configuration.ManagedElementSet;

/** A multiset composed of the union of two multiset.
 * @author Fabien Hermenier */
public class MultiSetsUnion<T extends ManagedElement> extends
ComposedMultiSet<T> {

  /** Make a new union.
   * @param h the first multiset
   * @param t the second set */
  public MultiSetsUnion(VJobMultiSet<T> h, VJobMultiSet<T> t) {
    super(h, t);
  }

  /** Make a new composition.
   * @param lbl the label of the multiset
   * @param h the first set
   * @param t the second set */
  public MultiSetsUnion(String lbl, VJobMultiSet<T> h, VJobMultiSet<T> t) {
    super(lbl, h, t);
  }

  @Override
  public String operator() {
    return " + ";
  }

  /** Expand the content of the multiset.
   * @return a new multiset which is the union of the first and the second
   * multiset. */
  @Override
  public Set<ManagedElementSet<T>> expand() {

    Set<ManagedElementSet<T>> res = new HashSet<ManagedElementSet<T>>();
    res.addAll(first().expand());
    res.addAll(second().expand());
    return res;
  }

  @Override
  public int size() {
    return expand().size();
  }
}

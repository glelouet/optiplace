/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.vjob.builder.plasma;

import entropy.configuration.ManagedElement;
import entropy.configuration.ManagedElementSet;

/** A non-solution to handle in less than 10 minutes integers in Plasma. TODO:
 * make a non-shamy solution
 * @author Fabien Hermenier */
public class NumberElement implements VJobElement<ManagedElement> {

  private int nb;

  private String label;

  public NumberElement(String label, int x) {
    this.label = label;
    nb = x;
  }

  public NumberElement(int x) {
    nb = x;
  }

  public int getValue() {
    return nb;
  }

  @Override
  public void setLabel(String id) {
    label = id;
  }

  @Override
  public String getLabel() {
    return label;
  }

  @Override
  public String pretty() {
    if (label != null) {
      return label;
    }
    return Integer.toString(nb);
  }

  @Override
  public String definition() {
    return Integer.toString(nb);
  }

  @Override
  public ManagedElementSet<ManagedElement> getElements() {
    throw new UnsupportedOperationException();
  }
}

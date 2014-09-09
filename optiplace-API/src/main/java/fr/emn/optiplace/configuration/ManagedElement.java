/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package fr.emn.optiplace.configuration;

/** An element of a configuration. It is simply an id associated to a name for
 * representation.<br />
 * Both those values are NOT mutable, so a {@link ManagedElement} is NOT
 * mutable. Since the ID and String are unique among the various elements of a
 * {@link Configuration}, a configuration MUST maintain the bijection ID-STRING,
 * as well as ID-Element<br />
 * Since the Id is mostly used, the equality of two managedElements is by
 * default tested on the ID<br />
 * The toString() method defaults to the name of the element
 * @author Guillaume Le Louët */
public interface ManagedElement {

  /**
   * Get the identifier of the element.
   *
   * @return a String
   */
  String getName();

  /** @return the internal ID */
  long getId();
}

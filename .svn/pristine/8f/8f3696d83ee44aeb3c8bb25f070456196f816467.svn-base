/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.vjob.builder.xml;

import java.util.Iterator;
import java.util.List;

/** @author Fabien Hermenier */
public class Param {

  public static enum Type {
    set, nb, vm, node
  }

  public Type type;

  public Object value;

  public Param(Type t, String ref) {
    type = t;
    value = ref;
  }

  public Param(int val) {
    type = Type.nb;
    value = val;
  }

  public Param() {}

  @Override
  public String toString() {
    if (type != null) {
      switch (type) {
        case set:
          StringBuilder b = new StringBuilder("{");
          @SuppressWarnings("unchecked") List<Object> l = (List<Object>) value;
          for (Iterator<Object> ite = l.iterator(); ite.hasNext();) {
            b.append(ite.next());
            if (ite.hasNext()) {
              b.append(", ");
            }
          }
          b.append("}");
          return b.toString();
        default:
          return value.toString();
      }
    }
    return "null";
  }
}

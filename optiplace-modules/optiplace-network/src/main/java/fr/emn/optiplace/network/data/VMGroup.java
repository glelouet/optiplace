package fr.emn.optiplace.network.data;

import fr.emn.optiplace.configuration.ManagedElement;

public class VMGroup extends ManagedElement {

	public final int use;
	public final int hashcode;

	/**
	 *
	 */
	public VMGroup(String name, int use) {
		super(name);
		this.use = use;
		hashcode = name.toLowerCase().hashCode() + use;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj.getClass() == VMGroup.class) {
			VMGroup g2 = (VMGroup) obj;
			return g2.name.equalsIgnoreCase(name) && g2.use == use;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return hashcode;
	}
}
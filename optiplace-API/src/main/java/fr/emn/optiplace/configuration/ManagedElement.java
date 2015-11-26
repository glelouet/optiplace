
package fr.emn.optiplace.configuration;

import java.util.Comparator;


/**
 * 
 * Managed elements are NOT case sensitive. so VM("V") is the same as VM("v")
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 *
 */
public class ManagedElement {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManagedElement.class);

	public static final Comparator<ManagedElement> CMP_NAME_INC = new Comparator<ManagedElement>() {

		@Override
		public int compare(ManagedElement o1, ManagedElement o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	public static final Comparator<ManagedElement> CMP_NAME_DEC = new Comparator<ManagedElement>() {

		@Override
		public int compare(ManagedElement o1, ManagedElement o2) {
			return o2.getName().compareTo(o1.getName());
		}
	};

	public final String name;

	public ManagedElement(String name) {
		this.name = name;
		if (name == null) {
			throw new NullPointerException("creating an managed element with null name is not allowed");
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.toLowerCase().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!this.getClass().equals(obj.getClass())) {
			return false;
		}
		if (!((ManagedElement) obj).name.toLowerCase().equals(name.toLowerCase())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name;
	}
}

package fr.emn.optiplace.network.data;

public class Link {

	public final String v0, v1;
	public final int hashCode;

	public Link(String v0, String v1) {
		if (v0.compareToIgnoreCase(v1) < 0) {
			String t = v0;
			v0 = v1;
			v1 = t;
		}
		this.v0 = v0;
		this.v1 = v1;
		hashCode = v0.hashCode() + v1.hashCode();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() == this.getClass()) {
			Link o = (Link) obj;
			return o.v0.equals(v0) && o.v1.equals(v1);
		}
		return false;
	}

	@Override
	public String toString() {
		return "link[" + v0 + "-" + v1 + "]";
	}

}
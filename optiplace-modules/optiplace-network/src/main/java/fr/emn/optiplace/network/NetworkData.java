package fr.emn.optiplace.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import fr.emn.optiplace.configuration.ManagedElement;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.configuration.VMHoster;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class NetworkData {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkData.class);

	//////////////////////////////////////////////
	// VM use part
	//////////////////////////////////////////////

	public static class VMGroup extends ManagedElement{

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
			if (this==obj) {
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

	protected HashMap<VM, VMGroup> vm2group = new HashMap<>();

	protected HashMap<String, VMGroup> name2group = new HashMap<>();

	public int use(VM vm1, VM vm2) {
		if (vm1 == null || vm2 == null || vm1.equals(vm2)) {
			return 0;
		}
		VMGroup g = vm2group.get(vm1);
		if (g.equals(vm2group.get(vm2))) {
			return g.use;
		}
		return 0;
	}

	/**
	 * create a group if no group with given name exists
	 *
	 * @param name
	 *          the name of the group
	 * @param use
	 *          the use of the elements of the group
	 * @return existing group if already present, a new group if no group with
	 *         that name present, or null if a group with same name but different
	 *         use is present.
	 */
	public VMGroup addGroup(String name, int use) {
		VMGroup ret = name2group.get(name);
		if (ret == null) {
			ret = new VMGroup(name, use);
			name2group.put(name, ret);
			return ret;
		} else {
			return ret.use == use ? ret : null;
		}
	}

	/**
	 * place VMs in given group
	 *
	 * @param group
	 *          the group to place VM in added if not already present.
	 * @param vms
	 *          the vms to add to the group
	 * @return true if the vms have been added to given group.
	 */
	public boolean addVM(VMGroup group, VM... vms) {
		if (vms == null || vms.length == 0 || group == null) {
			return false;
		}
		VMGroup g2 = addGroup(group.name, group.use);
		if (g2 == null) {
			return false;
		}
		for (VM v : vms) {
			vm2group.put(v, g2);
		}
		return true;
	}

	//////////////////////////////////////////////////
	// Node and extern capacity
	//////////////////////////////////////////////////

	public class Router extends VMHoster {

		public Router(String name) {
			super(name);
		}

	}

	protected HashMap<String, Router> name2router = new HashMap<>();

	public Router addRouter(String name) {
		Router ret = name2router.get(name);
		if (ret != null) {
			return ret;
		}
		ret = new Router(name);
		name2router.put(name, ret);
		return ret;
	}

	public class Link {

		public final VMHoster v0, v1;
		public final int hashCode;

		public Link(VMHoster v0, VMHoster v1) {
			if (v0.getName().compareToIgnoreCase(v1.getName()) < 0) {
				VMHoster t = v0;
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

	}

	protected Set<Link> links = new HashSet<>();

	public Link addLink(VMHoster h1, VMHoster h2, int capa) {
		if (h1 == null || h2 == null || h1.equals(h2)) {
			return null;
		}
		if (h1.getName().compareToIgnoreCase(h2.getName()) < 0) {
			VMHoster t = h1;
			h1 = h2;
			h2 = t;
		}
		int DOIT;
		// TODO
		return null;
	}

}

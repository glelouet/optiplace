/* Copyright (c) Fabien Hermenier This file is part of Entropy. Entropy is free
 * software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version.
 * Entropy is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details. You should have received a copy of the GNU Lesser General Public
 * License along with Entropy. If not, see <http://www.gnu.org/licenses/>. */

package entropy.configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** @author Fabien Hermenier */
public class SimpleNode implements Node, Cloneable {

	private String name;

	private int nbOfCPUs;

	private int memCapacity;

	private int coreCapacity;

	private String ip;

	private String mac;

	private String currentPlatform;

	private Map<String, Map<String, String>> availablePlatforms;

	/**
	 * Make a node without specifying any capacity.
	 * 
	 * @param name
	 *            the identifier of the node
	 */
	public SimpleNode(String name) {
		this(name, 1, 0, 0, null, null);
	}

	/**
	 * Make a node and specify its resource capacity
	 * 
	 * @param name
	 *            the identifier of the node
	 * @param nbOfCPUs
	 *            the number of physical CPUs available to the VMs
	 * @param coreCapa
	 *            the capacity of each physical CPU
	 * @param memoryCapacity
	 *            the memory capacity of each node
	 */
	public SimpleNode(String name, int nbOfCPUs, int coreCapa,
			int memoryCapacity) {
		this(name, nbOfCPUs, coreCapa, memoryCapacity, null, null);
	}

	/**
	 * Make a node and specify its resource capacity
	 * 
	 * @param name
	 *            the identifier of the node
	 * @param nbOfCPUs
	 *            the number of physical CPUs available to the VMs
	 * @param coreCapa
	 *            the capacity of each physical CPU
	 * @param memoryCapacity
	 *            the memory capacity of each node
	 * @param ip
	 *            the IP address of the node
	 * @param mac
	 *            the MAC address of the node
	 */
	public SimpleNode(String name, int nbOfCPUs, int coreCapa,
			int memoryCapacity, String ip, String mac) {
		this.name = name;
		this.nbOfCPUs = nbOfCPUs;
		memCapacity = memoryCapacity;
		this.coreCapacity = coreCapa;
		this.ip = ip;
		this.mac = mac;

		availablePlatforms = new HashMap<String, Map<String, String>>();
		currentPlatform = null;
	}

	/**
	 * Make a node and specify its resource capacity
	 * 
	 * @param name
	 *            the identifier of the node
	 * @param physicalName
	 *            the identifier of the physical node (if they are different
	 *            node == virtual node)
	 * @param nbOfCPUs
	 *            the number of physical CPUs available to the VMs
	 * @param coreCapacity
	 *            the capacity of each physical CPU
	 * @param memoryCapacity
	 *            the memory capacity of each node
	 */
	public SimpleNode(String name, String physicalName, int nbCPU,
			int coreCapa, int capaMem) {
		this.name = name;
		nbOfCPUs = nbCPU;
		memCapacity = capaMem;
		coreCapacity = coreCapa;
		ip = null;
		mac = null;
	}

	@Override
	public void rename(String n) {
		name = n;
	}

	@Override
	public void setNbOfCores(int nb) {
		nbOfCPUs = nb;
	}

	@Override
	public int getNbOfCores() {
		return nbOfCPUs;
	}

	@Override
	public int getCoreCapacity() {
		return coreCapacity;
	}

	@Override
	public int getCPUCapacity() {
		return getCoreCapacity() * getNbOfCores();
	}

	@Override
	public int getMemoryCapacity() {
		return memCapacity;
	}

	@Override
	public void setCoreCapacity(int c) {
		coreCapacity = c;
	}

	@Override
	public void setMemoryCapacity(int m) {
		memCapacity = m;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || !(o instanceof Node)) {
			return false;
		}
		Node that = (Node) o;
		return name.equals(that.getName());
	}

	/**
	 * Return the hash code of the node name
	 * 
	 * @return {@code getName().hashCode();}
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public Node clone() {
		SimpleNode clone = new SimpleNode(name, nbOfCPUs, coreCapacity,
				memCapacity);
		clone.ip = ip;
		clone.mac = mac;
		clone.currentPlatform = currentPlatform;
		for (String p : availablePlatforms.keySet()) {
			Map<String, String> cpy = new HashMap<String, String>();
			for (Map.Entry<String, String> op : availablePlatforms.get(p)
					.entrySet()) {
				cpy.put(op.getKey(), op.getValue());
			}
			clone.availablePlatforms.put(p, cpy);
		}
		return clone;
	}

	@Override
	public String getMACAddress() {
		return mac;
	}

	@Override
	public void setMACAddress(String address) {
		mac = address;
	}

	@Override
	public String getIPAddress() {
		return ip;
	}

	@Override
	public void setIPAddress(String ip) {
		this.ip = ip;
	}

	@Override
	public String getCurrentPlatform() {
		return currentPlatform;
	}

	@Override
	public boolean setCurrentPlatform(String k) {
		if (availablePlatforms.containsKey(k)) {
			currentPlatform = k;
			return true;
		}
		return false;
	}

	@Override
	public boolean addPlatform(String id) {
		if (!availablePlatforms.containsKey(id)) {
			addPlatform(id, new HashMap<String, String>());
			return true;
		}
		return false;
	}

	@Override
	public void addPlatform(String id, Map<String, String> options) {
		availablePlatforms.put(id, options);
		if (currentPlatform == null) {
			currentPlatform = id;
		}
	}

	@Override
	public Set<String> getAvailablePlatforms() {
		return availablePlatforms.keySet();
	}

	@Override
	public Map<String, String> getPlatformOptions(String p) {
		return availablePlatforms.get(p);
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(name);
		b.append("[nbCpus=").append(nbOfCPUs);
		b.append(", cpu=").append(getCPUCapacity());
		b.append(", mem=").append(memCapacity);
		if (currentPlatform != null) {
			b.append(", platform=").append(currentPlatform);
		}
		return b.append("]").toString();
	}

	boolean powerswitchable = false;

	@Override
	public boolean isPowerSwitchable() {
		return powerswitchable;
	}

	@Override
	public void setPowerSwitchable(boolean powerswitchable) {
		this.powerswitchable = powerswitchable;
	}
}

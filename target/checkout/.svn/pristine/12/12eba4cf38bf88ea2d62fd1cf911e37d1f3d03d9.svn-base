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

import java.util.Map;
import java.util.Set;

/**
 * Interface to specify a working node. A node has a certain CPU and memory
 * capacity that can be used to run VMs.<br />
 * The nodes are modifiable to reduce the number of interfaces required.
 * 
 * @author Fabien Hermenier
 */
public interface Node extends ManagedElement {

	/**
	 * Return the number of cores dedicated for Virtual Machines.
	 * 
	 * @return a positive integer
	 */
	int getNbOfCores();

	/**
	 * Return the capacity of each CPU core.
	 * 
	 * @return a positive integer
	 */
	int getCoreCapacity();

	/** @return the total capacity of the CPU, ie #cores * coreCapacity */
	int getCPUCapacity();
	/**
	 * Return the amount of memory dedicated for Virtual Machines.
	 * 
	 * @return a positive amount
	 */
	int getMemoryCapacity();

	/**
	 * Set the number of physical CPU cores available for the virtual machines.
	 * 
	 * @param nb
	 *            a positive integer
	 */
	void setNbOfCores(int nb);

	/**
	 * Set the CPU capacity of each CPU core of the node available to the
	 * virtual machines
	 * 
	 * @param c
	 *            a positvie integer
	 */
	void setCoreCapacity(int c);

	/**
	 * Set the memory capacity of the node, available to the virtual machines
	 * 
	 * @param m
	 *            a positive integer
	 */
	void setMemoryCapacity(int m);

	/**
	 * Deep copy of the node. All the parametersare copied
	 * 
	 * @return a copy of the node
	 */
	Node clone();

	/**
	 * Get the MAC Address of this node.
	 * 
	 * @return a MAC address that may be null
	 */
	String getMACAddress();

	/**
	 * Get the IP address of this node.
	 * 
	 * @return an IP address that may be null
	 */
	String getIPAddress();

	/**
	 * Get the current platform deployed on the node.
	 * 
	 * @return the identifier of the currently deployed platform if any.
	 *         {@code null} otherwise
	 */
	String getCurrentPlatform();

	/**
	 * Set the current platform on this node. The platform must already be
	 * available for this node
	 * 
	 * @param k
	 *            the identifier of the platform
	 * @return {@code true} if the platform has been set, {@code false} if the
	 *         platform is not available
	 */
	boolean setCurrentPlatform(String k);

	/**
	 * Add a possible hosting platform for this node with no platform specific
	 * options. If it is the first added platform, it becomes the current
	 * hosting platform If a platform with a same identifier is already in,
	 * there is no addition
	 * 
	 * @param id
	 *            the identifier of the platform
	 * @return {@code true} if a platform was added
	 */
	boolean addPlatform(String id);

	/**
	 * Add a possible hosting platform for this node with given options If it is
	 * the first added platform, it becomes the current hosting platform If a
	 * platform with a same identifier is already in, it is overriden
	 * 
	 * @param id
	 *            the identifier of the platform
	 * @param options
	 *            the options for the platform
	 */
	void addPlatform(String id, Map<String, String> options);

	/**
	 * Get the available platform for this node.
	 * 
	 * @return a set of platform identifier. May be empty
	 */
	Set<String> getAvailablePlatforms();

	/**
	 * Set the IP address of this node.
	 * 
	 * @param ip
	 *            the IP address
	 */
	void setIPAddress(String ip);

	/**
	 * set the MAC Address of this node.
	 * 
	 * @param address
	 *            a MAC address that may be null
	 */
	void setMACAddress(String address);

	/**
	 * Get the options associated to a platform
	 * 
	 * @param p
	 *            the platform identifier
	 * @return a map of option if the platform is available on this node.
	 *         {@code null} otherwise
	 */
	Map<String, String> getPlatformOptions(String p);

	/**
	 * set wether the server can be powered on or off. True means it can be
	 * switched on, then off.
	 * 
	 * @return the presence of an ability to switch the server power on or off.
	 */
	boolean isPowerSwitchable();

	/**
	 * set the capacity of the server to be shut down then restarted.
	 * 
	 * @param powerswitchable
	 *            the {@link #isPowerSwitchable()} valu to set
	 */
	void setPowerSwitchable(boolean powerswitchable);
}

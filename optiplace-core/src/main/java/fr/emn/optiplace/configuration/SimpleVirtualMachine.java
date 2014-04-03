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

/**
 * Simple implementation of VirtualMachine.
 * 
 * @author Fabien Hermenier
 */
public class SimpleVirtualMachine implements VirtualMachine, Cloneable {

	private String name;

	private int nbOfCPUs = 1;

	private int cpuConsumption = 0;

	private int memConsumption = 0;

	private int cpuDemand = 0;

	private int memDemand = 0;

	private int maxCPU = Integer.MAX_VALUE;

	private String template;

	private String platform;

	/**
	 * New virtual machine with no needs.
	 * 
	 * @param name
	 *            the name of the virtual machine
	 */
	public SimpleVirtualMachine(String name) {
		this(name, 1, 0, 0, 0, 0);
	}

	/**
	 * New virtual machine with a given resource consumption. The current
	 * resource demand is set to the resource consumption.
	 * 
	 * @param name
	 *            the name of the virtual machine
	 * @param nbOfCPUs
	 *            the number of virtual CPUs allocated to the virtual machine
	 * @param cpuConsumption
	 *            the current cpu consumption of the virtual machine
	 * @param memoryConsumption
	 *            the current memory consumption of the virtual machine
	 */
	public SimpleVirtualMachine(String name, int nbOfCPUs, int cpuConsumption,
			int memoryConsumption) {
		this(name, nbOfCPUs, cpuConsumption, memoryConsumption, cpuConsumption,
				memoryConsumption);
	}

	/**
	 * New virtual machine with a given resource consumption and demand
	 * 
	 * @param name
	 *            the name of the virtual machine
	 * @param nbOfCPUs
	 *            the number of virtual CPUs allocated to the virtual machine
	 * @param cpuConsumption
	 *            the current cpu consumption of the virtual machine
	 * @param memoryConsumption
	 *            the current memory consumption of the virtual machine
	 * @param cpuDemand
	 *            the current cpu demand of the virtual machine
	 * @param memoryDemand
	 *            the current memory demand of the virtual machine
	 */
	public SimpleVirtualMachine(String name, int nbOfCPUs, int cpuConsumption,
			int memoryConsumption, int cpuDemand, int memoryDemand) {
		this.name = name;
		this.cpuConsumption = cpuConsumption;
		this.nbOfCPUs = nbOfCPUs;
		memConsumption = memoryConsumption;
		this.cpuDemand = cpuDemand;
		memDemand = memoryDemand;
	}

	@Override
	public void rename(String n) {
		name = n;
	}

	@Override
	public int getNbOfCPUs() {
		return nbOfCPUs;
	}

	@Override
	public void setNbOfCPUs(int nb) {
		nbOfCPUs = nb;
	}

	@Override
	public int getCPUConsumption() {
		return cpuConsumption;
	}

	@Override
	public int getMemoryConsumption() {
		return memConsumption;
	}

	@Override
	public int getMemoryDemand() {
		return memDemand;
	}

	@Override
	public int getCPUDemand() {
		return cpuDemand;
	}

	@Override
	public void setCPUMax(int nb) {
		maxCPU = nb;
	}

	@Override
	public int getCPUMax() {
		return maxCPU;
	}

	@Override
	public void setCPUConsumption(int c) {
		cpuConsumption = c;
	}

	@Override
	public void setMemoryConsumption(int m) {
		memConsumption = m;
	}

	@Override
	public void setCPUDemand(int c) {
		cpuDemand = c;
	}

	@Override
	public void setMemoryDemand(int m) {
		memDemand = m;
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
		if (o == null || !(o instanceof VirtualMachine)) {
			return false;
		}

		VirtualMachine that = (VirtualMachine) o;
		return name.equals(that.getName());
	}

	/**
	 * Return the hashcode of the virtual machine name
	 * 
	 * @return {@code getName().hashCode();}
	 */
	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public VirtualMachine clone() {
		return clone(name);
	}

	@Override
	public VirtualMachine clone(String newname) {
		SimpleVirtualMachine vm = new SimpleVirtualMachine(newname, nbOfCPUs,
				cpuConsumption, memConsumption, cpuDemand, memDemand);
		vm.maxCPU = maxCPU;
		vm.template = template;
		vm.platform = platform;
		vm.migrable = migrable;
		return vm;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(name);
		b.append("[nbCpus=").append(nbOfCPUs);
		b.append(", cpu#cons=").append(cpuConsumption);
		b.append(", mem#cons=").append(memConsumption);
		b.append(", cpu#req=").append(cpuDemand);
		b.append(", mem#req=").append(memDemand);
		if (maxCPU >= 0) {
			b.append(", cpu#max=").append(maxCPU);
		}
		if (template != null) {
			b.append(", template=").append(template);
		}
		if (platform != null) {
			b.append(", platform=").append(platform);
		}
		return b.append("]").toString();
	}

	@Override
	public String getTemplate() {
		return template;
	}

	@Override
	public void setTemplate(String tpl) {
		template = tpl;
	}

	@Override
	public String getHostingPlatform() {
		return platform;
	}

	@Override
	public void setHostingPlatform(String p) {
		platform = p;
	}

	boolean migrable = true;

	@Override
	public boolean isMigrable() {
		return migrable;
	}

	@Override
	public void setMigrable(boolean isMigrable) {
		migrable = isMigrable;
	}
}

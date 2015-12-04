/* Copyright (c) 2010 Ecole des Mines de Nantes. This file is part of Entropy.
 * Entropy is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. Entropy is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details. You should have received a copy of the GNU Lesser General
 * Public License along with Entropy. If not, see
 * <http://www.gnu.org/licenses/>. */

package entropy.execution;

import java.util.Set;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import entropy.view.scheduling.Dependencies;
import entropy.view.scheduling.TimedExecutionGraph;
import entropy.view.scheduling.action.Instantiate;
import entropy.view.scheduling.action.Migration;
import entropy.view.scheduling.action.Run;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;

/**
 * Unit tests for TimedExecutionGraph.
 * 
 * @author Fabien Hermenier
 */
@Test(groups = {"unit"})
public class TestTimedExecutionGraph {

	/** generates nb Node mocks with specified configuration */
	public static Node[] makeNodes(int nb, String prefix, int nbOfCPUs,
			int cpuCapacity, int memoryCapacity) {
		Node[] ret = new Node[nb];
		for (int i = 0; i < nb; i++) {
			ret[i] = makeNode(prefix + i, nbOfCPUs, cpuCapacity, memoryCapacity);
		}
		return ret;
	}

	/** generates nb Node mocks with specified configuration */
	public static Node makeNode(String name, int nbOfCPUs, int cpuCapacity,
			int memoryCapacity) {
		Node n = Mockito.mock(Node.class);
		Mockito.when(n.getName()).thenReturn(name);
		Mockito.when(n.getNbOfCores()).thenReturn(nbOfCPUs);
		Mockito.when(n.getCoreCapacity()).thenReturn(cpuCapacity);
		Mockito.when(n.getMemoryCapacity()).thenReturn(memoryCapacity);
		return n;
	}

	/** generates nb {@link VirtualMachine} mocks with specified configuration */
	public static VirtualMachine[] makeVMs(int nb, String prefix, int nbOfCPUs,
			int cpuConsumption, int memoryConsumption) {
		VirtualMachine[] ret = new VirtualMachine[nb];
		for (int i = 0; i < nb; i++) {
			ret[i] = makeVM(prefix + i, nbOfCPUs, cpuConsumption,
					memoryConsumption);
		}
		return ret;
	}

	/** generates a {@link VirtualMachine} mocks with specified configuration */
	public static VirtualMachine makeVM(String name, int nbOfCPUs,
			int cpuConsumption, int memoryConsumption) {
		VirtualMachine vm = Mockito.mock(VirtualMachine.class);
		Mockito.when(vm.getName()).thenReturn(name);
		Mockito.when(vm.getNbOfCPUs()).thenReturn(nbOfCPUs);
		Mockito.when(vm.getCPUConsumption()).thenReturn(cpuConsumption);
		Mockito.when(vm.getMemoryConsumption()).thenReturn(memoryConsumption);
		return vm;
	}

	/** Test the extraction of dependencies. */
	public void testExtractDependencies() {
		TimedExecutionGraph g = new TimedExecutionGraph();
		VirtualMachine[] vms = makeVMs(6, "VM", 1, 1, 1);
		Node[] ns = makeNodes(5, "N", 1000, 1000, 1000);
		Migration m1 = new Migration(vms[0], ns[0], ns[2], 0, 10);
		Migration m2 = new Migration(vms[1], ns[1], ns[2], 0, 5);
		Migration m3 = new Migration(vms[2], ns[0], ns[1], 7, 9);
		Migration m4 = new Migration(vms[3], ns[1], ns[3], 3, 7);
		Migration m5 = new Migration(vms[4], ns[3], ns[2], 0, 3);
		Run r6 = new Run(vms[5], ns[3], 0, 5);
		m1.insertIntoGraph(g);
		m2.insertIntoGraph(g);
		m3.insertIntoGraph(g);
		m4.insertIntoGraph(g);
		m5.insertIntoGraph(g);
		r6.insertIntoGraph(g);
		Set<Dependencies> deps = g.extractDependencies();
		Dependencies d1 = new Dependencies(m4);
		d1.addDependency(m5);
		Dependencies d2 = new Dependencies(m3);
		d2.addDependency(m2);
		d2.addDependency(m4);
		Assert.assertTrue(deps.contains(d1));
		Assert.assertTrue(deps.contains(d2));
		Assert.assertEquals(deps.size(), 6);
	}

	public void testWithForge() {
		TimedExecutionGraph executionGraph = new TimedExecutionGraph();
		VirtualMachine vm = makeVM("VM1", 1, 1, 1);
		Instantiate instanciate = new Instantiate(vm, 0, 5);
		Run run = new Run(vm, makeNode("N1", 1, 2, 3), 10, 12);
		instanciate.insertIntoGraph(executionGraph);
		run.insertIntoGraph(executionGraph);
		Set<Dependencies> deps = executionGraph.extractDependencies();
		Dependencies dependencies = new Dependencies(run);
		dependencies.addDependency(instanciate);
		Assert.assertTrue(deps.contains(dependencies), "" + deps
				+ " should contain " + dependencies);
	}

	public void testToEventAgenda() {
		TimedExecutionGraph g = new TimedExecutionGraph();
		VirtualMachine[] vms = makeVMs(6, "VM", 1, 1, 1);
		Node[] ns = makeNodes(5, "N", 1000, 1000, 1000);
		Migration m3 = new Migration(vms[2], ns[0], ns[1], 7, 9);
		Migration m1 = new Migration(vms[0], ns[0], ns[2], 0, 10);
		Migration m2 = new Migration(vms[1], ns[1], ns[2], 0, 5);
		Migration m4 = new Migration(vms[3], ns[1], ns[3], 3, 7);
		Migration m5 = new Migration(vms[4], ns[3], ns[2], 0, 3);
		Run r6 = new Run(vms[5], ns[3], 0, 5);
		m1.insertIntoGraph(g);
		m2.insertIntoGraph(g);
		m3.insertIntoGraph(g);
		m4.insertIntoGraph(g);
		m5.insertIntoGraph(g);
		r6.insertIntoGraph(g);
		Assert.assertNotNull(g.toEventAgenda());
	}

}

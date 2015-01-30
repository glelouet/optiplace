/**
 *
 */
package fr.emn.optiplace.solver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.center.configuration.ManagedElement;
import fr.emn.optiplace.center.configuration.Node;
import fr.emn.optiplace.center.configuration.VM;
import fr.emn.optiplace.center.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2015
 *
 */
public class ProblemStatisticsTest {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProblemStatisticsTest.class);

    @Test
    public void test() {

	IReconfigurationProblem pb = Mockito.mock(IReconfigurationProblem.class);
	ProblemStatistics ps = new ProblemStatistics(pb);
	Map<Node, Integer> capacities = new HashMap<Node, Integer>();

	Node[] nodes = new Node[10];
	for (int i = 0; i < nodes.length; i++) {
	    nodes[i] = new Node("n" + i);
	    capacities.put(nodes[i], i % 2 * 10);
	}

	Mockito.when(pb.nodes()).thenReturn(nodes);
	ResourceSpecification rs = new ResourceSpecification() {

	    @Override
	    public void readLine(String line) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public Map<VM, Integer> toUses() {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public Map<Node, Integer> toCapacities() {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public int getUse(VM vm) {
		throw new UnsupportedOperationException();
	    }

	    @Override
	    public String getType() {
		return "testtype";
	    }

	    @Override
	    public int getCapacity(Node n) {
		return capacities.get(n);
	    }
	};

	List<Node> expected = Arrays.asList(nodes[9], nodes[7], nodes[5], nodes[3], nodes[1], nodes[8], nodes[6],
		nodes[4], nodes[2], nodes[0]);
	ArrayList<Node> res = ps.sortNodes(rs.makeNodeComparator(false), ManagedElement.CMP_NAME_DEC);
	Assert.assertEquals(res, expected, "res=" + res + " ; expected=" + expected);

	Assert.assertTrue(res == ps.sortNodes(rs.makeNodeComparator(false), ManagedElement.CMP_NAME_DEC),
		"bad caching !");
    }
}
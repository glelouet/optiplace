/**
 *
 */
package fr.emn.optiplace.ha;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.ha.rules.Among;
import fr.emn.optiplace.ha.rules.Ban;
import fr.emn.optiplace.ha.rules.Capacity;
import fr.emn.optiplace.ha.rules.Fence;
import fr.emn.optiplace.ha.rules.Greedy;
import fr.emn.optiplace.ha.rules.Lazy;
import fr.emn.optiplace.ha.rules.LoadInc;
import fr.emn.optiplace.ha.rules.Quarantine;
import fr.emn.optiplace.ha.rules.Root;
import fr.emn.optiplace.ha.rules.Split;
import fr.emn.optiplace.ha.rules.Spread;
import fr.emn.optiplace.view.Rule;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class HADataTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(HADataTest.class);

	@Test
	public void testCorrectParser() {
		List<String> desc = new ArrayList<>();
		List<Rule> parsed = new ArrayList<>();
		String s;

		s = "among[vm1, vm2][[n1], [n2, n3]]";
		parsed.add(Among.parse(s));
		desc.add(s);

		s = "ban[vm1, vm2][n2, n3, n4]";
		parsed.add(Ban.parse(s));
		desc.add(s);

		s = "capacity[n2, n3, n4](5)";
		parsed.add(Capacity.parse(s));
		desc.add(s);

		s = "fence[vm1, vm2][n2, n3, n4]";
		parsed.add(Fence.parse(s));
		desc.add(s);

		s = "lazy[n1, n2](lol)(3)";
		parsed.add(Lazy.parse(s));
		desc.add(s);

		s = "greedy[vm1, vm2](15)(CPU)";
		parsed.add(Greedy.parse(s));
		desc.add(s);

		s = "ordnodesload[n2, n3, n4](lol)";
		parsed.add(LoadInc.parse(s));
		desc.add(s);

		s = "quarantine[vm1, vm2][n2, n3, n4]";
		parsed.add(Quarantine.parse(s));
		desc.add(s);

		s = "root[vm1, vm2][n2, n3, n4]";
		parsed.add(Root.parse(s));
		desc.add(s);

		s = "split[vm1, vm2][n2, n3, n4]";
		parsed.add(Split.parse(s));
		desc.add(s);

		s = "spread[vm1, vm2][n2, n3, n4]";
		parsed.add(Spread.parse(s));
		desc.add(s);

		HAData test = new HAData();
		for (String line : desc) {
			test.readLine(line);
		}

		Assert.assertEquals(test.getRules(), parsed);
		HAData created = new HAData();
		created.getRules().addAll(parsed);
		Assert.assertEquals(created, test);
	}
}

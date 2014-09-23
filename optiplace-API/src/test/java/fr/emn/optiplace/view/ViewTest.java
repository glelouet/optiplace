/**
 *
 */
package fr.emn.optiplace.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Stream;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.view.fakes.ConfView;
import fr.emn.optiplace.view.fakes.DepView;
import fr.emn.optiplace.view.fakes.HollowView;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class ViewTest {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ViewTest.class);

	@Test
	public void testDependencies() {
		HollowView v1 = new HollowView();
		DepView v2 = new DepView();
		HashMap<String, View> m = new HashMap<>();
		m.put(v1.getClass().getName(), v1);
		m.put(v2.getClass().getName(), v2);
		v2.setDependencies(m);
		Assert.assertEquals(v2.dep, v1);
		Assert.assertEquals(v2.dep2, null);
	}

	@Test
	public void testConfiguration() {
		String data = "data line";
		ProvidedData pd = Mockito.mock(ProvidedData.class);
		Mockito.when(pd.lines()).thenReturn(Stream.of(data));
		ViewDataProvider vdp = Mockito.mock(ViewDataProvider.class);
		Mockito.when(vdp.getData("conf1")).thenReturn(pd);
		ConfView cv = new ConfView();
		cv.setConfs(vdp);
		Assert.assertEquals(cv.conf.line, data);
	}

	@Test
	public void testExtractDependencies() {
		View v = new DepView();
		Assert.assertEquals(v.extractDependencies(),
				Collections.singleton("fr.emn.optiplace.view.fakes.HollowView"));
	}

	@Test
	public void testExtractConfigurations() {
		View v = new ConfView();
		Assert.assertEquals(v.extractConfigurations(true),
				Collections.singleton("conf1"));
		Assert.assertEquals(v.extractConfigurations(false),
				Collections.singleton("conf2"));
		Assert.assertEquals(v.extractConfigurations(null),
				new HashSet<>(Arrays.asList("conf1", "conf2")));
	}
}

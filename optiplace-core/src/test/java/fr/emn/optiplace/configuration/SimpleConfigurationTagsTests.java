package fr.emn.optiplace.configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class SimpleConfigurationTagsTests {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SimpleConfigurationTagsTests.class);

	@Test
	public void testTagVM() {
		SimpleConfiguration cfg = new SimpleConfiguration();
		VM v1 = cfg.addVM("v1", null);
		VM v2 = cfg.addVM("v2", null);
		VM v3 = cfg.addVM("v3", null);

		// no VM tagged
		Assert.assertEquals(cfg.getAllTags().count(), 0l);
		Assert.assertEquals(cfg.getTags(v1).count(), 0l);
		Assert.assertEquals(cfg.getTags(v2).count(), 0l);
		Assert.assertEquals(cfg.getTags(v3).count(), 0l);

		// 2 VMS tagged
		String tag1 = "tag1";
		cfg.tag(v2, tag1);
		cfg.tag(v3, tag1);
		Assert.assertEquals(cfg.getAllTags().count(), 1l);
		Assert.assertEquals(cfg.getTags(v1).count(), 0l);
		Assert.assertEquals(cfg.getTags(v2).count(), 1l);
		Assert.assertEquals(cfg.getTags(v3).count(), 1l);
		Assert.assertFalse(cfg.isTagged(v1, tag1));
		Assert.assertTrue(cfg.isTagged(v2, tag1));
		Assert.assertTrue(cfg.isTagged(v3, tag1));
		Assert.assertEquals(cfg.getVmsTagged(tag1).collect(Collectors.toList()), Arrays.asList(v2, v3));

		// remove one tag
		cfg.delTag(v2, tag1);
		Assert.assertEquals(cfg.getAllTags().count(), 1l);
		Assert.assertEquals(cfg.getTags(v1).count(), 0l);
		Assert.assertEquals(cfg.getTags(v2).count(), 0l);
		Assert.assertEquals(cfg.getTags(v3).count(), 1l);
		Assert.assertFalse(cfg.isTagged(v1, tag1));
		Assert.assertFalse(cfg.isTagged(v2, tag1));
		Assert.assertTrue(cfg.isTagged(v3, tag1));
	}

}

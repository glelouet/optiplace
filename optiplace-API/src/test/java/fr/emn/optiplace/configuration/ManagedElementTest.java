/**
 *
 */
package fr.emn.optiplace.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.center.configuration.ManagedElement;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2015
 *
 */
public class ManagedElementTest {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ManagedElementTest.class);

    @Test
    public void testComparatorNameInc() {
	ManagedElement m0 = new ManagedElement("m0"), m1 = new ManagedElement("m1"), m2 = new ManagedElement("m2");
	List<ManagedElement> test = Arrays.asList(m0, m2, m1);
	Collections.sort(test, ManagedElement.CMP_NAME_INC);
	Assert.assertEquals(test, Arrays.asList(m0, m1, m2));
    }

    @Test
    public void testComparatorNameDec() {
	ManagedElement m0 = new ManagedElement("m0"), m1 = new ManagedElement("m1"), m2 = new ManagedElement("m2");
	List<ManagedElement> test = Arrays.asList(m0, m2, m1);
	Collections.sort(test, ManagedElement.CMP_NAME_DEC);
	Assert.assertEquals(test, Arrays.asList(m2, m1, m0));
    }
}

/**
 *
 */
package fr.emn.optiplace.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import fr.emn.optiplace.view.View;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2015
 *
 */
public class ViewManagerTest {

    @SuppressWarnings("unused")
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ViewManagerTest.class);

    @Test
    public void testSortListOfViews() {
	ArrayList<View> l = new ArrayList<>();
	View v1 = Mockito.mock(View.class);
	Mockito.when(v1.getName()).thenReturn("v1");
	View v2 = Mockito.mock(View.class);
	Mockito.when(v2.getName()).thenReturn("v2");
	View v3 = Mockito.mock(View.class);
	Mockito.when(v3.getName()).thenReturn("v3");
	l.add(v1);
	l.add(v3);
	l.add(v2);

	List<View> target = Arrays.asList(v1, v2, v3);
	ViewManager.sortListOfViews(l);

	Assert.assertEquals(l, target);
    }
}

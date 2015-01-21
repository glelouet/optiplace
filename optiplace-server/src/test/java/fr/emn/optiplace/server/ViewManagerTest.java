/**
 *
 */
package fr.emn.optiplace.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    /**
     *
     * @return a new Array of three views with names "v1", "v2", "v3"
     */
    public View[] makeViewsWithName(int num) {
	View[] ret = new View[num];
	for (int i = 0; i < num; i++) {
	    ret[i] = Mockito.mock(View.class);
	    Mockito.when(ret[i].getName()).thenReturn("v" + i);
	    Mockito.when(ret[i].toString()).thenReturn("v" + i);
	}
	return ret;
    }

    @Test
    public void testSortViewsByName() {
	View[] views = makeViewsWithName(3);
	ArrayList<View> l = new ArrayList<>();
	l.add(views[2]);
	l.add(views[0]);
	l.add(views[1]);

	ViewManager.sortViewsByName(l);

	List<View> target = Arrays.asList(views);
	Assert.assertEquals(l, target);
    }

    @Test
    public void testSortViewsByContainer() {
	View[] views = makeViewsWithName(4);

	// copy it now or the sort will change the order of elems in views
	List<View> target = new ArrayList<>();
	target.add(views[0]);
	target.add(views[3]);
	target.add(views[1]);
	target.add(views[2]);

	List<View> l = Arrays.asList(views);
	HashSet<String> contained = new HashSet<>();
	contained.add("v1");
	contained.add("v2");

	ViewManager.sortViewsByContained(l, contained);
	Assert.assertEquals(l, target, "res = " + l + " expected " + target);

    }
}

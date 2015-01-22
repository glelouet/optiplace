/**
 *
 */
package fr.emn.optiplace.server;

import java.util.ArrayList;
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
    public void testSortViewsByNameExcluding() {
	View[] views = makeViewsWithName(5);

	List<View> l = new ArrayList<>();
	l.add(views[1]);
	l.add(views[3]);
	l.add(views[4]);
	l.add(views[0]);
	l.add(views[2]);

	HashSet<String> contained = new HashSet<>();
	contained.add("v2");
	contained.add("v1");

	// copy it now or the sort will change the order of elems in views
	List<View> target = new ArrayList<>();
	target.add(views[0]);
	target.add(views[3]);
	target.add(views[4]);
	target.add(views[2]);
	target.add(views[1]);

	ViewManager.sortViewsByNameExcluding(l, contained);
	Assert.assertEquals(l, target, "res = " + l + " expected " + target);

    }
}

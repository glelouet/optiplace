package fr.emn.optiplace.eval;

import java.util.stream.Stream;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.view.View;


/**
 * streams {@link View}s from a {@link Configuration}
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 * @param <T>
 *          the type of the view
 */
public interface ViewStreamer<T extends View> {

	/**
	 * stream the possible views to associate to a Configuration, within limits.
	 * The stream makes it possible to lazy instantiate the views, reducing the
	 * memory overhead.
	 *
	 * @param c
	 *          the configuration to explore the views on
	 * @return a new stream of views related to the configuration.
	 */
	public Stream<T> explore(IConfiguration c);


}

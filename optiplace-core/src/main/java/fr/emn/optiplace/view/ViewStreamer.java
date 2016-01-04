package fr.emn.optiplace.view;

import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fr.emn.optiplace.configuration.Configuration;

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
	 * @param v
	 *          the configuration to explore the views on
	 * @return a new stream of views related to the configuration.
	 */
	public Stream<T> explore(Configuration v);

	/**
	 * abstract implementation of {@link ViewStreamer} using {@link Spliterator}.
	 * Need to implements
	 * {@link Spliterator#tryAdvance(java.util.function.Consumer)}
	 * 
	 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
	 *
	 * @param <T>
	 *          the view type to stream on.
	 */
	public static abstract class Split<T extends View> implements java.util.Spliterator<T>, ViewStreamer<T> {

		@Override
		public Stream<T> explore(Configuration v) {
			return StreamSupport.stream(this, false);
		}

		@Override
		public int characteristics() {
			return DISTINCT | NONNULL | IMMUTABLE | CONCURRENT;
		}

		@Override
		public Spliterator<T> trySplit() {
			return null;
		}

		@Override
		public long estimateSize() {
			return 0;
		}

	}

}

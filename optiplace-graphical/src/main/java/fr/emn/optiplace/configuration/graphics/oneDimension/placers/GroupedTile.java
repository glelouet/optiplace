/**
 *
 */
package fr.emn.optiplace.configuration.graphics.oneDimension.placers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.emn.optiplace.configuration.graphics.Tile;

/**
 * A Tile containing other tiles
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class GroupedTile<T> extends Tile<List<Tile<T>>> {

	/**
	 * @param id
	 */
	@SafeVarargs
	public GroupedTile(Tile<T>... elems) {
		super(elems == null ? Collections.emptyList() : Arrays.asList(elems));
	}

	public GroupedTile(Collection<Tile<T>> elems) {
		super(new ArrayList<>(elems != null ? elems : Collections.emptyList()));
	}

	public void deduceArea() {
		this.a = id.stream().mapToInt(t -> t.a).sum();
	}

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(GroupedTile.class);
}

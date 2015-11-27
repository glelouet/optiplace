/**
 *
 */
package fr.emn.optiplace.configuration.graphics.oneDimension;

import java.util.List;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.graphics.Tile;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public interface NodePlacer {

	public List<Tile<Node>> place(IConfiguration cfg);
}

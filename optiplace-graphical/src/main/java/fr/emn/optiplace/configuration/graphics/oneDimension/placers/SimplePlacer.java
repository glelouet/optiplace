/**
 *
 */
package fr.emn.optiplace.configuration.graphics.oneDimension.placers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.graphics.Tile;
import fr.emn.optiplace.configuration.graphics.oneDimension.NodePlacer;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class SimplePlacer implements NodePlacer {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SimplePlacer.class);

	@Override
	public List<Tile<Node>> place(IConfiguration cfg) {
		ResourceSpecification mem = cfg.resources().get("MEM");
		List<Tile<Node>> tiles = cfg.getNodes().map(n -> {
			Tile<Node> t = new Tile<>(n);
			t.a = mem.getCapacity(n);
			return t;
		}).collect(Collectors.toList());
		// int totalArea = tiles.stream().mapToInt(t -> t.a).sum();
		regroup(tiles);
		throw new UnsupportedOperationException();
	}

	/**
	 * Regroup the tiles by area decreasing, then sort them by Node name
	 * decreasing
	 *
	 * @param tiles
	 * @return
	 */
	public static ArrayList<GroupedTile<Node>> regroup(List<Tile<Node>> tiles) {
		Map<Integer, ArrayList<Tile<Node>>> byArea = new HashMap<>();
		tiles.forEach(t->{
			int a = t.a;
			ArrayList<Tile<Node>> l = byArea.get(a);
			if(l==null){
				l = new ArrayList<>();
				byArea.put(a, l);
			}
			l.add(t);
		});
		ArrayList<GroupedTile<Node>> ret = new ArrayList<>();
		for(Entry<Integer, ArrayList<Tile<Node>>> e : byArea.entrySet()) {
			Collections.sort(e.getValue(),
					(t1, t2) -> t1.id.name.compareTo(t2.id.name));
			GroupedTile<Node> gt = new GroupedTile<>(e.getValue());
			gt.a = e.getKey() * e.getValue().size();
			ret.add(gt);
		}
		Collections.sort(ret, (gt1, gt2) -> gt2.id.get(0).a - gt1.id.get(0).a);
		return ret;
	}
}

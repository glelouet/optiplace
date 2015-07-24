/**
 *
 */
package fr.emn.optiplace.configuration.graphics;

import java.io.StringReader;
import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kitfox.svg.Rect;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class TilesToSVG {

	private static final Logger logger = LoggerFactory.getLogger(TilesToSVG.class);

	public SVGIcon export(List<Tile<Node>> nodes, List<Tile<VM>> vms) {
		SVGUniverse uni = new SVGUniverse();
		SVGIcon icon = new SVGIcon();
		icon.setSvgUniverse(uni);
		int maxX = nodes.stream().mapToInt(t->t.dx+t.x).max().getAsInt();
		int maxY = nodes.stream().mapToInt(t->t.dy+t.y).max().getAsInt();
		URI uri = uni.loadSVG(new StringReader("<svg width=\"" + maxX
				+ "\" height=\"" + maxY + "\" style=\"fill:grey;\"></svg>"), "myImage");
		icon.setSvgURI(uri);
		SVGDiagram diag = uni.getDiagram(uri);
		// for each node
		for (Tile<Node> t : nodes) {
			try {
				Rect r = drawRect(t.x, t.dx, t.y, t.dy, t.color, null);
				if (r != null) {
					diag.getRoot().loaderAddChild(null, r);
				}
			} catch (Exception e) {
				logger.warn("", e);
			}
		}
		for (Tile<VM> v : vms) {
			try {
				Rect r = drawRect(v.x, v.dx, v.y, v.dy, v.color, null);
				if (r != null) {
					diag.getRoot().loaderAddChild(null, r);
				}
			} catch (Exception e) {
				logger.warn("", e);
			}
		}
		return icon;
	}

	public Rect drawRect(int x, int dx, int y, int dy, String fill,
			String strokeColor) {
		Rect ret = new Rect();
		try {
			ret.addAttribute("width", AnimationElement.AT_XML, "" + dx);
			ret.addAttribute("height", AnimationElement.AT_XML, "" + dy);
			ret.addAttribute("x", AnimationElement.AT_XML, "" + x);
			ret.addAttribute("y", AnimationElement.AT_XML, "" + y);
			if (fill != null) {
				ret.addAttribute("fill", AnimationElement.AT_XML, fill);
			}
			if (strokeColor != null) {
				ret.addAttribute("stroke", AnimationElement.AT_XML, strokeColor);
//				ret.addAttribute("stroke-width", AnimationElement.AT_XML, "" + border);
			}
			ret.updateTime(0.0);
		} catch (Exception e) {
			logger.warn("", e);
			return null;
		}
		return ret;
	}

	int border() {
		return 0;
	}

}

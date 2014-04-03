package fr.emn.optiplace.configuration.graphics;

import java.io.StringReader;
import java.net.URI;

import com.kitfox.svg.Rect;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGElementException;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.app.beans.SVGIcon;

import fr.emn.optiplace.configuration.graphics.SVGCreator;

public class SampleLoad {

	/**
	 * @param args
	 * @throws SVGElementException
	 */
	public static void main(String[] args) throws SVGElementException {
		int nb = 200;
		int width = 2;
		int height = 200;
		SVGCreator creator = new SVGCreator();
		SVGUniverse uni = new SVGUniverse();

		SVGIcon icon = new SVGIcon();
		icon.setSvgUniverse(uni);
		URI uri = uni.loadSVG(new StringReader("<svg width=\"" + nb * width
				+ "\" height=\"" + height + "\" style=\"fill:grey;\"></svg>"),
				"myImage");
		icon.setSvgURI(uri);
		SVGDiagram diag = uni.getDiagram(uri);
		for (int i = 0; i < nb; i++) {
			String color = SVGCreator.makeLoadColor((float) i / nb);
			Rect r = creator.drawRect(i * width, width, 0, height, color, null);
			diag.getRoot().loaderAddChild(null, r);
		}
		SVGCreator.writeSVG(icon, "target/sample.png");

	}

}

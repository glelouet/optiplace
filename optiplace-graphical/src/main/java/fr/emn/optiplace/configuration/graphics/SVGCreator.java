package fr.emn.optiplace.configuration.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Iterator;

import javax.imageio.ImageIO;

import com.kitfox.svg.Rect;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VirtualMachine;
import fr.emn.optiplace.configuration.graphics.positionners.BasicPositionner;
import fr.emn.optiplace.configuration.resources.CPUConsSpecification;
import fr.emn.optiplace.configuration.resources.MemConsSpecification;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;

/** @author guillaume */
public class SVGCreator {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SVGCreator.class);

	Positionner p = new BasicPositionner();

	public SVGIcon export(Configuration cfg) {
		ResourceSpecification x = null, y = null;
		Iterator<ResourceSpecification> it = cfg.resources().values()
				.iterator();
		if (it.hasNext()) {
			x = it.next();
		}
		if (it.hasNext()) {
			y = it.next();
		}
		if (x == null) {
			x = CPUConsSpecification.INSTANCE;
		}
		if (y == null) {
			if (x instanceof CPUConsSpecification) {
				y = MemConsSpecification.INSTANCE;
			} else {
				y = CPUConsSpecification.INSTANCE;
			}
		}
		return export(cfg, x, y);
	}
	public SVGIcon export(Configuration cfg, ResourceSpecification x,
			ResourceSpecification y) {
		Square2D[] nodeSquares = new Square2D[cfg.getAllNodes().size()];
		for (int i = 0; i < nodeSquares.length; i++) {
			Node n = cfg.getAllNodes().get(i);
			nodeSquares[i] = new Square2D(i, x.getCapacity(n), y.getCapacity(n));
		}
		Pos[] nodePos = new Pos[nodeSquares.length];
		for (int i = 0; i < nodePos.length; i++) {
			nodePos[i] = new Pos();
		}
		p.organize(nodeSquares, nodePos);
		return export(nodeSquares, nodePos, cfg, x, y);
	}

	public SVGIcon export(Square2D[] squares, Pos[] pos, Configuration cfg,
			ResourceSpecification rx, ResourceSpecification ry) {
		SVGUniverse uni = new SVGUniverse();
		SVGIcon icon = new SVGIcon();
		icon.setSvgUniverse(uni);
		// get maxX and maxY
		int maxX = 0, maxY = 0;
		for (int i = 0; i < squares.length; i++) {
			int x = squares[i].dX + pos[i].x;
			int y = squares[i].dY + pos[i].y;
			if (x > maxX) {
				maxX = x;
			}
			if (y > maxY) {
				maxY = y;
			}
		}
		URI uri = uni.loadSVG(new StringReader("<svg width=\"" + maxX
				+ "\" height=\"" + maxY + "\" style=\"fill:grey;\"></svg>"),
				"myImage");
		icon.setSvgURI(uri);
		SVGDiagram diag = uni.getDiagram(uri);

		// for each node
		for (int i = 0; i < squares.length; i++) {
			Node n = cfg.getOnlines().get(i);
			try {
				double nodeLoad = Math.max(
						rx.getUse(cfg, n) / rx.getCapacity(n),
						ry.getUse(cfg, n) / ry.getCapacity(n));
				Rect r = makeNodeRect(squares[i], pos[i], maxX, maxY,
						makeLoadColor(nodeLoad));
				if (r != null) {
					diag.getRoot().loaderAddChild(null, r);
				}
				int vmX = pos[i].x;
				int vmY = pos[i].y;
				// for each of his vms
				for (VirtualMachine vm : cfg.getRunnings(n)) {
					int dx = rx.getUse(vm), dy = ry.getUse(vm);
					double vmLoadx = 1.0 * dx / squares[i].dX, vmLoady = 1.0
							* dy / squares[i].dY;
					double vmLoad = Math.max(vmLoadx, vmLoady);
					Rect r2 = makeVMRect(vmX, dx, vmY, dy, vmLoad);
					vmX += vm.getCPUConsumption();
					vmY += vm.getMemoryConsumption();
					if (r2 != null) {
						diag.getRoot().loaderAddChild(null, r2);
					}
				}
			} catch (Exception e) {
				logger.warn("", e);
			}
		}
		return icon;
	}
	Rect makeNodeRect(Square2D s, Pos p, int maxX, int maxY, String load) {
		return drawRect(p.x, s.dX, p.y, s.dY, load, strokeColor);
	}

	/** extended informations of the set of nodes in a Configuration */
	public static class StructCfgVal {
		public int minNodeMem = Integer.MAX_VALUE;
		public int maxNodeMem = 0;
		public int sumNodeMem = 0;
		public int minNodeCPU = Integer.MAX_VALUE;
		public int maxNodeCPU = 0;
		public int sumNodeCPU = 0;
	}

	int border = 1;

	String strokeColor = "white";

	Rect makeVMRect(int x, int w, int y, int h, double load) {

		String vmBorder = "black";
		if (vmBorder != null) {
			w -= 2 * border;
			h -= 2 * border;
		}
		return drawRect(x, w, y, h, makeLoadColor(load), vmBorder);
	}

	public Rect drawRect(int x, int w, int y, int h, String fill,
			String strokeColor) {
		Rect ret = new Rect();
		try {
			ret.addAttribute("width", AnimationElement.AT_XML, "" + w);
			ret.addAttribute("height", AnimationElement.AT_XML, "" + h);
			ret.addAttribute("x", AnimationElement.AT_XML, "" + x);
			ret.addAttribute("y", AnimationElement.AT_XML, "" + y);
			if (fill != null) {
				ret.addAttribute("fill", AnimationElement.AT_XML, fill);
			}
			if (strokeColor != null) {
				ret.addAttribute("stroke", AnimationElement.AT_XML, strokeColor);
				ret.addAttribute("stroke-width", AnimationElement.AT_XML, ""
						+ border);
			}
			ret.updateTime(0.0);
		} catch (Exception e) {
			logger.warn("", e);
			return null;
		}
		return ret;
	}

	public static final float threshold = 0.4f;

	public static String makeLoadColor(double load) {
		if (load < threshold) {
			return makeColor(0, (float) Math.sqrt(load / threshold),
					(float) Math.sqrt(1 - load / threshold));
		} else {
			return makeColor(
					(float) Math.sqrt((load - threshold) / (1 - threshold)),
					(float) Math.sqrt(1 - (load - threshold) / (1 - threshold)),
					0);
		}
	}

	protected final static String[] APPENDTOFILL = new String[]{"000000",
			"00000", "0000", "000", "00", "0", ""};

	public static String makeColor(float r, float g, float b) {
		String val = Integer
				.toHexString(new Color(r, g, b).getRGB() & 0x00ffffff);
		return "#" + APPENDTOFILL[val.length()] + val;
	}

	public static boolean writeSVG(SVGIcon icon, String path) {
		BufferedImage image = new BufferedImage(icon.getIconWidth(),
				icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();

		g.setClip(0, 0, icon.getIconWidth(), icon.getIconHeight());
		icon.paintIcon(null, g, 0, 0);
		g.dispose();

		File outFile = new File(path);
		try {
			ImageIO.write(image, "png", outFile);
		} catch (IOException e) {
			logger.warn("Error writing image: ", e);
			return false;
		}
		return true;
	}
}

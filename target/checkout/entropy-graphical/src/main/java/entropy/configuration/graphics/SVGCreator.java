package entropy.configuration.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

import javax.imageio.ImageIO;

import com.kitfox.svg.Rect;
import com.kitfox.svg.SVGDiagram;
import com.kitfox.svg.SVGUniverse;
import com.kitfox.svg.animation.AnimationElement;
import com.kitfox.svg.app.beans.SVGIcon;

import entropy.configuration.Configuration;
import entropy.configuration.Node;
import entropy.configuration.VirtualMachine;

/** @author guillaume */
public class SVGCreator {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(SVGCreator.class);

	int width = 1024;
	int height = 768;

	public SVGIcon export(Configuration cfg) {
		SVGUniverse uni = new SVGUniverse();

		SVGIcon icon = new SVGIcon();
		icon.setSvgUniverse(uni);
		URI uri = uni.loadSVG(new StringReader("<svg width=\"" + width
				+ "\" height=\"" + height + "\" style=\"fill:grey;\"></svg>"),
				"myImage");
		// System.err.println("URI : " + uri);
		// System.err.println("element : " + uni.getElement(uri));
		icon.setSvgURI(uri);
		SVGDiagram diag = uni.getDiagram(uri);
		StructCfgVal cfgVals = extractCfgVals(cfg);
		// int maxCPU = cfgVals.maxNodeCPU;
		// int sumMem = cfgVals.sumNodeMem;
		int doneMem = 0;
		int doneCPU = 0;
		for (Node n : cfg.getOnlines()) {
			int mem = n.getMemoryCapacity();
			int cpu = n.getCoreCapacity() * n.getNbOfCores();
			try {
				Rect r = makeNodeRect(doneCPU, doneMem, n, cfg, cfgVals);
				if (r != null) {
					diag.getRoot().loaderAddChild(null, r);
				}
				int vmCPUs = 0;
				int vmMems = doneMem;
				for (VirtualMachine vm : cfg.getRunnings(n)) {
					Rect r2 = makeVMRect(vmCPUs, vmMems, vm, n, cfgVals);
					vmCPUs += vm.getCPUConsumption();
					vmMems += vm.getMemoryConsumption();
					if (r2 != null) {
						diag.getRoot().loaderAddChild(null, r2);
					}
				}
			} catch (Exception e) {
				logger.warn("", e);
			}
			doneMem += mem;
			doneCPU += cpu;
		}
		return icon;
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

	/**
	 * extract the general informations of a set of nodes in a configuration
	 * 
	 * @param cfg
	 * @return
	 */
	public static StructCfgVal extractCfgVals(Configuration cfg) {
		StructCfgVal ret = new StructCfgVal();
		for (Node n : cfg.getOnlines()) {
			int mem = n.getMemoryCapacity();
			int cpu = n.getCoreCapacity() * n.getNbOfCores();
			if (mem > ret.maxNodeMem) {
				ret.maxNodeMem = mem;
			}
			if (mem < ret.minNodeMem) {
				ret.minNodeMem = mem;
			}
			if (cpu > ret.maxNodeCPU) {
				ret.maxNodeCPU = cpu;
			}
			if (cpu < ret.minNodeCPU) {
				ret.minNodeCPU = cpu;
			}
			ret.sumNodeMem += mem;
			ret.sumNodeCPU += cpu;
		}
		return ret;
	}

	int border = 1;

	String strokeColor = "white";

	Rect makeNodeRect(int doneCPU, int doneMem, Node n, Configuration cfg,
			StructCfgVal cfgvals) {
		int x = doneMem * width / cfgvals.sumNodeMem;
		int y = 0;
		int mem = n.getMemoryCapacity();
		int cpu = n.getCoreCapacity() * n.getNbOfCores();
		int loadCPU = 0, loadMem = 0;
		for (VirtualMachine vm : cfg.getRunnings(n)) {
			loadCPU += vm.getCPUConsumption();
			loadMem += vm.getMemoryConsumption();
		}
		int w = mem * width / cfgvals.sumNodeMem - 2 * border;
		int h = cpu * height / cfgvals.sumNodeCPU - 2 * border;
		return drawRect(x, w, y, h, makeNodeColor(cpu, mem, loadCPU, loadMem),
				strokeColor);
	}

	Rect makeVMRect(int doneCPU, int doneMem, VirtualMachine vm, Node n,
			StructCfgVal cfg) {
		int cpu = vm.getCPUConsumption();
		int mem = vm.getMemoryConsumption();
		int x = doneMem * width / cfg.sumNodeMem;
		int y = doneCPU * height / cfg.sumNodeCPU;

		String vmBorder = "black";
		int w = mem * width / cfg.sumNodeMem;
		int h = cpu * height / cfg.sumNodeCPU;
		if (vmBorder != null) {
			w -= 2 * border;
			h -= 2 * border;
		}
		return drawRect(
				x,
				w,
				y,
				h,
				makeNodeColor(n.getCoreCapacity() * n.getNbOfCores(),
						n.getMemoryCapacity(), cpu, mem), vmBorder);
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

	public static String makeNodeColor(int cpu, int mem, int usedCpu,
			int usedMem) {
		float loadcpu = (float) usedCpu / cpu;
		float loadmem = (float) usedMem / mem;
		float load = Math.max(loadcpu, loadmem);
		return makeLoadColor(load);
	}

	public static String makeLoadColor(float load) {
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

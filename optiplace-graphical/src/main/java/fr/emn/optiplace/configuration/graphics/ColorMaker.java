/**
 *
 */
package fr.emn.optiplace.configuration.graphics;

import java.awt.Color;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 * 
 */
public class ColorMaker {

	public static final float threshold = 0.4f;

	public String makeLoadColor(double load) {
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

	public static final String BLACK = makeColor(0, 0, 0);

}

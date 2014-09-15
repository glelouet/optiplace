/**
 *
 */
package fr.emn.optiplace.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class FileViewData implements ProvidedData {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(FileViewData.class);

	protected String name;

	protected File f;

	/**
	 * @param name
	 * @param f
	 */
	public FileViewData(String name, File f) {
		this.name = name;
		this.f = f;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Stream<String> lines() {
		try {
			return new BufferedReader(new FileReader(f)).lines();
		} catch (FileNotFoundException e) {
			logger.warn("", e);
			return null;
		}
	}

	@Override
	public Map<String, String> toStringMap() {
		Map<String, String> ret = new HashMap<String, String>();
		lines().forEach(
				s -> {
					int posEQUAL = s.indexOf('=');
					if (posEQUAL != -1) {
						ret.put(s.substring(0, posEQUAL),
								s.substring(posEQUAL + 2, s.length() - 1));
					}
				});
		return ret;
	}
}

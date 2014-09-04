/**
 *
 */
package fr.emn.optiplace.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 */
public class FileViewConfiguration implements ViewConfiguration {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(FileViewConfiguration.class);

	protected String name;

	protected File f;

	/**
	 * @param name
	 * @param f
	 */
	public FileViewConfiguration(String name, File f) {
		this.name = name;
		this.f = f;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public Iterable<String> toLineIterable() {
		return new Iterable<String>() {

			@Override
			public Iterator<String> iterator() {
				try {
					return new Iterator<String>() {

						BufferedReader b = new BufferedReader(new FileReader(f));
						String next = null;

						@Override
						public void remove() {
							throw new UnsupportedOperationException();
						}

						@Override
						public String next() {
							if (hasNext()) {
								String ret = next;
								next = null;
								return ret;
							}
							return null;
						}

						@Override
						public boolean hasNext() {
							if (next != null) {
								return true;
							}
							try {
								next = b.readLine();
							} catch (IOException e) {
								logger.warn("", e);
							}
							return next != null;
						}
					};
				} catch (FileNotFoundException e) {
					logger.warn("", e);
					return null;
				}
			}
		};
	}

	@Override
	public Map<String, String> toStringMap() {
		Map<String, String> ret = new HashMap<String, String>();
		for (String s : toLineIterable()) {
			int posEQUAL = s.indexOf('=');
			if (posEQUAL != -1) {
				ret.put(s.substring(0, posEQUAL),
						s.substring(posEQUAL + 2, s.length() - 1));
			}
		}
		return ret;
	}
}

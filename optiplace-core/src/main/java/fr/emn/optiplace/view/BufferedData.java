/**
 *
 */
package fr.emn.optiplace.view;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2014
 *
 */
public class BufferedData extends ArrayList<String> implements ProvidedData {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(BufferedData.class);

	public BufferedData(String name) {
		this.name = name;
	}

	protected String name;

	@Override
	public String name() {
		return name;
	}

	@Override
	public Stream<String> lines() {
		return stream();
	}
}

/**
 *
 */
package fr.emn.optiplace.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 *
 * {@link ProvidedData} inheriting from List<> to acces the stream of String
 * {@link #lines()}
 *
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

    /**
     * add the string representation of an object to this, after splitting this
     * string by end of line "\\n"
     *
     * @param elem
     *            an element to add to this description
     * @return this (to chain with other elems)
     */
    public BufferedData withElem(Object elem) {
	Arrays.stream(elem.toString().split("\\n")).forEach(this::add);
	return this;
    }

    @Override
    public boolean equals(Object o) {
	if (o == this) {
	    return true;
	}
	if (o == null || o.getClass() != BufferedData.class) {
	    return false;
	}
	BufferedData other = (BufferedData) o;
	return name.equals(other.name) && super.equals(other);
    }
}

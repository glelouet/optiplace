package fr.emn.optiplace.configuration;

/**
 * A Node is a {@link VMLocation} we can manage. So we know what exactly is on
 * it, and must respect its resource specifications.
 *
 * @author Guillaume Le Louët
 *
 */
public class Node extends VMLocation {

	/** @param name */
	public Node(String name) {
		super(name);
	}

}

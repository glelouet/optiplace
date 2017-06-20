package fr.emn.optiplace.configuration;

/**
 * A Computer is a {@link VMLocation} we can manage. So we know what exactly is
 * on it, and must respect its resource specifications.
 *
 * @author Guillaume Le Louët
 *
 */
public class Computer extends VMLocation {

	/** @param name */
	public Computer(String name) {
		super(name);
	}

}

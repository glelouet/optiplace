package fr.emn.optiplace.configuration;

/**
 * An extern is a location of a VM we don't actually manage (but we can place -
 * or move away - the VM on it)
 *
 * @author Guillaume Le Louët
 *
 */
public final class Extern extends VMLocation {

	public Extern(String name) {
		super(name);
	}

}

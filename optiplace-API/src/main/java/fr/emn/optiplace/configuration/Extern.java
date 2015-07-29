package fr.emn.optiplace.configuration;

/**
 * An extern is an hoster of a VM we don't actually manage (but we can place -
 * or move away - the VM on it)
 *
 * @author Guillaume Le Louët
 *
 */
public class Extern extends VMHoster {

	public Extern(String name) {
		super(name);
	}

}

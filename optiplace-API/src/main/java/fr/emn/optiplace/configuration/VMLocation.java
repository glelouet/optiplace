
package fr.emn.optiplace.configuration;

/**
 * A VM location is a managedElement that can host VM. methods may return
 * VMLocations instead of Node or Extern as we do not know where a VM is hosted.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class VMLocation extends ManagedElement {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VMLocation.class);

	public VMLocation(String name) {
		super(name);
	}

}


package fr.emn.optiplace.configuration;

/**
 * A VM hoster is a managedElement that can host VM. methods may return
 * VMhosters instead of Node or Extern as we do not know where a VM is hosted.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class VMHoster extends ManagedElement {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(VMHoster.class);

	public VMHoster(String name) {
		super(name);
	}

}

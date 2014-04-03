/**
 *
 */
package fr.emn.optiplace.view;

import fr.emn.optiplace.configuration.resources.CPUConsSpecification;
import fr.emn.optiplace.configuration.resources.MemConsSpecification;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.view.View;

/**
 * a {@link View} with a static instance to provide the default CPU and RAM
 * resources.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class DefaultResourcesView extends EmptyView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(DefaultResourcesView.class);

	public static final DefaultResourcesView INSTANCE = new DefaultResourcesView();

	@Override
	public ResourceSpecification[] getPackedResource() {
		return new ResourceSpecification[]{CPUConsSpecification.INSTANCE,
				MemConsSpecification.INSTANCE};
	}
}

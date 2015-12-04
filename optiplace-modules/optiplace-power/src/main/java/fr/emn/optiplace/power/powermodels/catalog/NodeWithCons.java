/**
 *
 */
package fr.emn.optiplace.power.powermodels.catalog;

import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.power.powermodels.LinearCPUCons;
import fr.emn.optiplace.power.powermodels.StepCPUCons;

/**
 * extends the Node with basic cons informations and model providing. Only
 * useful for the Catalog, not to use in the solving process
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 */
public class NodeWithCons extends Node {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NodeWithCons.class);

	private final double[] observedConsValues;

	protected int nbCores, coreCapa, memoryCapa;

	/**
	 * @param name
	 *          the name of the model
	 * @param nbOfCore
	 *          the number of cores installed on the server
	 * @param coreCapa
	 *          the capacity of each core on the server
	 * @param memoryCapacity
	 *          installed memory available on the server
	 * @param observedConsValues
	 *          the consumption values associated to the CPU loads, for 0, 1/n,
	 *          2/n ... 100% of the cpu activity, n+1 being the number of
	 *          observations.
	 */
	public NodeWithCons(String name, int nbOfCore, int coreCapa, int memoryCapacity, double[] observedConsValues) {
		super(name);
		nbCores = nbOfCore;
		this.coreCapa = coreCapa;
		memoryCapa = memoryCapacity;
		this.observedConsValues = observedConsValues;
	}

	public Node makeNode(String name) {
		return new Node(name);
	}

	protected LinearCPUCons makeLinearCPUCons() {
		return Tools.makeLinearCPUConsFromObservations(observedConsValues);
	}

	protected LinearCPUCons cachedLinearCPUCons = null;

	public LinearCPUCons getLinearCPUCons() {
		if (cachedLinearCPUCons == null) {
			cachedLinearCPUCons = makeLinearCPUCons();
		}
		return cachedLinearCPUCons;
	}

	protected StepCPUCons makeStepCPUCons() {
		return Tools.makeStepCPUConsFromObservations(observedConsValues);
	}

	protected StepCPUCons cachedStepCPUCons = null;

	public StepCPUCons getStepCPUCons() {
		if (cachedStepCPUCons == null) {
			cachedStepCPUCons = makeStepCPUCons();
		}
		return cachedStepCPUCons;
	}

}

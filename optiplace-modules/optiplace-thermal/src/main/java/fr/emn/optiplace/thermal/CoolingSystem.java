package fr.emn.optiplace.thermal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cooling system efficiency. a cooling system dissipates a given power from an
 * air flow, to produce an air flow at constant temperature
 *
 * @author Guillaume Le Louët 2016 [ guillaume.lelouet@gmail.com ]
 *
 */
public class CoolingSystem {

	private static final Logger logger = LoggerFactory
			.getLogger(CoolingSystem.class);

	/**
	 * get the cost (in W) to cool down the consumption of the servers, when
	 * assigned given thermostat
	 *
	 * @param serversConsumptions
	 *          power to extract from the air.
	 * @param thermostat
	 *          temperature of air
	 * @return the additional power requried to extract given power from the air.
	 */
	public double getCoolingCost(double serversConsumptions, double thermostat) {
		logger.trace("sumconsumptions = " + serversConsumptions + ", eff="
				+ getEfficiency(thermostat));
		return serversConsumptions / getEfficiency(thermostat);

	}

	protected double eff20 = 1;
	protected double effCoeff = 0.1;

	/**
	 * set the linear value for the efficiency of the Cooling system.
	 *
	 * @param eff20
	 *          the extraction efficiency at 20°C, as W removed from the incoming
	 *          air per watt consumed.
	 * @param effCoeff
	 *          the extraction efficiency gain per 1°C increase
	 */
	public void setEffValues(double eff20, double effCoeff) {
		this.eff20 = eff20;
		this.effCoeff = effCoeff;
	}

	/**
	 * gives the efficiency of the cooling system for given outlet temperature.
	 * <br />
	 * This gives a linear function between 1 at 20°C required and 2 at 30°C. This
	 * should be overloaded in subclasses.
	 *
	 * @param thermostat
	 *          the temperature at which to produce the air
	 * @return the number of °C that can be extracted from the inlet air flow per
	 *         watt consumed by the cooling system, in order to produce outlet air
	 *         at given temperature.
	 */
	public double getEfficiency(double thermostat) {
		return (thermostat - 20) * effCoeff + eff20;
	}

}

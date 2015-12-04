package fr.emn.optiplace.thermal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * specify the cooling of a set of servers. require an {@link ImpactMap}, and
 * the specification of the servers' max inlet temperature.<br />
 * 
 * @author guillaume
 */
public class CoolingSystem {

	private static final Logger logger = LoggerFactory
			.getLogger(CoolingSystem.class);

	/**
	 * get the cost (in W) to cool down the consumption of the servers, when
	 * assigned given thermostat
	 * 
	 * @param serversConsumptions
	 * @param thermostat
	 * @return
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
	 *            the efficiency(°C removed from the incoming air) per watt
	 *            consumed when the outgoing thermostat is 20 °C
	 * @param effCoeff
	 *            the gain of °C removed from from incoming air per watt
	 *            consumed when the outgoing air thermostat increases per one
	 *            °C.
	 */
	public void setEffValues(double eff20, double effCoeff) {
		this.eff20 = eff20;
		this.effCoeff = effCoeff;
	}

	/**
	 * gives the efficiency of the cooling system for given outlet temperature.<br />
	 * This gives a linear function between 1 at 20°C required and 2 at 30°C.
	 * This should be overloaded in subclasses.
	 * 
	 * @param outletTemp
	 *            the temperature at which to produce
	 * @return the number of °C that can be extracted from the inlet air flow
	 *         per watt consumed by the cooling system, in order to produce
	 *         outlet air at given temperature.
	 */
	public double getEfficiency(double thermostat) {
		return (thermostat - 20) * effCoeff + eff20;
	}

}

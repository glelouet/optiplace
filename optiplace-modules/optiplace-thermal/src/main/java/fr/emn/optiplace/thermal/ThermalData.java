package fr.emn.optiplace.thermal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.emn.optiplace.power.PowerData;
import fr.emn.optiplace.view.ProvidedDataReader;

/**
 * The thermal informations of a datacenter monitored.<br />
 * Requires
 * <ul>
 * <li>{@link ImpactMap} to map between servers consumptions(W) and entry air
 * temperature(°C). Default is 0 impact map</li>
 * <li>{@link PowerData} to modelize the servers' consumption from a
 * configuration.</li>
 * <li>{@link #serversMaxTemperature servers entry temperature requirements}.
 * This map specifies all the handled servers</li>
 * <li>{@link #coolingSystems CoolingSystem}s to specify the cooling cost of
 * each set of server.</li>
 * </ul>
 *
 * @author guillaume
 */
public class ThermalData implements ProvidedDataReader {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ThermalData.class);

	/**
	 * make a weight map of strings to the given value.
	 *
	 * @param weight
	 * The weight to give to each name
	 * @param restrictedServers
	 * the Strings to e weighted
	 * @return a new {@link HashMap} from those names to this value.
	 */
	public static HashMap<String, Double> makeRestrictedWeight(double weight,
			String... restrictedServers) {
		HashMap<String, Double> ret = new HashMap<String, Double>();
		if (restrictedServers != null) {
			for (String s : restrictedServers) {
				ret.put(s, weight);
			}
		}
		return ret;
	}

	/** max temperature allowed for each server. */
	protected HashMap<String, Double> serversMaxTemperature = new HashMap<String, Double>();

	/** @return */
	protected HashMap<String, Double> getServersMaxTemperature() {
		return serversMaxTemperature;
	}

	/**
	 * @param serversMaxTemperature
	 * the serversMaxTemperature to set
	 */
	public void setServersMaxTemperature(
			HashMap<String, Double> serversMaxTemperature) {
		this.serversMaxTemperature = serversMaxTemperature;
	}

	/** the map of impact from each server to each other */
	protected ImpactMap impactMap = new ImpactMap();

	/** @return the impactMap */
	public ImpactMap getImpactMap() {
		return impactMap;
	}

	/**
	 * @param impactMap
	 * the impactMap to set
	 */
	public void setImpactMap(ImpactMap impactMap) {
		this.impactMap = impactMap;
	}

	protected HeatRepartition heatRepartition = new HeatRepartition();

	protected List<CoolingSystem> coolingSystems = new ArrayList<CoolingSystem>();

	/** @return the coolingSystems */
	public List<CoolingSystem> getCoolingSystems() {
		return coolingSystems;
	}

	/**
	 * @param coolingSystems
	 * the coolingSystems to set
	 */
	public void setCoolingSystems(List<CoolingSystem> coolingSystems) {
		this.coolingSystems = coolingSystems;
		heatRepartition.addCoolingSystem(coolingSystems
				.toArray(new CoolingSystem[] {}));
	}

	/**
	 * @param l
	 */
	@Override
	public void readLine(String l) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}

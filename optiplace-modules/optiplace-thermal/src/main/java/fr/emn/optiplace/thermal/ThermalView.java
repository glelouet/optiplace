package fr.emn.optiplace.thermal;

import java.util.HashMap;
import java.util.Map.Entry;

import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.ProvidedData;
import fr.emn.optiplace.view.annotations.Depends;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;

/**
 * @author guillaume
 *
 */
@ViewDesc
public class ThermalView extends EmptyView {
  @SuppressWarnings("unused")
  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
  .getLogger(ThermalView.class);

  @Parameter(confName = "thermal")
  ThermalData data = new ThermalData();

  @Override
  public void setConfig(ProvidedData conf) {
    data = new ThermalData();
    data.read(conf);
  };

  @Depends
  PowerView conv;

  /**
   * get the cooling cost associated to a configuration
   *
   * @param cfg
   *            the configuration to cool down
   * @return the sum of the cooling costs of the {@link CoolingSystem}s
   */
  // public double getCoolingCost(Configuration cfg, ) {
  // double res = 0;
  // HashMap<String, Double> consumptions = conv.getConsumptionData()
  // .getConsumptions(cfg, false);
  // HashMap<CoolingSystem, Double> dissipations = new HashMap<CoolingSystem,
  // Double>();
  // for (CoolingSystem cs : data.coolingSystems) {
  // dissipations.put(cs, Double.POSITIVE_INFINITY);
  // }
  // for (Entry<String, Double> e : consumptions.entrySet()) {
  // String s = e.getKey();
  // Double c = e.getValue();
  // Map<CoolingSystem, Double> repartitions = data.heatRepartition
  // .getRepartition(s);
  // for (CoolingSystem cs : data.coolingSystems) {
  // dissipations.put(cs,
  // dissipations.get(cs) + c * repartitions.get(cs));
  // }
  // }
  // HashMap<String, Double> thermostats =
  // getRequiredThermostats(consumptions);
  // for (CoolingSystem cs : data.coolingSystems) {
  // double minThermostat = Double.POSITIVE_INFINITY;
  // for (String s : data.heatRepartition.getDissipatedServers(cs)) {
  // if (thermostats.get(s) < minThermostat) {
  // minThermostat = thermostats.get(s);
  // }
  // }
  // res += cs.getCoolingCost(dissipations.get(cs), minThermostat);
  // }
  // return res;
  // }

  HashMap<String, Double> getRequiredThermostats(
      HashMap<String, Double> consumptions) {
    HashMap<String, Double> ret = new HashMap<String, Double>(
        data.serversMaxTemperature);
    HashMap<String, Double> elevations = data.impactMap
        .getTempIncreases(consumptions);
    for (Entry<String, Double> e : ret.entrySet()) {
      e.setValue(e.getValue() - elevations.get(e.getKey()));
    }
    return ret;
  }

}

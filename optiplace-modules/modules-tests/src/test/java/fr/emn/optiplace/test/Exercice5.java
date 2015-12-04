package fr.emn.optiplace.test;

import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.hostcost.HostCostView;
import fr.emn.optiplace.power.PowerView;
import fr.emn.optiplace.power.rules.LimitPower;

/**
 * Problème de placement avec réduction du coût et limite de la consommation.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
public class Exercice5 {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Exercice5.class);

	public static void main(String[] args) {
		Configuration c = new Configuration("cpu", "mem");
		Node n0 = c.addOnline("n0", 100);
		Node n1 = c.addOnline("n1", 200);

		HAView ha = new HAView();

		PowerView pw = new PowerView();
		pw.getPowerData().setLinearConsumption(n0, 20, 60);
		pw.addRule(new LimitPower(pw, 50));

		HostCostView hc = new HostCostView();

		IOptiplace o = new Optiplace(c).with(ha).with(pw).with(hc);
		o.withGoal("hostcost");
	}
}

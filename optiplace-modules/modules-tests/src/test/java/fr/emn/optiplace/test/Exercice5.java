package fr.emn.optiplace.test;

import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.VM;
import fr.emn.optiplace.hostcost.HostCostView;
import fr.emn.optiplace.power.PowerView;


/**
 * Problème de placement avec réduction du coût et limite de la consommation.
 *
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
@SuppressWarnings("unused")
public class Exercice5 {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Exercice5.class);

	public static void main(String[] args) {
		Configuration c = new Configuration("CPU");
		Node n0 = c.addOnline("n0", 100);
		Node n1 = c.addOnline("n1", 200);
		Extern e = c.addExtern("e", 100);

		VM v0 = c.addVM("v0", null, 30);
		VM v1 = c.addVM("v1", null, 30);
		VM v2 = c.addVM("v2", null, 30);
		VM v3 = c.addVM("v3", null, 45);
		VM v4 = c.addVM("v4", null, 45);

		// la vue powerview permet de spécifier des modèles de consommation
		// pour l'instant un seul modèle : linéaire en CPU
		PowerView pw = new PowerView();
		pw.getPowerData().setLinearConsumption(n0, 20, 120);
		pw.getPowerData().setLinearConsumption(n1, 30, 430);
		pw.limitSumPower(150);

		HostCostView hc = new HostCostView();
		hc.getCostData().setHostCost(e.getName(), 2);

		IOptiplace o = new Optiplace(c).with(pw).with(hc);
		o.withGoal("hostcost");

		System.err.println(o.solve().getDestination());
	}
}

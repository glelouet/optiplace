package fr.emn.optiplace.test;

import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.Extern;
import fr.emn.optiplace.configuration.Node;
import fr.emn.optiplace.configuration.Site;
import fr.emn.optiplace.configuration.VM;

/**
 * Placement de VM sur des Node et Extern
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2015
 *
 */
@SuppressWarnings("unused")
public class Exercice1 {

	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Exercice1.class);

	public static void main(String[] args) {

		// construction du probleme à resoudre
		// une configuration représente l'ensemble des éléments utilisés
		// mais aussi la position des VM,
		// le regroupement des hosters en sites,
		// et les besoins et capacités en ressources
		Configuration c = new Configuration();

		Extern e = c.addExtern("extern");
		Node n = c.addOnline("node");

		// on met le node et l'extern dans le même site.
		Site site = c.addSite("site", e, n);

		// v0 est initialement placée sur l'extern
		VM v0 = c.addVM("v0", e);

		// v1 est initialement placée sur le node
		VM v1 = c.addVM("v1", n);

		// on crée une VM avec un nom équivalent a celui d'un node existant
		// donc elle est null
		VM falseVM = c.addVM("Node", n);
		// lève une NPE
		System.err.println(falseVM.getName());

		// cette fois on devrait avoir une VM correcte puisqu'elle existe déjà
		VM v1bis = c.addVM("v1", n);
		// VM v1bis=c.addVM("v1", e);
		// aurait deplacé la VM sur l'extern
		System.err.println(v1bis.getName());

		// résolution du problème
		Optiplace s = new Optiplace(c);
		s.solve();
		// affichage sur la sortie d'erreur de la configuration
		// déterminée par Optiplace
		System.err.println(s.getTarget().getDestination());

		// en une ligne :
		// System.err.println(new Optiplace(c).solve().getDestination());
	}

}

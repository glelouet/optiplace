package fr.emn.optiplace.server;

import java.io.File;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2014
 * 
 */
public class ViewManagerMain {
	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory
			.getLogger(ViewManagerMain.class);

	public static void main(String[] args) {
		ViewManager m = new ViewManager();
		m.setJarDir(new File("target/"));
		m.start();
	}
}

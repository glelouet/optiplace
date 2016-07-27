/**
 *
 */
package fr.emn.optiplace.hosanna;

import com.usharesoft.hosanna.tosca.parser.factory.ToscaParserFactory;

import alien4cloud.tosca.parser.ParsingException;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class LoadFile {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoadFile.class);

	/**
	 * @param args
	 * @throws ParsingException
	 */
	public static void main(String[] args) throws ParsingException {
		ToscaParserFactory.getInstance().getToscaParser().fromYaml("hosanna-sample.yml");
	}
}

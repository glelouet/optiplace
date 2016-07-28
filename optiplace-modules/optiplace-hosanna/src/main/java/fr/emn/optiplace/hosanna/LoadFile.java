/**
 *
 */
package fr.emn.optiplace.hosanna;

import java.io.IOException;

import com.usharesoft.hosanna.tosca.parser.HosannaToscaParser;
import com.usharesoft.hosanna.tosca.parser.factory.ToscaParserFactory;

import alien4cloud.tosca.model.ArchiveRoot;
import alien4cloud.tosca.parser.ParsingException;
import alien4cloud.tosca.parser.ParsingResult;

/**
 * tests on the parser. Load a file to yaml, then from yaml to string.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 *
 */
public class LoadFile {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(LoadFile.class);

	/**
	 * @param args
	 * @throws ParsingException
	 * @throws IOException
	 */
	public static void main(String[] args) throws ParsingException, IOException {
		HosannaToscaParser parser = ToscaParserFactory.getInstance().getToscaParser();
		ParsingResult<ArchiveRoot> res = parser.fromYaml("hosanna-sample.yml");
		System.out.println(parser.toYaml(res));
	}
}

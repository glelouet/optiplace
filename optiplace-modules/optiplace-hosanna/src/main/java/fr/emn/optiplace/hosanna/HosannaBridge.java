/**
 *
 */
package fr.emn.optiplace.hosanna;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.usharesoft.hosanna.tosca.parser.factory.ToscaParserFactory;

import alien4cloud.tosca.model.ArchiveRoot;
import alien4cloud.tosca.parser.ParsingResult;
import fr.emn.optiplace.DeducedTarget;
import fr.emn.optiplace.IOptiplace;
import fr.emn.optiplace.Optiplace;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.parser.ConfigurationFiler;
import fr.emn.optiplace.ha.HAView;
import fr.emn.optiplace.hosanna.activeeon.InfraParser;
import fr.emn.optiplace.hosanna.alien4cloud.ToscaBridge;

/**
 * loads up a TOSCA YAML file, translate it to an optiplace configuration, call optiplace on it and translate back to
 * TOSCA.
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 */
public class HosannaBridge {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(HosannaBridge.class);

	private String infraFile = null;

	public void setInfraFile(String fileName) {
		infraFile = fileName;
	}

	public IConfiguration loadPhysical() {
		if (infraFile != null) {
			ConfigurationFiler cf = new ConfigurationFiler(new File(infraFile));
			cf.read();
			return cf.getCfg();
		} else {
			return new InfraParser().getInfra();
		}
	}

	/**
	 * load a TOSCA file, add it to the physical infrastructure, then solve the problem.
	 *
	 * @param filename
	 *          name of the TOSCA file to solve
	 * @return the modified TOSCA description, after solving the problem.
	 */
	public ArchiveRoot solveTosca(String filename) {
		ToscaBridge tb = new ToscaBridge(filename);
		IConfiguration src = loadPhysical();
		HAView ha = new HAView();

		ArchiveRoot ret = tb.addToOptiplace(src, ha);
		IOptiplace pb = new Optiplace(src).with(ha);
		DeducedTarget dest = pb.solve();
		// System.err.println("dest is " + dest.getDestination());
		tb.addPlacement(dest, ret);
		return ret;
	}

	public static void main(String[] args) throws IOException {
		final int NEXT_NONE = 0, NEXT_INFRA = 1, NEXT_OUT = 2;
		int next = NEXT_NONE;
		String toscaInFile = null, infraFile = null, toscaOutFile = null;

		for (String arg : args) {
			switch (next) {
			case NEXT_NONE:
				switch (arg) {
				case "-i":
					next = NEXT_INFRA;
					break;
				case "-o":
					next = NEXT_OUT;
					break;
				default:
					toscaInFile = arg;
				}
				break;
			case NEXT_INFRA:
				infraFile = arg;
				next = NEXT_NONE;
				break;
			case NEXT_OUT:
				toscaOutFile = arg;
				next = NEXT_NONE;
				break;
			default:
				throw new UnsupportedOperationException(
						"while parsing commad line argument, token type " + next + " is unknown");
			}
		}
		if (toscaInFile == null || next != NEXT_NONE) {
			System.err.println("error : requires at least the tosca file to read\n"
					+ "java -jar jarfile TOSCAFILE [-i INFRATRUCTUREFILE] [-o TOSCAOUTFILE] [-s SERVICEFILE]\n"
					+ "if TOSCAOUTFILE is not specified, the result is written to stdout");
			return;
		}
		HosannaBridge hb = new HosannaBridge();
		hb.setInfraFile(infraFile);

		ParsingResult<ArchiveRoot> res = new ParsingResult<>();
		res.setResult(hb.solveTosca(toscaInFile));
		String data = ToscaParserFactory.getInstance().getToscaParser().toYaml(res);

		if (toscaOutFile != null) {
			try (PrintWriter out = new PrintWriter(toscaOutFile)) {
				out.println(data);
			}
		} else {
			System.out.println(data);
		}
	}

}

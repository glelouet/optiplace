
package fr.emn.optiplace.power;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import fr.emn.optiplace.configuration.Computer;
import fr.emn.optiplace.configuration.IConfiguration;
import fr.emn.optiplace.configuration.resources.ResourceSpecification;
import fr.emn.optiplace.power.PowerModel.Parser;
import fr.emn.optiplace.power.powermodels.LinearCPUCons;
import fr.emn.optiplace.view.ProvidedDataReader;


/**
 * associates servers names to their consumption model. handles storing and
 * loading of the data. Not synchronized.<br />
 * gives the consumption of a node in a configuration, as a linear function of
 * its CPU load. Also gives the total consumption of a {@link IConfiguration}.
 * <br />
 * I this model, the consumption of a Computer with no VM is its min consumption
 * ; its optimal consumption is 0. Optimal thus means the nodes can be shut down
 * with no additional cost
 *
 * @author guillaume
 */
public class PowerData extends HashMap<Computer, PowerModel> implements ProvidedDataReader {

	private static final long serialVersionUID = 1L;

	private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PowerData.class);

	public PowerModel get(Computer n) {
		PowerModel ret = super.get(n);
		if (ret != null) {
			return ret;
		}
		for (java.util.Map.Entry<Pattern, PowerModel> e : matchings.entrySet()) {
			if (e.getKey().matcher(n.getName()).matches()) {
				return e.getValue();
			}
		}
		return null;
	}

	/**
	 * set the min and max consumption of a node
	 *
	 * @param host
	 *          the node
	 * @param min
	 *          the min
	 * @param max
	 *          the max
	 */
	public void setLinearConsumption(Computer host, double min, double max) {
		put(host, new LinearCPUCons(min, max));
	}

	Parser[] parsers = {
			LinearCPUCons.PARSER
	};

	/**
	 * parse a description to a model
	 *
	 * @param s
	 *          the description
	 * @return the model parsed or null if it does not match any known model
	 */
	public PowerModel parse(String s) {
		for (Parser p : parsers) {
			PowerModel ret = p.parse(s);
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}

	/**
	 * parse a model and affect it to a Computer
	 *
	 * @param n
	 *          the node
	 * @param model
	 *          the model description
	 * @return the parsed model
	 */
	public PowerModel put(Computer n, String model) {
		PowerModel ret = parse(model);
		put(n, ret);
		return ret;
	}

	/**
	 * write this data to a {@link OutputStream}<br />
	 * format : each line is : [NAME MODELCLASS MODEL.toString()]
	 *
	 * @param os
	 *          the stream to write the data to. Is flushed at the end.
	 */
	public void write(OutputStream os) {
		PrintStream ps = new PrintStream(os);
		for (java.util.Map.Entry<Computer, PowerModel> e : entrySet()) {
			PowerModel sd = e.getValue();
			ps.println(e.getKey().getName() + " " + sd.getClass().getSimpleName() + " " + sd.toString());
		}
		ps.flush();
	}

	/**
	 * read this data from an {@link InputStream}. The data must be in the format
	 * of the {@link #write(OutputStream)}
	 *
	 * @param is
	 *          the stream to read data from
	 * @throws IOException
	 *           if an exception appeared while reading from is
	 */
	public void read(InputStream is) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = br.readLine();
		while (line != null) {
			readLine(line);
			line = br.readLine();
		}
	}

	@Override
	public void readLine(String line) {
		try {
			if (line != null) {
				int idx = line.indexOf(' ');
				String sname = line.substring(0, idx);
				String models = line.substring(idx + 1);
				put(new Computer(sname), parse(models));
			}
		}
		catch (Exception e) {
			logger.warn("could not decode line " + line, e);
		}

	}

	/**
	 * gives the consumption of a node in a given {@link IConfiguration}, using
	 * the linear interpolation.
	 *
	 * @param cfg
	 *          the configuration of the VMs on the nodes
	 * @param n
	 *          the node to get the consumption
	 * @return the integer value of the consumption of the node
	 */
	public double getConsumption(IConfiguration cfg, Computer n) {
		return get(n).getConsumption(cfg, n);
	}

	public HashMap<Computer, Double> getConsumptions(IConfiguration cfg, boolean unusedOff) {
		HashMap<Computer, Double> ret = new HashMap<>();
		cfg.getComputers().forEach(n -> {
			double val = unusedOff ? getUnusedOffConsumption(cfg, n) : getConsumption(cfg, n);
			ret.put(n, val);
		});
		return ret;
	}

	/**
	 * gives the consumption of a node in a given {@link IConfiguration}, using
	 * the linear interpolation, if it can be switched off. The consumption of a
	 * node is 0 if this node is not used by VMs.
	 *
	 * @param cfg
	 *          the configuration of the VMs on the nodes
	 * @param n
	 *          the node to get the consumption
	 * @return the integer value of the optimal consumption of the node
	 */
	public double getUnusedOffConsumption(IConfiguration cfg, Computer n) {
		if (cfg.nbHosted(n) == 0) {
			return 0;
		}
		return getConsumption(cfg, n);
	}

	/**
	 * get the total consumption of a configuration
	 *
	 * @param cfg
	 *          the configuration to evaluate. all the present node must be
	 *          contained in this' {@link #keySet()}
	 * @return the sumn of the online nodes' consumption.
	 */
	public double getTotalConsumption(IConfiguration cfg, Map<String, ResourceSpecification> specs) {
		return cfg.getComputers().mapToDouble(n -> getConsumption(cfg, n)).sum();
	}

	/**
	 * get the total optimal consumption of a configuration
	 *
	 * @param cfg
	 *          the configuration to evaluate. all the present node must be
	 *          contained in this' {@link #keySet()}
	 * @return the sumn of the online nodes' optimal consumption.
	 * @see #getUnusedOffConsumption(IConfiguration, Computer) for the optimal
	 *      consumption
	 */
	public double getTotalUnusedOffConsumption(IConfiguration cfg) {
		return cfg.getComputers().mapToDouble(n -> getUnusedOffConsumption(cfg, n)).sum();
	}

	final LinkedHashMap<Pattern, PowerModel> matchings = new LinkedHashMap<>();

	/**
	 * @return the patterns from the servers names to their consumption models
	 */
	public LinkedHashMap<Pattern, PowerModel> getMatchings() {
		return matchings;
	}
}

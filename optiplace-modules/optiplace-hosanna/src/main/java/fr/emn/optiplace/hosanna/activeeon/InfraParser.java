package fr.emn.optiplace.hosanna.activeeon;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import com.jayway.jsonpath.JsonPath;

import fr.emn.optiplace.configuration.Configuration;
import fr.emn.optiplace.configuration.IConfiguration;

public class InfraParser {

	protected String getInfraText() {

		try (Scanner scan = new Scanner(
				new URL("http://try-pca.activeeon.com:8080/connector-iaas/infrastructures/").openStream())) {
			StringBuilder sb = new StringBuilder();
			scan.forEachRemaining(sb::append);
			return sb.toString();
		} catch (IOException e) {
			throw new UnsupportedOperationException("uncaught", e);
		}
	}

	public IConfiguration getInfra() {
		Configuration ret = new Configuration("");
		List<String> elems = JsonPath.read(getInfraText(),
				"$.*.id");
		elems.forEach(c -> {
			ret.addExtern(c);
		});
		return ret;
	}

}

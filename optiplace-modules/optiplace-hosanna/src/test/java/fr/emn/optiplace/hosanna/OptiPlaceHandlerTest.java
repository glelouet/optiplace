package fr.emn.optiplace.hosanna;

import org.hosanna.infrastructure.Infrastructure;
import org.testng.annotations.Test;

public class OptiPlaceHandlerTest {

	@Test
	public void testInfra() {
		Infrastructure infra = Infrastructure.getInfrastructure();
		System.err.println("" + infra);
	}

}

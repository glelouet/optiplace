package fr.emn.optiplace.core.packers;

import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import solver.variables.IntVar;
import fr.emn.optiplace.core.packers.FastBinPacker;

public class FastBinPackerTest {

	@Test
	public void testInsertDescreasing() {
		IntVar[] vars = {Mockito.mock(IntVar.class),
				Mockito.mock(IntVar.class),
				Mockito.mock(IntVar.class)};
		for (int i = 0; i < vars.length; i++) {
			Mockito.when(vars[i].getVal()).thenReturn(i);
			Mockito.when(vars[i].toString()).thenReturn("v" + i);
		}
		ArrayList<IntVar> list = new ArrayList<IntVar>();
		list.add(vars[2]);
		list.add(vars[0]);
		int index = FastBinPacker.insertDescreasing(list, vars[1]);
		Assert.assertEquals(index, 1);
		Assert.assertEquals(list,
				Arrays.asList(new IntVar[]{vars[2], vars[1], vars[0]}));
	}

}

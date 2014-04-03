package entropy.core.packers;

import java.util.ArrayList;
import java.util.Arrays;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import choco.kernel.solver.variables.integer.IntDomainVar;
import entropy.core.packers.FastBinPacker;

public class FastBinPackerTest {

	@Test
	public void testInsertDescreasing() {
		IntDomainVar[] vars = {Mockito.mock(IntDomainVar.class),
				Mockito.mock(IntDomainVar.class),
				Mockito.mock(IntDomainVar.class)};
		for (int i = 0; i < vars.length; i++) {
			Mockito.when(vars[i].getVal()).thenReturn(i);
			Mockito.when(vars[i].toString()).thenReturn("v" + i);
		}
		ArrayList<IntDomainVar> list = new ArrayList<IntDomainVar>();
		list.add(vars[2]);
		list.add(vars[0]);
		int index = FastBinPacker.insertDescreasing(list, vars[1]);
		Assert.assertEquals(index, 1);
		Assert.assertEquals(list,
				Arrays.asList(new IntDomainVar[]{vars[2], vars[1], vars[0]}));
	}

}

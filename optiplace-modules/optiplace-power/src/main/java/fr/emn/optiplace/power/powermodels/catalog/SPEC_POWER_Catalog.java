package fr.emn.optiplace.power.powermodels.catalog;

/**
 * {@link NodeWithCons} models from thespecpower web site.
 * 
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2013
 */
public class SPEC_POWER_Catalog {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SPEC_POWER_Catalog.class);

	/**
	 * <pre>
	 * //
	 * // bestCPU/pow :
	 * public static final NodeWithCons name = new NodeWithCons(&quot;&quot;, cores, mhz, GB * 1024, new double[] {});
	 * </pre>
	 */

	// //////////2013-q2

	// http://www.spec.org/power_ssj2008/results/res2013q2/power_ssj2008-20130409-00602.html
	// bestCPU/pow : 4817
	public static final NodeWithCons Acer_Altos_R380_F2 = new NodeWithCons("Acer Altos R380 F2", 16, 2200, 24 * 1024,
			new double[] { 71.5, 89., 104, 122, 146, 174, 193, 213, 239, 295, 316 });

	// http://www.spec.org/power_ssj2008/results/res2013q2/
	// bestCPU/pow : 7829
	public static final NodeWithCons Dell_PowerEdge_C5220 = new NodeWithCons("Dell PowerEdge C5220", 48, 2600, 96 * 1024,
			new double[] { 194, 254, 303, 345, 386, 427, 481, 539, 597, 635, 672 });

	// //////////2013-q1

	// http://www.spec.org/power_ssj2008/results/res2013q1/power_ssj2008-20121224-00593.html
	// bestCPU/pow : 4880
	public static final NodeWithCons Acer_AR580_F2 = new NodeWithCons("Acer AR580 F2", 8, 2700, 32 * 1024,
			new double[] { 134, 172, 202, 239, 286, 346, 405, 452, 490, 582, 667 });

	// http://www.spec.org/power_ssj2008/results/res2013q1/power_ssj2008-20121212-00590.html
	// bestCPU/pow : 4101
	public static final NodeWithCons Acer_AW2000h_Aw170h_F2 = new NodeWithCons("Acer AW2000h-Aw170h F2", 64, 2600,
			128 * 4 * 1024, new double[] { 364, 461, 548, 649, 779, 915, 1052, 1169, 1257, 1526, 1700 });

	// http://www.spec.org/power_ssj2008/results/res2013q1/power_ssj2008-20121224-00594.html
	// bestCPU/pow : 4793
	public static final NodeWithCons Acer_AR360_F2 = new NodeWithCons("Altos R380 F2", 4, 2200, 24 * 1024,
			new double[] { 69.4, 103, 117, 137, 159, 182, 201, 218, 240, 290, 315 });

	// /////////////2012-q1

	// http://www.spec.org/power_ssj2008/results/res2012q1/power_ssj2008-20120306-00434.html
	// bestCPU/pow : 5234
	public static final NodeWithCons Dell_PowerEdge_R620 = new NodeWithCons("Dell PowerEdge R620", 16, 2600, 24 * 1024,
			new double[] { 54.1, 78.4, 88.5, 99.5, 115, 126, 143, 165, 196, 226, 243 });

	// http://www.spec.org/power_ssj2008/results/res2012q1/power_ssj2008-20120306-00435.html
	// bestCPU/pow : 5455
	public static final NodeWithCons Dell_PowerEdge_R720 = new NodeWithCons("Dell PowerEdge R720", 16, 2600, 24 * 1024,
			new double[] { 51, 75, 84.5, 95, 110, 120, 137, 159, 187, 217, 231 });

	// http://www.spec.org/power_ssj2008/results/res2012q1/power_ssj2008-20120306-00436.html
	// bestCPU/pow : 5576
	public static final NodeWithCons Dell_PowerEdge_T620 = new NodeWithCons("Dell PowerEdge T620", 16, 2600, 24 * 1024,
			new double[] { 50.2, 73.7, 83.3, 93.8, 110, 119, 135, 156, 183, 212, 227 });

	// http://www.spec.org/power_ssj2008/results/res2012q1/power_ssj2008-20120305-00429.html
	// bestCPU/pow : 6030
	public static final NodeWithCons Fujitsu_PRIMERGY_RX300_S7 = new NodeWithCons("Fujitsu PRIMERGY RX300 S7", 16, 2200,
			32 * 1024, new double[] { 53.1, 74.9, 85.3, 94.9, 105, 117, 134, 156, 187, 217, 245 });

	// http://www.spec.org/power_ssj2008/results/res2012q1/power_ssj2008-20120229-00423.html
	// bestCPU/pow : 5234
	public static final NodeWithCons Huawei_RH2288_V2_E52660 = new NodeWithCons("Huawei_RH2288_V2_E52660", 16, 2200,
			32 * 1024, new double[] { 59.7, 87.2, 97.9, 109, 120, 133, 150, 171, 196, 231, 257 });

	// http://www.spec.org/power_ssj2008/results/res2012q1/power_ssj2008-20120306-00438.html
	// bestCPU/pow : 5467
	public static final NodeWithCons Huawei_RH2288_V2_E52670 = new NodeWithCons("Huawei_RH2288_V2_E52670", 16, 2600,
			32 * 1024, new double[] { 55.6, 79.4, 90.5, 102, 112, 128, 148, 172, 200, 238, 282 });
}

/**
 *
 */
package fr.emn.optiplace.power.powermodels.catalog;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.emn.optiplace.power.powermodels.LinearCPUCons;
import fr.emn.optiplace.power.powermodels.StepCPUCons;

/**
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com]2013
 * 
 */
public class Tools {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(Tools.class);

	@SuppressWarnings("unchecked")
	public static <T> T[] arr(T... objects) {
		if (objects == null) {
			return (T[]) new Object[] {};
		}
		return objects;
	}

	/**
	 * make an array of double
	 * 
	 * @param ds
	 *          the doubles to add
	 * @return at least an empty array, with ds if specified
	 */
	public static double[] darr(double... ds) {
		if (ds == null) {
			return new double[] {};
		}
		return ds;
	}

	/**
	 * find the (a, y0) params to make a linear regression of the (x,y) points
	 * using lowest square method
	 * 
	 * @param x
	 *          the x elements of the points
	 * @param y
	 *          the y elements of the points
	 * @return [y0, a] to match y=y0+a⋅x
	 */
	public static double[] linearRegressionSquare(double[] x, double[] y) {
		double[] ret = new double[2];
		assert x.length == y.length;
		int l = x.length;
		double avgx = 0;
		double avgy = 0;
		for (int i = 0; i < l; i++) {
			avgx += x[i];
			avgy += y[i];
		}
		avgx /= l;
		avgy /= l;
		double sumMultDiff = 0, sumSquareDeltaX = 0;
		for (int i = 0; i < l; i++) {
			sumMultDiff += (x[i] - avgx) * (y[i] - avgy);
			sumSquareDeltaX += (x[i] - avgx) * (x[i] - avgx);
		}
		ret[1] = sumMultDiff / sumSquareDeltaX;
		ret[0] = avgy - ret[1] * avgx;
		return ret;
	}

	/**
	 * convert a threshold-based model T(x) to a map-based model M(x)
	 * 
	 * @param minval
	 *          the value of T when x reach the lowest value (eg. -inf)
	 * @param thresholds
	 *          the threshold to modify T(x)
	 * @param vals
	 *          the value of T(x) associated to the thresholds of x
	 * @param minX
	 *          the min value of x
	 * @param maxX
	 *          the max value of x
	 * @return the map of [x][M(x)]
	 */
	public static double[][] thresholdsToMap(int minval, double[] thresholds, int[] vals, double minX, double maxX) {
		assert thresholds.length == vals.length;
		int l = vals.length;
		double[][] ret = new double[2][];
		ret[0] = new double[l + 1];
		ret[1] = new double[l + 1];
		if (l > 0) {
			ret[0][0] = 0.5 * (minX + thresholds[0]);
			ret[1][0] = minval;
		}
		for (int i = 1; i < l; i++) {
			ret[0][i] = 0.5 * (thresholds[i - 1] + thresholds[i]);
			ret[1][i] = 0.5 * (vals[i - 1] + vals[i]);
		}
		if (l > 0) {
			ret[0][l] = 0.5 * (maxX + thresholds[l - 1]);
			ret[1][l] = vals[l - 1];
		}
		return ret;
	}

	/**
	 * put the elements of the map in the arrays. ensure they are of same size.
	 * 
	 * @param map
	 *          the map containing the x->y vals
	 * @param x
	 *          the array to store the x
	 * @param y
	 *          the array to store the y
	 */
	public static void putMapInArrays(Map<Double, Integer> map, double[] x, double[] y) {
		assert map.size() == x.length && x.length == y.length;
		int pos = 0;
		for (Entry<Double, Integer> e : map.entrySet()) {
			x[pos] = e.getKey();
			y[pos] = e.getValue();
			pos++;
		}
	}

	public static final double[] base1LoadsThres = { 0.05, 0.15, 0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95 };

	/**
	 * 
	 * @param base1LoadCons
	 *          the consumption values associated to the load of 0, k, 2k, ..1
	 *          times the max CPU capa, with k=1/(length-1)
	 * @return a new consumption model based on the values
	 */
	public static StepCPUCons makeStepCPUConsFromObservations(double[] base1LoadCons) {
		int nbThres = base1LoadCons.length - 1;
		StepCPUCons ret = new StepCPUCons();
		int[] vals = new int[nbThres];
		double[] thres = new double[nbThres];
		int min = (int) base1LoadCons[0];
		double k = 1.0 / nbThres;
		for (int i = 0; i < nbThres; i++) {
			thres[i] = k * i + 0.5 * k;
			vals[i] = (int) base1LoadCons[i + 1];
		}
		ret.setSteps(min, thres, vals);
		return ret;
	}

	private static HashMap<Integer, double[]> base1steps = new HashMap<>();

	/**
	 * 
	 * @param nbVals
	 *          number of double to get
	 * @return [0, k, 2k...1], k=1/(nbvals-1)
	 */
	static double[] getBase1Steps(int nbVals) {
		double[] ret = base1steps.get(nbVals);
		if (ret == null) {
			ret = new double[nbVals];
			double k = 1.0 / (nbVals - 1);
			for (int i = 0; i < nbVals; i++) {
				ret[i] = k * i;
				// System.err.println("pos " + i + " : " + ret[i]);
			}
			base1steps.put(nbVals, ret);
		}
		return ret;
	}

	public static LinearCPUCons makeLinearCPUConsFromObservations(double[] base1LoadCons) {
		double[] params = linearRegressionSquare(getBase1Steps(base1LoadCons.length), base1LoadCons);
		LinearCPUCons ret = new LinearCPUCons(params[0], params[0] + params[1]);
		return ret;
	}
}

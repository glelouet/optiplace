
package fr.emn.optiplace.network;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import fr.emn.optiplace.configuration.VMLocation;
import fr.emn.optiplace.network.NetworkData.NetworkDataBridge;
import fr.emn.optiplace.network.NetworkData.VMCouple;
import fr.emn.optiplace.network.data.Link;
import fr.emn.optiplace.solver.choco.IReconfigurationProblem;
import fr.emn.optiplace.view.EmptyView;
import fr.emn.optiplace.view.annotations.Parameter;
import fr.emn.optiplace.view.annotations.ViewDesc;


@ViewDesc
public class NetworkView extends EmptyView {

	@SuppressWarnings("unused")
	private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(NetworkView.class);

	@Parameter(confName = "network")
	protected NetworkData data = new NetworkData();

	public NetworkData getData() {
		return data;
	}

	public void setData(NetworkData data) {
		this.data = data;
	}

	NetworkDataBridge bridge;

	@Override
	public void associate(IReconfigurationProblem rp) {
		super.associate(rp);
		bridge = data.bridge(rp.b());
		makeLinksUses();
	}

	@Override
	public void clear() {
		super.clear();
		bridge = null;
		vmCouple2Links = null;
		hoster2hoster2links = null;
		link2Use = null;
	}

	/**
	 * for each link idx, the use variable of this link
	 */
	IntVar[] link2Use = null;

	public void makeLinksUses() {
		if (link2Use != null) {
			return;
		}
		makeVMLinks();
		link2Use = new IntVar[bridge.nbLinks()];
		int[] couple2use = bridge.coupleUseArray();
		for (int linkIdx = 0; linkIdx < bridge.nbLinks(); linkIdx++) {
			Link l = bridge.link(linkIdx);
			IntVar[] coupleContainsLink = new IntVar[bridge.nbCouples()];
			for (int coupleIdx = 0; coupleIdx < coupleContainsLink.length; coupleIdx++) {
				BoolVar var = v.createBoolVar(bridge.vmCouple(coupleIdx) + "?uses(" + l + ")");
				coupleContainsLink[coupleIdx] = var;
				pb.getModel().member(v.createIntegerConstant(linkIdx), vmCouple2Links[coupleIdx]).reifyWith(var);
			}
			IntVar linkUse = v.scalar(coupleContainsLink, couple2use);
			try {
				linkUse.updateBounds(0, data.getCapacity(l), Cause.Null);
			}
			catch (ContradictionException e) {
				throw new UnsupportedOperationException(e);
			}
			link2Use[linkIdx] = linkUse;
		}
	}

	public IntVar getUse(Link l) {
		return getUse(bridge.link(l));
	}

	public IntVar getUse(int idx) {
		return idx == -1 || idx >= link2Use.length ? null : link2Use[idx];
	}

	/**
	 * for each idx of vm couple that has a network use, the variable constrained
	 * to the set of links required
	 */
	SetVar[] vmCouple2Links = null;

	/**
	 * create and fill the {@link #vmCouple2Links} if it is null.
	 */
	public void makeVMLinks() {
		if (vmCouple2Links != null) {
			return;
		}
		makeHosterCouples();
		// XXX we use a flatmat (one-dimension array representation) of the matrix,
		// we should use dedicated constraint
		int nbcols = hoster2hoster2links[0].length;
		SetVar[] flatMat = new SetVar[hoster2hoster2links.length * nbcols + 1];
		flatMat[0] = v.createEnumSetVar("noLink", new int[] {});
		// matrix[i][j]=flatmat[i*nbcols+j+1]
		// flamat[0] = empty set, case where a VM of the couple is not running
		for (int i = 0; i < hoster2hoster2links.length; i++) {
			assert hoster2hoster2links[i].length == nbcols : "hoster2hoster2links has invalid number of columns : "
					+ hoster2hoster2links[i].length + " on row " + i;
			for (int j = 0; j < nbcols; j++) {
				flatMat[i * nbcols + j + 1] = hoster2hoster2links[i][j];
			}
		}
		vmCouple2Links = new SetVar[bridge.nbCouples()];
		for (int i = 0; i < vmCouple2Links.length; i++) {
			VMCouple c = bridge.vmCouple(i);
			// if a vm is waiting, so is the other one.
			h.equality(pb.isWaiting(c.v0), pb.isWaiting(c.v1));
			vmCouple2Links[i] = v.createRangeSetVar(c.toString + ".links", 0, bridge.nbLinks() - 1);
			// idx on the array (when both VM are running)
			IntVar idxNonWaiting = v.plus(v.plus(v.mult(pb.getVMLocation(c.v0), nbcols), pb.getVMLocation(c.v1)), 1);
			// we check the state of
			IntVar flatMatIdx = v.createEnumIntVar(c.toString() + ".flatMatIdx", 0, flatMat.length);
			pb.switchState(c.v0, flatMatIdx, idxNonWaiting, idxNonWaiting, v.createIntegerConstant(0));
			post(pb.getModel().element(flatMatIdx, flatMat, 0, vmCouple2Links[i]));
		}
	}

	/** hoster i to hoster j => SetVar of links joining both. symetric */
	SetVar[][] hoster2hoster2links = null;

	/**
	 * create and fill the {@link #hoster2hoster2links}, if null.
	 */
	public void makeHosterCouples() {
		if (hoster2hoster2links != null) {
			return;
		}
		hoster2hoster2links = new SetVar[b.locations().length][];
		int[] empty = new int[] {};
		for (int i = 0; i < b.locations().length; i++) {
			hoster2hoster2links[i] = new SetVar[b.locations().length];
		}
		for (int i = 0; i < b.locations().length; i++) {
			VMLocation hi = b.location(i);
			hoster2hoster2links[i][i] = v.createFixedSet("path(" + hi.getName() + ").links", empty);
			for (int j = 0; j < i; j++) {
				VMLocation hj = b.location(j);
				int[] h2hLinks = bridge.links(i, j);
				SetVar links = v.createFixedSet("path(" + hi.getName() + "-" + hj.getName() + ").links", h2hLinks);
				hoster2hoster2links[i][j] = hoster2hoster2links[j][i] = links;
			}
		}
	}

	@Override
	public String toString() {
		return data.toString();
	}

}

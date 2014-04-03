package entropy.configuration.graphics;

import java.util.HashSet;

/**
 * a disposition places an Element, its target.<br />
 * The target touches a left and top positions, or null. They are the lAnchor
 * and tAnchor.<br />
 * 
 * @author guillaume
 */
public class Disposition {

	protected Disposition lAnchor = null, tAnchor = null;

	protected Element target = null;

	/** computed from anchors and their elements */
	protected int startX = 0, startY = 0;

	protected boolean clean = true;

	public HashSet<Disposition> anchored = new HashSet<Disposition>();

	public void setLAnchor(Disposition anchor) {
		if (this.lAnchor != null) {
			this.lAnchor.anchored.remove(this);
		}
		this.lAnchor = anchor;
		anchor.anchored.add(this);
		dirty();
	}

	public void setTAnchor(Disposition anchor) {
		if (this.tAnchor != null) {
			this.tAnchor.anchored.remove(this);
		}
		this.tAnchor = anchor;
		anchor.anchored.add(this);
		dirty();
	}

	public void setTarget(Element target) {
		this.target = target;
		dirty();
	}

	/** to call any time an internal modification is made */
	public void dirty() {
		clean = false;
		for (Disposition d : anchored) {
			d.dirty();
		}
	}

	/** recompute the internal positions, if not clean. */
	public void replace() {
		if (clean) {
			return;
		}
		if (tAnchor != null) {
			tAnchor.replace();
		}
		if (lAnchor != null) {
			lAnchor.replace();
		}
		startX = lAnchor.getStartX()
				+ (lAnchor.getTarget() != null ? lAnchor.getTarget().dX : 0);
		startY = tAnchor.getStartY()
				+ (tAnchor.getTarget() != null ? tAnchor.getTarget().dY : 0);
		clean = true;
	}

	/** @return */
	public Element getTarget() {
		return target;
	}

	/** @return */
	public int getStartX() {
		replace();
		return startX;
	}

	/** @return */
	public int getStartY() {
		replace();
		return startY;
	}

}
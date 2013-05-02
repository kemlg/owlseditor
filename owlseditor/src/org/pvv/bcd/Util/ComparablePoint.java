package org.pvv.bcd.Util;

import java.awt.Point;

public class ComparablePoint extends Point implements Comparable {

	public ComparablePoint() {
		super();
	}

	public ComparablePoint(Point p) {
		super(p);
	}

	public ComparablePoint(int x, int y) {
		super(x, y);
	}

	public String toString() {
		return "(" + x + "," + y + ")";
	}

	public int compareTo(Object o) {
		if (o instanceof ComparablePoint)
			return compareTo((ComparablePoint) o);
		return 0;
	}

	public int compareTo(ComparablePoint cp) {
		if (cp == null)
			return 1;
		if (this.x != cp.x) {
			// primary sort by X coord
			return this.x - cp.x;
		} else {
			// secondary sort by Y coord
			return this.y - cp.y;
		}
	}
}

package org.pvv.bcd.Util;

public class ComparableBoolean implements Comparable {
	public static ComparableBoolean TRUE = new ComparableBoolean(true);
	public static ComparableBoolean FALSE = new ComparableBoolean(false);

	private boolean m_b;

	public ComparableBoolean(boolean b) {
		m_b = b;
	}

	public ComparableBoolean(Boolean B) {
		m_b = B.booleanValue();
	}

	public Boolean toBool() {
		return m_b ? Boolean.TRUE : Boolean.FALSE;
	}

	public boolean booleanValue() {
		return m_b;
	}

	public int compareTo(Object ob) {
		boolean ob_bool;
		if (ob instanceof ComparableBoolean)
			ob_bool = ((ComparableBoolean) ob).booleanValue();
		else if (ob instanceof Boolean)
			ob_bool = ((Boolean) ob).booleanValue();
		else
			return 0;
		if (ob_bool == m_b)
			return 0;
		return m_b ? 1 : -1;
	}

	public boolean equals(Object ob) {
		return toBool().equals(ob);
	}

	public int hashCode() {
		return toBool().hashCode();
	}

	public String toString() {
		return m_b ? "yes" : "no";
	}
}

package org.pvv.bcd.Util;

public class MutableFloat extends FloatProxy {
	public void addFloat(float num) {
		m_f += num;
	}

	public void multiplyFloat(float mult) {
		m_f *= mult;
	}

	public MutableFloat() {
		super();
	}

	public MutableFloat(String pre) {
		super(pre);
	}

	public MutableFloat(String pre, String post) {
		super(pre, post);
	}

	public MutableFloat(float f) {
		super(f);
	}

	public MutableFloat(float f, int sd) {
		super(f, sd);
	}

	public MutableFloat(float f, int sd, String pre) {
		super(f, sd, pre);
	}

	public MutableFloat(float f, int sd, String pre, String post) {
		super(f, sd, pre, post);
	}

}

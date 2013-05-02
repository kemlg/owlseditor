//Title:        Polaris Marketer
//Version:
//Copyright:    Copyright (c) 1998
//Author:       Bent C Dalager
//Company:
//Description:  Analyses trade routes in Polaris

package org.pvv.bcd.Util;

import java.text.DecimalFormat;

public class FloatProxy extends Number implements Comparable {
	protected float m_f;
	private int m_sigDig;
	private String m_string;
	private String m_preString = null;
	private String m_postString = null;
	private boolean m_bShowNumber = true;

	private transient StringBuffer m_sb;

	public FloatProxy() {
		this(0.0f);
	}

	public FloatProxy(String pre) {
		this(0.0f, -1, pre, null);
		m_bShowNumber = false;
	}

	public FloatProxy(String pre, String post) {
		this(0.0f, -1, pre, post);
		m_bShowNumber = false;
	}

	public FloatProxy(float f) {
		this(f, -1, null, null);
	}

	public FloatProxy(float f, int sd) {
		this(f, sd, null, null);
	}

	public FloatProxy(float f, int sd, String pre) {
		this(f, sd, pre, null);
	}

	public FloatProxy(float f, int sd, String pre, String post) {
		m_f = f;
		m_sigDig = sd;
		m_string = null;
		m_preString = pre;
		m_postString = post;
	}

	public float floatValue() {
		return m_f;
	}

	public void setSigDig(int sd) {
		m_sigDig = sd;
		m_string = null;
	}

	public byte byteValue() {
		return new Float(m_f).byteValue();
	}

	public short shortValue() {
		return new Float(m_f).shortValue();
	}

	public int intValue() {
		return new Float(m_f).intValue();
	}

	public long longValue() {
		return new Float(m_f).longValue();
	}

	public double doubleValue() {
		return new Float(m_f).doubleValue();
	}

	public int compareTo(Object o) {
		if (!(o instanceof Number))
			return 0;
		Number n = (Number) o;
		if (m_f < n.floatValue())
			return -1;
		if (m_f > n.floatValue())
			return 1;
		return 0;
	}

	static public String toString(float f, int sign_digits) {
		FloatProxy fp = new FloatProxy(f, sign_digits);
		return fp.toString();
	}

	static public String toString(float f) {
		return toString(f, -1);
	}

	static public String toString(int i, int sign_digits) {
		return toString((float) i, sign_digits);
	}

	static public String toString(int i) {
		return toString(i, -1);
	}

	public String toString() {
		int i;

		if (m_string != null)
			return m_string;

		if (!m_bShowNumber) {
			m_string = "";
			if (m_preString != null)
				m_string += m_preString;
			if (m_postString != null)
				m_string += m_postString;
			return m_string;
		}

		long l = (long) m_f;
		int digits = Long.toString(l).length();
		int tokeep = m_sigDig - digits;
		if (tokeep < 0)
			tokeep = 0;
		DecimalFormat formater;
		switch (tokeep) {
		case 0:
			formater = m_sDefaultFormat0;
			break;
		case 1:
			formater = m_sDefaultFormat1;
			break;
		case 2:
			formater = m_sDefaultFormat2;
			break;
		case 3:
			formater = m_sDefaultFormat3;
			break;
		case 4:
			formater = m_sDefaultFormat4;
			break;
		default:
			if (m_sb == null)
				m_sb = new StringBuffer(20);
			m_sb.setLength(0);
			m_sb.append(m_strFormatPrefix);
			m_sb.append('.');
			int j = tokeep;
			while (j-- > 0)
				m_sb.append('0');
			formater = new DecimalFormat(m_sb.toString());
			break;
		}
		m_sb = new StringBuffer(formater.format(m_f));

		/*
		 * if (m_sigDig != -1) { // Finite precision
		 * 
		 * // Find location of decimal sign for (i=0; i < m_sb.length(); ++i) if
		 * (m_sb.charAt(i) == '.') break;
		 * 
		 * int decimal_sign_location = i;
		 * 
		 * if (decimal_sign_location!=m_sb.length()) { // Has decimal places,
		 * check how many to keep etc.
		 * 
		 * int digits_to_keep = m_sigDig; // Now find how many digits to the
		 * right of the decimal sign to keep digits_to_keep -=
		 * decimal_sign_location; if (m_f<1) { // Disregard leading zeros in the
		 * calculation of digits to keep ++digits_to_keep; // The zero to the
		 * right of the decimal sign int dig_cnt = decimal_sign_location+1;
		 * while ( (dig_cnt<m_sb.length()) && (m_sb.charAt(dig_cnt)=='0') ) {
		 * ++digits_to_keep; ++dig_cnt; } } if (digits_to_keep < 0)
		 * digits_to_keep = 0;
		 * 
		 * // Calculate new length of string int new_length; if (digits_to_keep
		 * <= 0) // Always keep all digits to the left of the decimal sign
		 * new_length = decimal_sign_location; else // Remember to account for
		 * the decimal sign ('+1') new_length = decimal_sign_location + 1 +
		 * digits_to_keep;
		 * 
		 * // Round off to the lowest shown digit float new_f = m_f *
		 * (float)Math.pow(10.0,(double)digits_to_keep); new_f =
		 * (float)Math.round(new_f); new_f = new_f /
		 * (float)Math.pow(10.0,(double)digits_to_keep);
		 * 
		 * m_sb = new StringBuffer(Float.toString(new_f));
		 * 
		 * if (new_length<m_sb.length()) m_sb.setLength(new_length); } }
		 */
		// Put in commas for each 3 digits
		// (i.e., 1000000.0001 becomes 1,000,000.0001)
		/*
		 * for (i=0; i < m_sb.length(); ++i) if (m_sb.charAt(i) == '.') break;
		 * int insert_at = i-3; boolean is_negative = (m_f<0); while ( insert_at
		 * > 0 && ((!is_negative) || (insert_at > 1)) ) { m_sb.insert(insert_at,
		 * ','); insert_at -= 3; }
		 */
		if (m_preString != null)
			m_sb.insert(0, m_preString);
		if (m_postString != null)
			m_sb.append(m_postString);
		m_string = m_sb.toString();
		return m_string;
	}

	private final static String m_strFormatPrefix = "###,###,##0";
	private final static DecimalFormat m_sDefaultFormat0 = new DecimalFormat(
			m_strFormatPrefix);
	private final static DecimalFormat m_sDefaultFormat1 = new DecimalFormat(
			m_strFormatPrefix + ".0");
	private final static DecimalFormat m_sDefaultFormat2 = new DecimalFormat(
			m_strFormatPrefix + ".00");
	private final static DecimalFormat m_sDefaultFormat3 = new DecimalFormat(
			m_strFormatPrefix + ".000");
	private final static DecimalFormat m_sDefaultFormat4 = new DecimalFormat(
			m_strFormatPrefix + ".0000");
	private final static DecimalFormat m_sDefaultFormat5 = new DecimalFormat(
			m_strFormatPrefix + ".00000");
}

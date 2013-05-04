package org.pvv.bcd.instrument.JTree;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This is the unique drag and drop id that is used for transfering node data
 * around. It contains some black magic that needs a bit of memory jogging to
 * properly document. This will happen in due time. Feel free to nag about it if
 * you really need/want it.
 */
public class DndId implements Serializable {
	private long m_nInstrumentId;
	private long m_nOperationId;

	public DndId(long instrument, long operation) {
		m_nInstrumentId = instrument;
		m_nOperationId = operation;
	}

	public long getInstrumentId() {
		return m_nInstrumentId;
	}

	public long getOperationId() {
		return m_nOperationId;
	}

	/**
	 * Serialisation, delegates to ObjectOutputStream.
	 */
	void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	/**
	 * Serialisation, delegates to ObjectOutputStream.
	 */
	void readObject(ObjectInputStream ois) throws ClassNotFoundException,
			IOException {
		ois.defaultReadObject();
	}

}
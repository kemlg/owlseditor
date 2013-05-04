package org.pvv.bcd.instrument.JTree;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * This is the default transfer info that is used for transfering node data
 * around. It contains some black magic that needs a bit of memory jogging to
 * properly document. This will happen in due time. Feel free to nag about it if
 * you really need/want it.
 */
public class DefaultTransferInfo implements Serializable {

	private SubTreeNode[] m_arrNodes;
	private DndId m_dndId;

	public DefaultTransferInfo(SubTreeNode[] nodes, DndId dndId) {
		m_arrNodes = nodes;
		m_dndId = dndId;
	}

	public DndId getDndId() {
		return m_dndId;
	}

	public SubTreeNode[] getNodes() {
		return m_arrNodes;
	}

	/**
	 * Serialisation, delegates to ObjectOutputStream.
	 */
	void writeObject(ObjectOutputStream oos) throws IOException {
		System.out.println("DefaultTransferInfo.writeObject()");
		oos.defaultWriteObject();
	}

	/**
	 * Serialisation, delegates to ObjectOutputStream.
	 */
	void readObject(ObjectInputStream ois) throws ClassNotFoundException,
			IOException {
		System.out.println("DefaultTransferInfo.readObject()");
		ois.defaultReadObject();
	}

}
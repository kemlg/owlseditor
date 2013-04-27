package org.pvv.bcd.instrument.JTree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import com.sri.owlseditor.cmp.tree.OWLSDataFlavorProvider;

/**
 * This is the default transferable that is used for transfering node
 * data around. It contains some black magic that needs a bit of memory
 * jogging to properly document. This will happen in due time. Feel free
 * to nag about it if you really need/want it.
 */
public class DefaultTransferable
   implements Transferable
{
   /**
    * Our very own custom data flavour, useful for transferring between
    * instrumenters. Not useful for transferring data to other software.
    */
   
	final public static DataFlavor DEFAULT_NODE_INFO_FLAVOUR =
   			  	new DataFlavor(
   			         DefaultNodeInfo.class,
   			         "Default Node Info");
	
   /**
    * <p>
    * List of all data flavours we support. This is initialized statically.
    * </p>
    * <p>
    * On last count, we supported:
    * <ul>
    * <li>DEFAULT_NODE_INFO_FLAVOUR</li>
    * <li>DataFlavor.stringFlavor</li>
    * <li>DataFlavor.javaFileListFlavor (only works for input, not output)</li>
    * <li>text/plain; charset=UTF-16BE</li>
    * <li>text/plain; charset=UTF-16LE</li>
    * <li>text/plain; charset=UTF-16</li>
    * <li>text/plain; charset=ISO-8859-1</li>
    * <li>text/plain; charset=UTF-8</li>
    * <li>text/plain; charset=US-ASCII</li>
    * <li>text/plain; charset=ascii</li>
    * </ul>
    * </p>
    */
	private static DataFlavor m_flavours[];

   static
   {
      Vector v = new Vector();
      v.addElement(DEFAULT_NODE_INFO_FLAVOUR);
      /*
      v.addElement(DataFlavor.stringFlavor);
      v.addElement(DataFlavor.javaFileListFlavor);
      addMimeType(v, "text/plain; charset=UTF-16BE");
      addMimeType(v, "text/plain; charset=UTF-16LE");
      addMimeType(v, "text/plain; charset=UTF-16");
      addMimeType(v, "text/plain; charset=ISO-8859-1");
      addMimeType(v, "text/plain; charset=UTF-8");
      addMimeType(v, "text/plain; charset=US-ASCII");
      addMimeType(v, "text/plain; charset=ascii");
      */
      m_flavours = new DataFlavor[v.size()];
      v.copyInto(m_flavours);
   }

   
   /**
    * Utility method to add data flavours to a vector. Will not add them
    * if they turn out to be invalid.
    *
    * @param v Vector to add to
    * @param mimetype name of mime type to add
    */
   protected static void addMimeType(Vector v, String mimetype)
   {
      try
      {
         v.addElement(new DataFlavor(mimetype));
      }
      catch (ClassNotFoundException ex)
      {
         ex.printStackTrace();
      }
   }

   private DefaultTransferInfo m_data;

   public DefaultTransferable(DefaultMutableTreeNode node, DndId dndId)
   {
   	m_data = new DefaultTransferInfo(
   			new SubTreeNode[]{new SubTreeNode(node, true)},
			dndId);
   }

   public DefaultTransferable(DefaultMutableTreeNode[] nodes, DndId dndId)
   {
   	SubTreeNode[] stn = new SubTreeNode[nodes.length];
   	for (int i=0; i<nodes.length; i++)
   		stn[i] = new SubTreeNode(nodes[i], true);
   	m_data = new DefaultTransferInfo(
   			stn,
			dndId);
   }

   public DataFlavor[] getTransferDataFlavors()
   {
      return m_flavours;
   }

   public Object getTransferData(DataFlavor flavor)
      throws UnsupportedFlavorException, IOException
   {
      Object retval;
      if (flavor.equals(OWLSDataFlavorProvider.getInstance().getOWLSDataFlavor())){
        //System.out.println("DefaultTransferable.getTransferData() returning " +
		//					"object " + m_data + " of data flavor OWLSDataFlavor");
        retval = m_data;
      }
      else if (flavor.equals(DEFAULT_NODE_INFO_FLAVOUR))
      {
        //System.out.println("DefaultTransferable.getTransferData() returning " +
        //					"object " + m_data + " of default data flavor");
      	retval = m_data;
      }
      else if (flavor.equals(DataFlavor.stringFlavor))
      {
         StringBuffer sbuff = new StringBuffer();
         for (int i=0; i<m_data.getNodes().length; ++i)
            sbuff.append(m_data.getNodes()[i].toString());
         retval = sbuff.toString();
      }
      else if (flavor.isMimeTypeEqual("text/plain"))
      {
         ByteArrayOutputStream os = new ByteArrayOutputStream();
         OutputStreamWriter osw =
            new OutputStreamWriter(os, flavor.getParameter("charset"));
         PrintWriter wr = new PrintWriter(osw);
         for (int i=0; i<m_data.getNodes().length; ++i)
            wr.println(m_data.getNodes()[i].toString());
         wr.flush();
         wr.close();
         retval = new ByteArrayInputStream(os.toByteArray());
      }
      else if (flavor.equals(DataFlavor.javaFileListFlavor))
      {
         File dir = File.createTempFile("paste", "");
         dir.mkdir();
         dir.deleteOnExit();
         List list = new ArrayList();
         for (int i=0; i<m_data.getNodes().length; ++i)
            list.add(m_data.getNodes()[i].toFile(dir));
         retval = list;
      }
      else
      {
         throw new UnsupportedFlavorException(flavor);
      }
      return retval;
   }

   public boolean isDataFlavorSupported(DataFlavor flavor)
   {
      boolean retval = false;
      for (int i=0; i<m_flavours.length; ++i)
         if (flavor.equals(m_flavours[i]))
         {
            retval = true;
            break;
         }
      return retval;
   }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wonderly.netbeans.perforce;


import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Date;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.Document;
import org.wonderly.awt.Packer;

/**
 *
 * @author gregg
 */
public class Inspect extends JFrame {
	Logger log = Logger.getLogger(getClass().getName());
	public static void main( String args[] ) throws IOException, InterruptedException {
		new Inspect(new String[]{ "C:/perforce/cyte/Products/Agency/main/src/diffs.out" } );
	}
	public Inspect( String args[] ) throws IOException, InterruptedException {
		super( "Inspection View of "+args[0] );
		log = Logger.getLogger(getClass().getName());
		File tf = File.createTempFile( "inspectpre", ".html");
		File inf = new File( args[0] );
		FileReader fr = new FileReader( inf );
		BufferedReader rd = new BufferedReader( fr );
		FileWriter wr = new FileWriter( tf );
		PrintWriter pw = new PrintWriter( wr );
		String str;
		boolean lower = false;
		String last = "";
		int lnk = 0;
		int flnk = 0;
		String infront = "", behind = "";
		String top = "<ol>\n";
		while( ( str = rd.readLine() ) != null ) {
			if( str.length() == 0 )
				str =" ";
			char c = str.charAt(0);
			String type="";
			if( c == ' ' || c == '\t' ) {
				type="<Div style=\"background:#cfcfcf\">";
			} else if( str.startsWith("---") || str.startsWith("*** ") ) {
				lower = str.startsWith("---");
				behind = "<a href=\"#_link"+(lnk)+"\">&lt;Next&gt; - Change #"+(lnk+1)+"</a>";
				infront = "<a href=\"#_link"+(lnk-2)+"\">&lt;Prev&gt;</a>";
				type = "<Div style=\"background:#9f9fff\">";
			} else if( str.startsWith("******") ) {
				lower = false;
				type="<Div style=\"background:#5f5fff\">";
				infront = "<a name=\"_link"+lnk+"\">Change #"+(lnk+1)+"</a><br>";
				++lnk;
			} else if( c == '-' ) {
				type="<Div style=\"background:#ff2f2f;color:#ffffff\">";
			} else if( c == '+' ) {
				type="<Div style=\"background:#1fff1f\">";
			} else if( c == '!' ) {
				if( lower )
					type="<Div style=\"background:#ff9090;color:#000000\">";
				else
					type="<Div style=\"background:#ffff5f;color:#000000\">";
			} else if( str.startsWith("====" ) || str.startsWith("}====")) {
				String arr[] = str.split(" ");
				String fn = "";
				boolean add = false;
				behind = "</a><br><a href=\"#_file"+(flnk+1)+"\">&lt;Next File&gt; - File #"+(flnk+2)+"</a>";
				infront = "<a href=\"#_file"+(flnk-1)+"\">&lt;Prev File&gt;</a><br>File #"+(flnk+1)+": <a href=\"#__INDEX__\">";
				for( String n : arr ) {
					if( n.equals("-") ) {
						add = true;
					} else if( add && n.equals("====") == false && n.equals("}====") == false) {
						if( fn.equals("") == false )
							fn += " ";
						fn += n;
					}
				}

				String tfn = fn.replace("\\","_").replace("/","_").replace(":","_");
				type = "<hr size=\"5\">" +
						"<a name=\"_file"+flnk+"\">" +
						"</a><a name=\""+tfn+"\"></a>\n"+"<Div style=\"color:#ffffff;background:#000000\">";
				top += "<li><a href=\"#"+tfn+"\">"+fn+"</a>\n";
				flnk++;
			}
			str = str.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
			//str = str.replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
			if( last.equals(type) ) {
				pw.println(infront + str + behind ); //+"<br>");
			} else {
				if( last.equals("") == false )
					pw.println("</Div>");
				pw.println( type + infront + str + behind ); //+"<br>" );
				last = type;
			}
			infront = behind = "";
		}
		pw.println("</Div>");
		pw.close();
		rd.close();
		rd = new BufferedReader( new FileReader( tf ) );
		File otf = File.createTempFile( "inspect", ".html");
		wr = new FileWriter( otf );
		pw = new PrintWriter( wr );

		pw.println("<html><head><title>Differences - Generated "+new Date()+"</title></head><body>" +
			"<a name=\"_file-1\"></a>" +
			"<h1>Diffs from "+args[0]+"</h1><hr size=\"3\">"+
//			"<a name=\"_file"+flnk+"\">" +
				"<h1>Index to File <a href=\"#__diffs\">Diffs</a></h1><hr>" +
				"<a name=\"__INDEX__\">\n"+top+"</ol>"+
			//"<ul><li><a href=\"#__INDEX__\">Index to file diffs</a></li></ul>" +
			"<hr><a name=\"__diffs\"><h1>Diffs</h1><pre>");

		while( (str = rd.readLine()) != null ) {
			pw.println( str );
		}

		pw.println("</pre><hr size=\"5\"><a name=\"_file"+flnk+"\"></a><h2>End Of <a href=\"#__INDEX__\">Diffs</a> - Generated "+new Date()+"</h2><hr>");//+top+"</ol><hr>");
			pw.println("</body></html>");
		rd.close();
		pw.close();

//		Process p = Runtime.getRuntime().exec( new String[]{ "cmd", "/c", "start "+ otf.toString()} );
//		p.waitFor();
//		System.exit(0);
		final JEditorPane pn = new JEditorPane("file:/"+tf.toString());
		Packer pk = new Packer( getContentPane() );
		pk.pack( new JScrollPane( pn ) ).fillboth();
		pack();
		pn.setEditable(false);
		HyperlinkListener hl = new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
				HyperlinkEvent.EventType type = hyperlinkEvent.getEventType();
				final URL url = hyperlinkEvent.getURL();
				if (type == HyperlinkEvent.EventType.ENTERED) {
					log.info("URL: " + url);
				} else if (type == HyperlinkEvent.EventType.ACTIVATED) {
					log.info("Activated");
					Runnable runner = new Runnable() {
						public void run() {
							// Retain reference to original
							Document doc = pn.getDocument();
							try {
								pn.setPage(url);
							} catch (IOException ioException) {
								JOptionPane.showMessageDialog(Inspect.this,
									"Error following link", "Invalid link",
								JOptionPane.ERROR_MESSAGE);
								pn.setDocument(doc);
							}
						}
					};
					SwingUtilities.invokeLater(runner);
				}
			}
		};
		pn.addHyperlinkListener( hl );
//		pn.setText("<html>" +
//				"<body>" +
//				"<ul>" +
//				"<li><a href=\"#a\">1</a>" +
//				"</ul>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"1<br>" +
//				"<a name=\"1\">1</a><br>" +
//				"</body>" +
//				"</html>");
		setLocationRelativeTo( null );
		setVisible(true);
		addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent ev) {
				System.exit(0);
			}
		});
	}
}

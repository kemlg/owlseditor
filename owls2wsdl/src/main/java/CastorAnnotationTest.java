/*
 * CastorAnnotationTest.java
 *
 * Created on 5. März 2007, 11:36
 */
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.Writer;
import org.exolab.castor.xml.schema.writer.SchemaWriter;
import org.exolab.castor.xml.schema.*;
import org.exolab.castor.util.LocalConfiguration;
import java.util.Enumeration;


/**
 * Test class included in a bug report for <b>Castor project</b>.<br />
 * <a href="http://jira.codehaus.org/browse/CASTOR-1895">http://jira.codehaus.org/browse/CASTOR-1895</a><br />
 * <a href="http://www.castor.org/release-notes.html">http://www.castor.org/release-notes.html</a>
 */
public class CastorAnnotationTest {
    
    public static void printXSD(Schema schema, OutputStream out) 
    {   
        try {
            Writer writer = new OutputStreamWriter(out, "UTF-8");                
            LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "true");
            SchemaWriter sw = new SchemaWriter(writer);
            sw.write(schema);
            LocalConfiguration.getInstance().getProperties().setProperty("org.exolab.castor.indent", "false");
        }
        catch(java.io.UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        catch(java.io.IOException e) {
            e.printStackTrace();            
        }
        catch(org.xml.sax.SAXException e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        Schema schema = new Schema("xsd",Schema.DEFAULT_SCHEMA_NS);
        
        Annotation currentSchemaAnnotation = new Annotation();
        for(int i=1; i<=3; i++) {
            Documentation doc = new Documentation();
            doc.setSource("INFO "+i);
            currentSchemaAnnotation.addDocumentation(doc);
        }
        schema.addAnnotation(currentSchemaAnnotation);
        printXSD(schema, System.out);
        
        Enumeration docEnumeration = currentSchemaAnnotation.getDocumentation();
        while(docEnumeration.hasMoreElements()) {
            System.out.println("- "+ ((Documentation)docEnumeration.nextElement()).getSource());
        }
    }
}

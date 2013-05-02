package com.sri.owlseditor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.sri.owlseditor.widgets.ParamTypeWidget;
import com.sri.owlseditor.widgets.dataflow.HasDataFromWidget;
import com.sri.owlseditor.widgets.dataflow.ProducedBindingWidget;
import com.sri.owlseditor.widgets.swrl.SWRLWidget;
import com.sri.owlseditor.widgets.wsdl.WSDLInputWidget;
import com.sri.owlseditor.widgets.wsdl.WSDLOutputWidget;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.widget.WidgetMapper;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetMapper;

public class OWLSEditorWidgetMapper implements WidgetMapper {
	private WidgetMapper dwm;
	private Project prj;
	private HashMap<String, Class<?>>	table;

	public OWLSEditorWidgetMapper(Project prj, OWLModel om) {
		System.out.println("Creating OWLSEditor Widget Mapper...");
		this.prj = prj;
		dwm = new OWLWidgetMapper(om);
		table = new HashMap<String, Class<?>>();
		table.put(
				"http://www.daml.org/services/owl-s/1.2/Process.owl#parameterType",
				ParamTypeWidget.class);
		table.put(
				"http://www.daml.org/services/owl-s/1.2/Expression.owl#expressionObject",
				SWRLWidget.class);
		table.put(
				"http://www.daml.org/services/owl-s/1.2/Process.owl#hasDataFrom",
				HasDataFromWidget.class);
		table.put(
				"http://www.daml.org/services/owl-s/1.2/Process.owl#producedBinding",
				ProducedBindingWidget.class);
		table.put(
				"http://www.daml.org/services/owl-s/1.2/Grounding.owl#wsdlInput",
				WSDLInputWidget.class);
		table.put(
				"http://www.daml.org/services/owl-s/1.2/Grounding.owl#wsdlOutput",
				WSDLOutputWidget.class);
	}

	public WidgetDescriptor createWidgetDescriptor(Cls arg0, Slot arg1,
			Facet arg2) {
		WidgetDescriptor wd;

//		System.out.println("createWidgetDescriptor(" + arg0 + ", " + arg1
//				+ ", " + arg2 + ")");
		wd = dwm.createWidgetDescriptor(arg0, arg1, arg2);
		Class<?> match = table.get(arg1.getName());
		if (match != null) {
			System.out.println("We found a match!");
			wd.setWidgetClassName(match.getName());
		}

		return wd;
	}

	public String getDefaultWidgetClassName(Cls arg0, Slot arg1, Facet arg2) {
//		System.out.println("getDefaultWidgetClassName(" + arg0 + ", " + arg1
//				+ ", " + arg2 + ")");
		String	wd;
		
		wd = dwm.getDefaultWidgetClassName(arg0, arg1, arg2);
		Class<?> match = table.get(arg1.getName());
		if (match != null) {
			System.out.println("We found a match!");
			wd = match.getName();
		}

		return wd;
	}

	public Collection getSuitableWidgetClassNames(Cls arg0, Slot arg1,
			Facet arg2) {
//		System.out.println("getSuitableWidgetClassNames(" + arg0 + ", " + arg1
//				+ ", " + arg2 + ")");
		Collection	wd;
		
		wd = dwm.getSuitableWidgetClassNames(arg0, arg1, arg2);
		Class<?> match = table.get(arg1.getName());
		if (match != null) {
			System.out.println("We found a match!");
			wd = new ArrayList();
			wd.add(match.getName());
		}

		return wd;
	}

	public boolean isSuitableWidget(Cls arg0, Slot arg1, Facet arg2,
			WidgetDescriptor arg3) {
		System.out.println("isSuitableWidget(" + arg0 + ", " + arg1 + ", "
				+ arg2 + ", " + arg3 + ")");
		boolean	wd;
		
		wd = dwm.isSuitableWidget(arg0, arg1, arg2, arg3);
		Class<?> match = table.get(arg1.getName());
		if (match != null) {
			System.out.println("We found a match!");
			wd = match.getName().equals(arg3.getWidgetClassName());
		}

		return wd;
	}

}

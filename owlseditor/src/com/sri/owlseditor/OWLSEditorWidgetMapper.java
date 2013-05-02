package com.sri.owlseditor;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;

import com.sri.owlseditor.widgets.ParamTypeWidget;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Facet;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.WidgetDescriptor;
import edu.stanford.smi.protege.widget.WidgetMapper;
import edu.stanford.smi.protegex.owl.model.OWLModel;
import edu.stanford.smi.protegex.owl.ui.widget.OWLWidgetMapper;

public class OWLSEditorWidgetMapper implements WidgetMapper {
	private WidgetMapper				dwm;
	private Project						prj;
	private HashMap<String,Class<?>>	table;
	
	public OWLSEditorWidgetMapper(Project prj, OWLModel om) {
		System.out.println("Creating OWLSEditor Widget Mapper...");
		this.prj = prj;
		dwm = new OWLWidgetMapper(om);
		table = new HashMap<String,Class<?>>();
		table.put("http://www.daml.org/services/owl-s/1.2/Process.owl#parameterType", ParamTypeWidget.class);
	}

	public WidgetDescriptor createWidgetDescriptor(Cls arg0, Slot arg1,
			Facet arg2) {
		WidgetDescriptor	wd;
		
		System.out.println("createWidgetDescriptor(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
		wd = dwm.createWidgetDescriptor(arg0, arg1, arg2);
		Class<?> match = table.get(arg1.getName());
		if(match != null)
		{
			System.out.println("We found a match!");
			wd.setWidgetClassName("com.sri.owlseditor.widgets.ParamTypeWidget");
		}
		
		return wd;
	}

	public String getDefaultWidgetClassName(Cls arg0, Slot arg1, Facet arg2) {
		System.out.println("getDefaultWidgetClassName(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
		return dwm.getDefaultWidgetClassName(arg0, arg1, arg2);
	}

	public Collection getSuitableWidgetClassNames(Cls arg0, Slot arg1,
			Facet arg2) {
		System.out.println("getSuitableWidgetClassNames(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
		return dwm.getSuitableWidgetClassNames(arg0, arg1, arg2);
	}

	public boolean isSuitableWidget(Cls arg0, Slot arg1, Facet arg2,
			WidgetDescriptor arg3) {
		System.out.println("isSuitableWidget(" + arg0 + ", " + arg1 + ", " + arg2 + ")");
		return dwm.isSuitableWidget(arg0, arg1, arg2, arg3);
	}

}

/*
 * ProjectServiceDependencyTypesModel.java
 *
 */

package de.dfki.dmas.owls2wsdl.gui.models;

import de.dfki.dmas.owls2wsdl.gui.RuntimeModel;
import de.dfki.dmas.owls2wsdl.core.Project;
import javax.swing.AbstractListModel;

/**
 *
 * @author cosmic
 */
public class ProjectServiceDependencyTypesModel_obsolete extends AbstractListModel
{
    public Object getElementAt(int index) {
        return RuntimeModel.getInstance().getProject().getServiceDependencyTypes().get(index);
    }

    public int getSize() {
        return RuntimeModel.getInstance().getProject().getServiceDependencyTypes().size();
    }    
}

/*
 * ProjectServiceMissingTypesModel.java
 *
 * Created on 20. März 2007, 10:20
 */

package de.dfki.dmas.owls2wsdl.gui.models;

import de.dfki.dmas.owls2wsdl.gui.RuntimeModel;
import de.dfki.dmas.owls2wsdl.core.Project;
import javax.swing.AbstractListModel;

/**
 *
 * @author Oliver
 */
public class ProjectServiceMissingTypesModel_obsolete extends AbstractListModel
{
    public Object getElementAt(int index) {
        return RuntimeModel.getInstance().getProject().getServiceMissingTypes().get(index);
    }

    public int getSize() {
        return RuntimeModel.getInstance().getProject().getServiceMissingTypes().size();
    }   
}

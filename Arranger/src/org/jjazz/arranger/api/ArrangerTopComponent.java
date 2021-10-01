/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 *  Copyright @2019 Jerome Lelasseux. All rights reserved.
 *
 *  This file is part of the JJazzLabX software.
 *   
 *  JJazzLabX is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License (LGPLv3) 
 *  as published by the Free Software Foundation, either version 3 of the License, 
 *  or (at your option) any later version.
 *
 *  JJazzLabX is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JJazzLabX.  If not, see <https://www.gnu.org/licenses/>
 * 
 *  Contributor(s): 
 */
package org.jjazz.arranger.api;

import org.jjazz.arranger.ArrangerPanel;
import org.jjazz.util.api.ResUtil;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.jjazz.arrangermode.api//Arranger//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ArrangerTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "jlnavigator", openAtStartup = false)
@ActionID(category = "Window", id = "org.jjazz.arrangermode.api.ArrangerTopComponent")
@ActionReference(path = "Menu/Window", position = 5, separatorAfter = 7)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ArrangerAction",
        preferredID = "ArrangerTopComponent"
)
public final class ArrangerTopComponent extends TopComponent
{
    private ArrangerPanel arrangerPanel;
    public ArrangerTopComponent()
    {
        setName(ResUtil.getString(getClass(), "CTL_ArrangerTopComponent"));
        setToolTipText(ResUtil.getString(getClass(), "HINT_ArrangerTopComponent"));
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);


        initComponents();

        arrangerPanel = new ArrangerPanel();
        add(arrangerPanel);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.LINE_AXIS));
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened()
    {
        arrangerPanel.opened();
    }

    @Override
    public void componentClosed()
    {
        arrangerPanel.closing();
    }
    
    public ArrangerPanel getArrangerPanel()
    {
        return arrangerPanel;
    }

    /**
     *
     * @return Can be null
     */
    static public ArrangerTopComponent getInstance()
    {
        return (ArrangerTopComponent) WindowManager.getDefault().findTopComponent("ArrangerTopComponent");
    }

    void writeProperties(java.util.Properties p)
    {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p)
    {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}

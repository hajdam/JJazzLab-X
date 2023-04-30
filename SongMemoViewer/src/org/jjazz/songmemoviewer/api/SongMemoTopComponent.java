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
package org.jjazz.songmemoviewer.api;

import java.util.logging.Logger;
import org.jjazz.songmemoviewer.SongMemoEditor;
import org.jjazz.util.api.ResUtil;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.jjazz.songnotesviewer.api//SongNotes//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "SongNotesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false, position = 500)
@ActionID(category = "Window", id = "org.jjazz.songnotesviewer.api.SongNotesTopComponent")
@ActionReference(path = "Menu/Window", position = 15, separatorAfter = 200)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SongNotesAction",
        preferredID = "SongNotesTopComponent"
)
public final class SongMemoTopComponent extends TopComponent
{
    
    private final SongMemoEditor editor;
    private static final Logger LOGGER = Logger.getLogger(SongMemoTopComponent.class.getSimpleName());    
    
    public SongMemoTopComponent()
    {
        initComponents();
        setName(ResUtil.getString(getClass(), "CTL_SongNotesTopComponent"));
        setToolTipText(ResUtil.getString(getClass(), "CTL_SongNotesTopComponentDesc"));
        putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.FALSE);
        putClientProperty(TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DND_COPY_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.FALSE);
        putClientProperty(TopComponent.PROP_SLIDING_DISABLED, Boolean.FALSE);
        editor = new SongMemoEditor();
        add(editor);
        
    }

    /**
     *
     * @return Can be null
     */
    static public SongMemoTopComponent getInstance()
    {
        return (SongMemoTopComponent) WindowManager.getDefault().findTopComponent("SongNotesTopComponent");
    }
    
    @Override
    public UndoRedo getUndoRedo()
    {
        return editor.getUndoManager();
    }
    
    @Override
    public Lookup getLookup()
    {
        return editor.getLookup();
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
        // TODO add custom code on component opening
        // LOGGER.severe("componentOpened() ");
    }
    
    @Override
    public void componentClosed()
    {
        // LOGGER.severe("componentClosed() ");
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

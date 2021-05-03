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
package org.jjazz.ui.ss_editor.actions;

import org.jjazz.ui.ss_editor.api.SS_ContextActionSupport;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.ACCELERATOR_KEY;
import static javax.swing.Action.NAME;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jjazz.rhythm.api.Rhythm;
import org.jjazz.rhythm.api.RhythmParameter;
import org.jjazz.songstructure.api.SongPart;
import org.jjazz.ui.ss_editor.api.SS_EditorTopComponent;
import org.jjazz.ui.ss_editor.api.SS_SelectionUtilities;
import org.jjazz.undomanager.JJazzUndoManager;
import org.jjazz.undomanager.JJazzUndoManagerFinder;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.jjazz.songstructure.api.SongStructure;
import org.jjazz.songstructure.api.SongPartParameter;
import org.jjazz.ui.ss_editor.api.RpValueCopyBuffer;
import org.jjazz.ui.ss_editor.api.SS_ContextActionListener;
import static org.jjazz.ui.utilities.Utilities.getGenericControlKeyStroke;
import org.jjazz.util.ResUtil;

/**
 * Paste RhythmParameter values.
 * <p>
 */
@ActionID(category = "JJazz", id = "org.jjazz.ui.ss_editor.actions.pasterpvalue")
@ActionRegistration(displayName = "#CTL_PasteRpValue", lazy = false)
@ActionReferences(
        {
            @ActionReference(path = "Actions/RhythmParameter", position = 30, separatorAfter = 31),
        })
public class PasteRpValue extends AbstractAction implements ContextAwareAction, SS_ContextActionListener, ChangeListener
{

    private Lookup context;
    private SS_ContextActionSupport cap;
    private String undoText = ResUtil.getString(getClass(), "CTL_PasteRpValue");

    public PasteRpValue()
    {
        this(Utilities.actionsGlobalContext());
    }

    private PasteRpValue(Lookup context)
    {
        this.context = context;
        cap = SS_ContextActionSupport.getInstance(this.context);
        cap.addListener(this);
        putValue(NAME, undoText);
        putValue(ACCELERATOR_KEY, getGenericControlKeyStroke(KeyEvent.VK_V));
        RpValueCopyBuffer buffer = RpValueCopyBuffer.getInstance();
        buffer.addChangeListener(this);
        selectionChange(cap.getSelection());
    }

    @Override
    public Action createContextAwareInstance(Lookup context)
    {
        return new PasteRpValue(context);
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        SS_SelectionUtilities selection = cap.getSelection();
        SongStructure sgs = selection.getModel();
        List<SongPart> spts = sgs.getSongParts();
        int sptIndex = spts.indexOf(selection.getSelectedSongPartParameters().get(0).getSpt());
        assert sptIndex > -1 : "spts=" + spts + " " + selection.getSelectedSongPartParameters();


        var buffer = RpValueCopyBuffer.getInstance();
        Rhythm r = buffer.getRhythm();
        RhythmParameter<?> rp = buffer.getRhythmParameter();
        List<Object> values = buffer.get();


        JJazzUndoManager um = JJazzUndoManagerFinder.getDefault().get(sgs);
        um.startCEdit(undoText);

        for (Object value : values)
        {
            if (sptIndex >= spts.size())
            {
                break;
            }
            SongPart spt = spts.get(sptIndex);
            if (spt.getRhythm() == r)
            {
                sgs.setRhythmParameterValue(spt, (RhythmParameter) rp, value);
            }
            sptIndex++;
        }

        um.endCEdit(undoText);
    }

    @Override
    public void selectionChange(SS_SelectionUtilities selection)
    {
        RpValueCopyBuffer buffer = RpValueCopyBuffer.getInstance();
        boolean b = false;
        if (!buffer.isEmpty() && selection.isRhythmParameterSelected() && selection.isContiguousSptSelection())
        {
            b = selection.getSelectedSongPartParameters().get(0).getRp() == buffer.getRhythmParameter();
        }
        setEnabled(b);
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
        SS_EditorTopComponent tc = SS_EditorTopComponent.getActive();
        if (tc != null)
        {
            SS_SelectionUtilities selection = new SS_SelectionUtilities(tc.getSS_Editor().getLookup());
            selectionChange(selection);
        }
    }
}

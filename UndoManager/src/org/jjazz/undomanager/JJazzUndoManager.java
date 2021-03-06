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
package org.jjazz.undomanager;

import java.util.LinkedList;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import org.openide.*;
import org.openide.awt.UndoRedo;
import org.openide.util.ChangeSupport;

/**
 * This is a copy of UndoRedo.Manager with a few convenience functions added to work with CompoundEdits.
 */
public class JJazzUndoManager extends UndoManager implements UndoRedo
{

    /**
     * Listeners liked Netbeans UndoAction/RedoAction linked to undo/redo buttons
     */
    private final ChangeSupport cs = new ChangeSupport(this);
    /**
     * vector of Edits to run
     */
    private final LinkedList<UndoableEditEvent> runus = new LinkedList<>(); // for fix of #8692
    /**
     * Current CEdit.
     */
    private CEdit currentCEdit = null;
    /**
     * True is undo/redo in progress.
     */
    private boolean undoRedoInProgress = false;

    public boolean isUndoRedoInProgress()
    {
        return undoRedoInProgress;
    }

    public void startCEdit(String n)
    {
        if (currentCEdit != null)
        {
            throw new IllegalStateException("currentCEdit=" + currentCEdit + " n=" + n);
        }
        currentCEdit = new CEdit(n);
        addEdit(currentCEdit);
    }

    public void endCEdit(String n)
    {
        if (currentCEdit == null || !currentCEdit.getPresentationName().equals(n))
        {
            throw new IllegalStateException("currentCEdit=" + currentCEdit + " n=" + n);
        }
        currentCEdit.end();
        if (currentCEdit.isEmpty())
        {
            // To avoid having undo/redo buttons enabled for nothing
            trimLastEdit();
        }
        // Force notification
        fireChange();
        // Ready for next compoundedit
        currentCEdit = null;
    }

    /**
     * Consume an undoable edit. Delegates to superclass and notifies listeners.
     *
     * @param ue the edit
     */
    @Override
    public void undoableEditHappened(final UndoableEditEvent ue)
    {
        /*
       * Edits are posted to request processor and the deadlock in #8692 between undoredo and document that fires the
       * undoable edit should be avoided this way.
         */
        synchronized (runus)
        {
            runus.add(ue);
        }

        updateTask();
    }

    /**
     * Discard all the existing edits from the undomanager.
     */
    @Override
    public void discardAllEdits()
    {
        synchronized (runus)
        {
            runus.add(null);
        }

        updateTask();
    }

    @Override
    public void undo() throws CannotUndoException
    {
        undoRedoInProgress = true;
        super.undo();
        updateTask();
        undoRedoInProgress = false;
    }

    @Override
    public void redo() throws CannotRedoException
    {
        undoRedoInProgress = true;
        super.redo();
        updateTask();
        undoRedoInProgress = false;
    }

    @Override
    public void undoOrRedo() throws CannotRedoException, CannotUndoException
    {
        undoRedoInProgress = true;
        super.undoOrRedo();
        updateTask();
        undoRedoInProgress = false;
    }

    /**
     * Call die on next edit to be redone (if there is one).
     */
    public void killNextEditToBeRedone()
    {
//      UndoableEdit ue = super.editToBeRedone();
//      if (ue != null)
//      {
//         ue.die();
//      }
        this.trimLastEdit();
        fireChange();
    }

    /**
     * Convenience method : the right way to handle UnsupportedEditException when catched by the highest level caller, ie the one
     * who called startCEdit() and should call endCEdit(). This method should be called first in the catch section.
     * <p>
     * 1/ Call endCEdit() on cEditName to terminate properly the compound edit. <br>
     * Because exception occured there will be missing SimpleEdits in the CEdit (compared to normal).
     * <p>
     * 2/ Call undo()<br>
     * This will roll back the CEdit eEditName, ie undo each of the collected simple edits before the exception occured.
     * <p>
     * 3/ Remove CEdit cEditName from the undomanager, so that it can't be redone.
     * <p>
     * 4/ Show a dialog to notify errMsg.
     *
     * @param cEditName
     * @param errMsg
     */
    public void handleUnsupportedEditException(String cEditName, String errMsg)
    {
        endCEdit(cEditName);

        undo();
        // Make redo not possible
        trimLastEdit();
        fireChange();

        // Notify user
        NotifyDescriptor d = new NotifyDescriptor.Message(errMsg, NotifyDescriptor.ERROR_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }

    private void updateTask()
    {
        for (;;)
        {
            UndoableEditEvent ue;

            synchronized (runus)
            {
                if (runus.isEmpty())
                {
                    break;
                }

                ue = runus.removeFirst();
            }

            if (ue == null)
            {
                superDiscardAllEdits();
            } else
            {
                superUndoableEditHappened(ue);
            }
        }
        fireChange();
    }

    /*
    * Attaches change listener to the this object. The listener is notified everytime the undo/redo ability of this
    * object changes.
     */
    //#32313 - synchronization of this method was removed
    @Override
    public void addChangeListener(ChangeListener l)
    {
        cs.addChangeListener(l);
    }

    /*
    * Removes the listener
     */
    @Override
    public void removeChangeListener(ChangeListener l)
    {
        cs.removeChangeListener(l);
    }

    @Override
    public String getUndoPresentationName()
    {
        return this.canUndo() ? super.getUndoPresentationName() : ""; // NOI18N
    }

    @Override
    public String getRedoPresentationName()
    {
        return this.canRedo() ? super.getRedoPresentationName() : ""; // NOI18N
    }

    /**
     * Called from undoableEditHappened() inner class
     */
    private void superUndoableEditHappened(UndoableEditEvent ue)
    {
        super.undoableEditHappened(ue);
    }

    /**
     * Called from discardAllEdits() inner class
     */
    private void superDiscardAllEdits()
    {
        super.discardAllEdits();
    }

    /**
     * Notify listeners.
     */
    private void fireChange()
    {
        cs.fireChange();
    }

    /**
     * Remove the last edit from the UndoManager. Must be used with care, e.g. only when you know that the last edit is an empty
     * CompoundEdit.
     */
    private void trimLastEdit()
    {
        trimEdits(edits.size() - 1, edits.size() - 1);
    }

    @Override
    public String toString()
    {
        return "JJazzUndoManager";
    }
}

/**
 * A CompoundEdit with convenience operations to work with the JazzUndoManager.
 */
class CEdit extends CompoundEdit
{

    private String name;

    public CEdit(String n)
    {
        if (n == null)
        {
            throw new IllegalArgumentException("n=" + n);
        }
        name = n;
    }

    public boolean isEmpty()
    {
        return edits.isEmpty();
    }

    @Override
    public String getPresentationName()
    {
        return name;
    }

    // @Override
    @Override
    public String getUndoPresentationName()
    {
        // return CTL_Undo() + " " + getPresentationName();
        return getPresentationName();
    }

    // @Override
    @Override
    public String getRedoPresentationName()
    {
        // return CTL_Redo() + " " + getPresentationName();
        return getPresentationName();
    }

    @Override
    public boolean isSignificant()
    {
        return true;
    }

    @Override
    public String toString()
    {
        return getPresentationName();
    }
}

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
package org.jjazz.test;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.jjazz.harmony.TimeSignature;
import org.jjazz.leadsheet.chordleadsheet.api.UnsupportedEditException;
import org.jjazz.rhythm.api.Rhythm;
import org.jjazz.rhythm.database.api.RhythmDatabase;
import org.jjazz.rhythm.parameters.RhythmParameter;
import org.jjazz.song.api.Song;
import org.jjazz.undomanager.JJazzUndoManager;
import org.jjazz.undomanager.JJazzUndoManagerFinder;
import org.netbeans.api.progress.BaseProgressUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.jjazz.songstructure.api.SongStructure;
import org.jjazz.songstructure.api.SongPart;

/**
 * For debug purposes...
 */
//@ActionID(category = "JJazz", id = "org.jjazz.test.testallrhythms")
//@ActionRegistration(displayName = "Test all rhythms on current song")
//@ActionReferences(
//        {
//           @ActionReference(path = "Menu/Edit", position = 50024),
//        })
public final class TestAllRhythms implements ActionListener
{

    private static final Logger LOGGER = Logger.getLogger(TestAllRhythms.class.getSimpleName());

    @Override
    public void actionPerformed(ActionEvent ae)
    {
        LOGGER.severe("TestAllRhythms() ------------");
        Song song = Utilities.actionsGlobalContext().lookup(Song.class);
        if (song == null)
        {
            LOGGER.severe("No current song");
            return;
        }

        Runnable r = new Run(song);
        BaseProgressUtils.showProgressDialogAndRun(r, "Executing test all rhythms...");
    }

    private class Run implements Runnable
    {

        Song song;

        Run(Song s)
        {
            song = s;
        }

        @Override
        public void run()
        {
            SongStructure sgs = song.getSongStructure();
            JJazzUndoManager um = JJazzUndoManagerFinder.getDefault().get(sgs);
            um.setLimit(1);      // to not use too much memory with all rhythms instances...
            RhythmDatabase rdb = RhythmDatabase.Utilities.getDefault();
            TimeSignature ts0 = sgs.getSongPart(0).getRhythm().getTimeSignature();
            for (Rhythm r : rdb.getRhythms(ts0, null))
            {
                LOGGER.log(Level.SEVERE, "-- r={0} file={1}", new Object[]
                {
                    r.getName(), r.getFile().getAbsolutePath()
                });
                SongPart spt = sgs.getSongPart(0);
                SongPart newSpt = spt.clone(r, spt.getStartBarIndex(), spt.getNbBars(), spt.getParentSection());
                RhythmParameter rp0 = r.getRhythmParameters().get(0);
                try
                {
                    sgs.replaceSongParts(Arrays.asList(spt), Arrays.asList(newSpt));
                    if (rp0.getDisplayName().equals("Style part") && rp0.getPossibleValues().contains("Main A"))
                    {
                        sgs.setRhythmParameterValue(newSpt, rp0, "Main A");
                    }
                    try
                    {
                        Thread.sleep(50);    // Leave time for the UI to be updated before next model change
                    } catch (InterruptedException ex)
                    {
                        Exceptions.printStackTrace(ex);
                    }
                } catch (UnsupportedEditException ex)
                {
                    LOGGER.log(Level.SEVERE, "Problem changing rhythm. ex={0}", ex.getLocalizedMessage());
                    continue;
                }
            }
            um.setLimit(100);
        }
    }

}

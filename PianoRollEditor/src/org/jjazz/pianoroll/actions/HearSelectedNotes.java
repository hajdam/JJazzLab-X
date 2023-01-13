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
package org.jjazz.pianoroll.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.event.ChangeListener;
import org.jjazz.phrase.api.NoteEvent;
import org.jjazz.phrase.api.Phrase;
import org.jjazz.pianoroll.api.NoteView;
import org.jjazz.pianoroll.api.NotesSelectionListener;
import org.jjazz.pianoroll.api.PianoRollEditor;
import org.jjazz.rhythm.api.MusicGenerationException;
import org.jjazz.testplayerservice.spi.TestPlayer;
import org.jjazz.util.api.ResUtil;
import org.jjazz.util.api.Utilities;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.actions.BooleanStateAction;

/**
 * Action to toggle the play of the last selected note.
 */
public class HearSelectedNotes extends BooleanStateAction
{

    private final PianoRollEditor editor;
    private CollectAndPlayNotesTask collectAndPlayNotesTask;
    private final ChangeListener changeListener;
    private static final Logger LOGGER = Logger.getLogger(HearSelectedNotes.class.getSimpleName());

    public HearSelectedNotes(PianoRollEditor editor)
    {
        this.editor = editor;

        putValue(Action.SMALL_ICON, new ImageIcon(getClass().getResource("resources/HearNoteOFF.png")));
        putValue(Action.LARGE_ICON_KEY, new ImageIcon(getClass().getResource("resources/HearNoteON.png")));
        // putValue("JJazzDisabledIcon", new ImageIcon(getClass().getResource("/org/jjazz/ui/musiccontrolactions/resources/PlaybackPointDisabled-24x24.png")));   //NOI18N                                
        putValue(Action.SHORT_DESCRIPTION, ResUtil.getString(getClass(), "HearNoteTooltip"));
        putValue("hideActionText", true);


        var nsl = getNotesSelectionListener();
        changeListener = evt -> selectionChanged(nsl.getLastNoteViewAddedToSelection());
        
        setSelected(false);
    }


    @Override
    public void actionPerformed(ActionEvent e)
    {
        setSelected(!getBooleanState());
    }

    public void setSelected(boolean b)
    {
        if (b == getBooleanState())
        {
            return;
        }
        setBooleanState(b);     // Notify action listeners

        if (b)
        {
            getNotesSelectionListener().addListener(changeListener);
        } else
        {
            getNotesSelectionListener().removeListener(changeListener);
            stopTask();
        }
    }

    @Override
    public String getName()
    {
        return "HearSelectedNotesName";
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return null;
    }

    // ====================================================================================
    // Private methods
    // ====================================================================================

    private void selectionChanged(NoteView lastNoteViewAddedToSelection)
    {
        if (getBooleanState() == false || lastNoteViewAddedToSelection == null)
        {
            stopTask();
            return;
        }

        startTask();
        hearNote(lastNoteViewAddedToSelection.getModel());

    }

    /**
     * Play one or more notes (wait a delay to possibly accept other notes in case a multiple selection was done).
     *
     * @param ne
     */
    private void hearNote(NoteEvent ne)
    {
        LOGGER.log(Level.FINE, "hearNote() -- ne={0}", ne);
        collectAndPlayNotesTask.getQueue().add(ne);
    }

    private void startTask()
    {
        if (collectAndPlayNotesTask == null)
        {
            collectAndPlayNotesTask = new CollectAndPlayNotesTask(10, 5);
        }
        collectAndPlayNotesTask.start();
    }

    private void stopTask()
    {
        if (collectAndPlayNotesTask != null)
        {
            collectAndPlayNotesTask.stop();
        }
    }

    private NotesSelectionListener getNotesSelectionListener()
    {
        return NotesSelectionListener.getInstance(editor.getLookup());
    }


    // ==========================================================================================================
    // Inner classes
    // ==========================================================================================================
    /**
     * A thread to accumulate incoming notes during waitTimeMsBeforePlaying, then start playing the first notes.
     * <p>
     */
    private class CollectAndPlayNotesTask implements Runnable
    {


        private enum State
        {
            OFF,
            WAITING,
            ACCUMULATING,
            PLAYING
        };
        private final int POLL_DELAY_MS = 1;
        private final Queue<NoteEvent> queue = new ConcurrentLinkedQueue<>();
        private ExecutorService executorService;
        private final int waitTimeMsBeforePlaying;
        private final int maxNbNotesPlayed;
        private State state;
        private List<NoteEvent> noteEvents = new ArrayList<>(20);


        /**
         * Create the task.
         *
         * @param waitTimeMsBeforePlaying
         * @param maxNbNotesPlayed
         */
        public CollectAndPlayNotesTask(int waitTimeMsBeforePlaying, int maxNbNotesPlayed)
        {
            this.waitTimeMsBeforePlaying = waitTimeMsBeforePlaying;
            this.maxNbNotesPlayed = maxNbNotesPlayed;
            state = State.OFF;
        }


        public Queue<NoteEvent> getQueue()
        {
            return queue;
        }

        public void start()
        {
            if (state.equals(State.OFF))
            {
                setState(State.WAITING);
                executorService = Executors.newSingleThreadExecutor();
                executorService.submit(this);
            }
        }

        public void stop()
        {
            if (!state.equals(State.OFF))
            {
                setState(State.OFF);
                Utilities.shutdownAndAwaitTermination(executorService, 1, 1);
            }
        }

        @Override
        public void run()
        {
            long startTimeMs = 0;

            LOGGER.fine("CollectAndPlayNotesTask.run() -- ");

            while (!state.equals(State.OFF))
            {
                if (state.equals(State.ACCUMULATING) && (System.currentTimeMillis() - startTimeMs) > waitTimeMsBeforePlaying)
                {
                    setState(State.PLAYING);
                }

                NoteEvent incoming = queue.poll();           // Does not block if empty                
                if (incoming != null)
                {

                    LOGGER.log(Level.FINE, "      state={0} incoming={1}", new Object[]
                    {
                        state, incoming
                    });

                    switch (state)
                    {
                        case WAITING ->
                        {
                            startTimeMs = System.currentTimeMillis();
                            noteEvents.add(incoming);
                            setState(State.ACCUMULATING);
                        }
                        case ACCUMULATING ->
                        {
                            noteEvents.add(incoming);
                        }
                        case PLAYING, OFF ->
                        {
                            // Nothing
                        }

                        default ->
                            throw new AssertionError(state.name());
                    }
                }


                try
                {
                    Thread.sleep(POLL_DELAY_MS);
                } catch (InterruptedException ex)
                {
                    return;
                }
            }

            LOGGER.fine("CollectAndPlayNotesTask.run() stopped ");
        }

        public synchronized State getState()
        {
            return state;
        }

        private synchronized void setState(State s)
        {
            if (s.equals(state))
            {
                return;
            }

            LOGGER.log(Level.FINE, "setState() -- old={0} >> {1}", new Object[]
            {
                state, s
            });
            state = s;
            switch (state)
            {
                case OFF:
                    break;
                case WAITING:
                    noteEvents.clear();
                    queue.clear();
                    break;
                case ACCUMULATING:
                    break;
                case PLAYING:
                    play();
                    break;
                default:
                    throw new AssertionError(state.name());
            }
        }

        private void play()
        {
            assert state.equals(State.PLAYING);

            if (noteEvents.isEmpty())
            {
                setState(State.WAITING);
                return;
            }

            LOGGER.log(Level.FINE, "play() -- noteEvents={0}", noteEvents);


            // Prepare the phrase
            float pos = 0;
            float durMax = noteEvents.size() == 1 ? 8f : 0.5f;
            Phrase p = new Phrase(editor.getModel().getChannel());
            for (var ne : noteEvents.subList(0, Math.min(noteEvents.size(), maxNbNotesPlayed)))
            {
                float dur = Math.min(durMax, ne.getDurationInBeats());
                var newNe = ne.getCopyDurPos(dur, pos);
                pos += dur + 0.1f;
                p.add(newNe);
            }


            // Play it
            try
            {
                Runnable endAction = () ->
                {
                    if (getState().equals(State.PLAYING))
                    {
                        setState(State.WAITING);
                    }
                };
                TestPlayer.getDefault().playTestNotes(p, endAction);
            } catch (MusicGenerationException ex)
            {
                Exceptions.printStackTrace(ex);
            }
        }

    }
}

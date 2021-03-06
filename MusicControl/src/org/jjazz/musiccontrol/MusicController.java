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
package org.jjazz.musiccontrol;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.SwingUtilities;
import org.jjazz.leadsheet.chordleadsheet.api.item.Position;
import org.jjazz.midi.InstrumentMix;
import org.jjazz.midi.MidiConst;
import org.jjazz.midi.MidiUtilities;
import org.jjazz.midi.JJazzMidiSystem;
import org.jjazz.midimix.MidiMix;
import org.jjazz.midimix.UserChannelRvKey;
import static org.jjazz.musiccontrol.Bundle.*;
import org.jjazz.rhythm.api.RhythmVoice;
import org.jjazz.rhythmmusicgeneration.MidiSequenceBuilder;
import org.jjazz.rhythmmusicgeneration.MusicGenerationContext;
import org.jjazz.rhythmmusicgeneration.MusicGenerationException;
import org.jjazz.song.api.Song;
import org.openide.util.NbBundle.Messages;

/**
 * Control the music playback.
 * <p>
 * Property changes are fired for:<br>
 * - start/pause/stop state changes<br>
 * - pre-playback : vetoable change, ie listeners can fire a PropertyVetoException to prevent playback to start<br>
 * - click and loop ON/OFF changes<br>
 * <p>
 * Use PlaybackListener to get notified of other events (bar/beat changes etc.) during playback. Note that PlaybackListeners will
 * be notified out of the Swing EDT.
 */
@Messages(
        {
            "ERR_SequencerLimited=This sequencer implementation is limited, music playback may not work"
        })
public class MusicController implements PropertyChangeListener, MetaEventListener, ControllerEventListener
{

    public static final String PROP_PLAYBACK_STATE = "PropPlaybackState";
    /**
     * This vetoable property is changed/fired just before starting playback and can be vetoed by vetoables listeners to cancel
     * playback start.
     * <p>
     * NewValue=MusicGenerationContext object.
     */
    public static final String PROPVETO_PRE_PLAYBACK = "PropVetoPrePlayback";
    public static final String PROP_CLICK = "PropClick";
    public static final String PROP_LOOPCOUNT = "PropLoopCount";
    private static final int TEMPO_DEFAULT = 120;

    /**
     * The playback states.
     * <p>
     * Property change listeners are notified with property PROP_PLAYBACK_STATE.
     */
    public enum State
    {
        PLAYBACK_STOPPED,
        PLAYBACK_PAUSED,
        PLAYBACK_STARTED
    }
    private static MusicController INSTANCE;
    /**
     * The context for which we will play music.
     */
    private MusicGenerationContext mgContext;
    /**
     * The playback context for one version of a song.
     */
    private PlaybackContext playbackContext;
    private State playbackState;
    /**
     * The current beat position during playback.
     */
    Position currentBeatPosition = new Position();
    private Sequencer sequencer;
    private int loopCount;
    private boolean isClickEnabled;

    /**
     * The list of the controller changes listened to
     */
    private static final int[] listenedControllers =
    {
        MidiConst.CTRL_CHG_JJAZZ_BEAT_CHANGE,
        MidiConst.CTRL_CHG_JJAZZ_ACTIVITY
    };
    /**
     * If true display built sequence when it is built
     */
    private boolean debugBuiltSequence = false;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private VetoableChangeSupport vcs = new VetoableChangeSupport(this);
    private List<PlaybackListener> playbackListeners = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(MusicController.class.getSimpleName());

    public static MusicController getInstance()
    {
        synchronized (MusicController.class)
        {
            if (INSTANCE == null)
            {
                INSTANCE = new MusicController();
            }
        }
        return INSTANCE;
    }

    private MusicController()
    {
        loopCount = 0;
        playbackState = State.PLAYBACK_STOPPED;
        isClickEnabled = false;

        // Initialize the sequencer
        sequencer = JJazzMidiSystem.getInstance().getDefaultSequencer();
        sequencer.addMetaEventListener(this);
        int[] res = sequencer.addControllerEventListener(this, listenedControllers);
        if (res.length != listenedControllers.length)
        {
            LOGGER.severe(ERR_SequencerLimited());
        }
        sequencer.setTempoInBPM(TEMPO_DEFAULT);

        // Listen to click settings changes
        ClickManager.getInstance().addPropertyChangeListener(this);
    }

    /**
     * Set the music context on which this controller's methods (play/pause/etc.) will operate.
     * <p>
     * Stop any playback. Tempo is set to song's tempo.
     *
     * @param context Can be null.
     */
    public void setContext(MusicGenerationContext context)
    {
        if (context != null && context.equals(this.mgContext))
        {
            return;
        }
        stop();
        if (this.mgContext != null)
        {
            this.mgContext.getMidiMix().removePropertyListener(this);
            this.mgContext.getSong().removePropertyChangeListener(this);
        }
        if (playbackContext != null)
        {
            playbackContext.close();
            playbackContext = null;
        }
        this.mgContext = context;
        if (this.mgContext != null)
        {
            this.mgContext.getMidiMix().addPropertyListener(this);
            this.mgContext.getSong().addPropertyChangeListener(this);
            setTempo(this.mgContext.getSong().getTempo());
            playbackContext = new PlaybackContext(this.mgContext);
        }
    }

    /**
     * Start the playback of a song using the current context.
     * <p>
     * Song is played from the beginning of the context range. Before playing the song, vetoable listeners are notified with a
     * PROPVETO_PRE_PLAYBACK property change.<p>
     *
     * @param fromBarIndex Play the song from this bar. Bar must be within the context's range.
     *
     * @throws java.beans.PropertyVetoException If a vetoable listener vetoed the playback start. A listener who has already
     *                                          notified user should throw an exception with a null message.
     * @throws MusicGenerationException         If a problem occurred which prevents song playing: no Midi out, song is already
     *                                          playing, rhythm music generation problem, etc.
     * @throws IllegalStateException            If context is null.
     *
     * @see #getPlayingSongCopy()
     */
    public void play(int fromBarIndex) throws MusicGenerationException, PropertyVetoException
    {
        if (mgContext == null)
        {
            throw new IllegalStateException("context=" + mgContext + ", fromBarIndex=" + fromBarIndex);
        }
        if (!mgContext.getRange().isIn(fromBarIndex))
        {
            throw new IllegalArgumentException("context=" + mgContext + ", fromBarIndex=" + fromBarIndex);
        }

        checkMidi();                // throws MusicGenerationException
        checkPlaybackNotStarted();  // throws MusicGenerationException

        // If we're here then playbackState = PAUSE or STOPPED
        if (mgContext.getRange().isEmpty())
        {
            // Throw an exception to let the UI roll back (eg play button)
            throw new MusicGenerationException("Nothing to play");
        }

        // Check that all listeners are OK to start playback
        vcs.fireVetoableChange(PROPVETO_PRE_PLAYBACK, null, mgContext);  // can raise PropertyVetoException

        // Regenerate the sequence and the related data if needed
        if (playbackContext.isDirty())
        {
            playbackContext.update();
        }

        // Set start position
        setPosition(fromBarIndex);

        // Start or restart the sequencer
        sequencer.setLoopCount(loopCount);

        sequencer.start();

        State old = this.getPlaybackState();
        playbackState = State.PLAYBACK_STARTED;

        pcs.firePropertyChange(PROP_PLAYBACK_STATE, old, playbackState);
    }

    /**
     * Resume playback from the pause state.
     * <p>
     * If played/paused song was modified, then resume() will just redirect to the play() method. If state is not PLAYBACK_PAUSED,
     * nothing is done.
     *
     * @throws org.jjazz.rhythmmusicgeneration.MusicGenerationException
     * @throws java.beans.PropertyVetoException
     */
    public void resume() throws MusicGenerationException, PropertyVetoException
    {
        if (!playbackState.equals(State.PLAYBACK_PAUSED))
        {
            return;
        }
        if (playbackContext.isDirty())
        {
            // Song was modified during playback, do play() instead
            play(mgContext.getRange().from);
            return;
        }

        if (mgContext == null)
        {
            throw new IllegalStateException("context=" + mgContext);
        }

        checkMidi();                // throws MusicGenerationException

        // Check that all listeners are OK to resume playback
        vcs.fireVetoableChange(PROPVETO_PRE_PLAYBACK, null, mgContext.getSong());  // can raise PropertyVetoException

        // Let's go again
        sequencer.start();

        State old = this.getPlaybackState();
        playbackState = State.PLAYBACK_STARTED;

        pcs.firePropertyChange(PROP_PLAYBACK_STATE, old, playbackState);

    }

    /**
     * Stop the playback of the sequence if it was playing, and reset the position to the beginning of the sequence.
     */
    public void stop()
    {
        sequencer.stop();
        State old = this.getPlaybackState();
        playbackState = State.PLAYBACK_STOPPED;
        pcs.firePropertyChange(PROP_PLAYBACK_STATE, old, playbackState);
        // Position must be reset after the stop so that playback beat change tracking listeners are not reset upon stop
        int bar = mgContext != null ? mgContext.getRange().from : 0;
        setPosition(bar);
    }

    /**
     * Stop the playback of the sequence and leave the position unchanged.
     * <p>
     * If played song was modified after playback started, then pause() will just redirect to the stop() method. If state is not
     * PLAYBACK_STARTED, nothing is done.
     */
    public void pause()
    {
        if (!playbackState.equals(State.PLAYBACK_STARTED))
        {
            return;
        }
        if (playbackContext.isDirty())
        {
            // Song was modified during playback, pause() not allowed, do stop() instead
            stop();
            return;
        }
        sequencer.stop();
        State old = getPlaybackState();
        playbackState = State.PLAYBACK_PAUSED;
        pcs.firePropertyChange(PROP_PLAYBACK_STATE, old, playbackState);
    }

    /**
     * The current MusicGenerationContext.
     *
     * @return Can be null.
     */
    public MusicGenerationContext getContext()
    {
        return mgContext;
    }

    public Position getBeatPosition()
    {
        return currentBeatPosition;
    }

    public State getPlaybackState()
    {
        return playbackState;
    }

    public void setClickEnabled(boolean b)
    {
        if (isClickEnabled != b)
        {
            isClickEnabled = b;
            if (sequencer.isRunning())
            {
                sequencer.setTrackMute(playbackContext.clickTrackId, !isClickEnabled);
            }
            pcs.firePropertyChange(PROP_CLICK, !b, b);
        }
    }

    public boolean isClickEnabled()
    {
        return isClickEnabled;
    }

    public int getLoopCount()
    {
        return loopCount;
    }

    /**
     * Set the loop count of the playback.
     *
     * @param loopCount If 0, play the song once (no loop). Use Sequencer.LOOP_CONTINUOUSLY for endless loop.
     */
    public void setLoopCount(int loopCount)
    {
        if (loopCount != Sequencer.LOOP_CONTINUOUSLY && loopCount < 0)
        {
            throw new IllegalArgumentException("loopCount=" + loopCount);
        }
        int old = this.loopCount;
        this.loopCount = loopCount;
        this.sequencer.setLoopCount(loopCount);
        pcs.firePropertyChange(PROP_LOOPCOUNT, old, this.loopCount);
    }

    public void setDebugBuiltSequence(boolean b)
    {
        debugBuiltSequence = b;
    }

    public boolean isDebugBuiltSequence()
    {
        return debugBuiltSequence;
    }

    /**
     * Add a listener to be notified of playback bar/beat changes events etc.
     * <p>
     * Listeners should note they will be called out of the Swing EDT (Event Dispatch Thread).
     *
     * @param listener
     */
    public synchronized void addPlaybackListener(PlaybackListener listener)
    {
        if (!playbackListeners.contains(listener))
        {
            playbackListeners.add(listener);
        }
    }

    public synchronized void removePlaybackListener(PlaybackListener listener)
    {
        playbackListeners.remove(listener);
    }

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Listeners will be notified via the PROPVETO_PRE_PLAYBACK property change before a playback is started.
     * <p>
     * The NewValue is a MusicGenerationContext object. Note that listener is responsible for informing the user if the change was
     * vetoed.
     *
     * @param listener
     */
    public synchronized void addVetoableChangeListener(VetoableChangeListener listener)
    {
        vcs.addVetoableChangeListener(listener);
    }

    public synchronized void removeVetoableChangeListener(VetoableChangeListener listener)
    {
        vcs.removeVetoableChangeListener(listener);
    }

    /**
     * Send a short sequence of notes on specified channel.
     * <p>
     * If fixPitch &lt; 0 then fixPitch is ignored: play a series of notes starting at 60+transpose. If fixPitch&gt;=0 then play a
     * series of notes with same pitch=fixPitch.
     *
     * @param channel
     * @param fixPitch  -1 means not used.
     * @param transpose Transposition value in semi-tons to be added. Ignored if fixPitch&gt;=0.
     * @param endAction Called when sequence is over. Can be null.
     * @throws org.jjazz.rhythmmusicgeneration.MusicGenerationException If a problem occurred. endAction.run() is called before
     *                                                                  throwing the exception.
     */
    public void playTestNotes(int channel, int fixPitch, int transpose, final Runnable endAction) throws MusicGenerationException
    {
        try
        {
            checkMidi();
            checkPlaybackNotStarted();
        } catch (MusicGenerationException ex)
        {
            endAction.run();
            throw ex;
        }
        if (playbackContext != null)
        {
            playbackContext.setDirty(); // Make sure song sequence is recalculated if there was a song playing before
        }
        stop();
        try
        {
            // build a track to hold the notes to send
            Sequence seq = new Sequence(Sequence.PPQ, 16);
            Track track = seq.createTrack();
            ShortMessage sm;
            for (int i = 60; i <= 72; i += 3)
            {
                sm = new ShortMessage();
                int pitch = fixPitch >= 0 ? fixPitch : i + transpose;
                sm.setMessage(ShortMessage.NOTE_ON, channel, pitch, 60);
                track.add(new MidiEvent(sm, i - 57));
                sm = new ShortMessage();
                sm.setMessage(ShortMessage.NOTE_OFF, channel, pitch, 60);
                track.add(new MidiEvent(sm, i - 52));
            }
            // create an object to listen to the End of Track MetaEvent and stop the sequencer
            MetaEventListener stopSequencer = new MetaEventListener()
            {
                @Override
                public void meta(MetaMessage event)
                {
                    if (event.getType() == 47) // Meta Event for end of sequence
                    {
                        sequencer.removeMetaEventListener(this);
                        if (endAction != null)
                        {
                            endAction.run();
                        }
                    }
                }
            };
            sequencer.addMetaEventListener(stopSequencer);

            // play the sequence
            sequencer.setTickPosition(0);
            sequencer.setLoopCount(0);
            sequencer.setSequence(seq);
            setTempo(100);
            sequencer.start();
        } catch (InvalidMidiDataException e)
        {
            throw new MusicGenerationException(e.getLocalizedMessage());
        }
    }

    //-----------------------------------------------------------------------
    // Implementation of the ControlEventListener interface
    //-----------------------------------------------------------------------
    /**
     * Handle the listened controllers notifications.
     * <p>
     * CAUTIOUS : the global listenedControllers array must be consistent with this method !
     *
     * @param event
     */
    @Override
    public void controlChange(ShortMessage event)
    {
        long tick = sequencer.getTickPosition() - playbackContext.songTickStart;
        int data1 = event.getData1();
        switch (data1)
        {
            case MidiConst.CTRL_CHG_JJAZZ_MARKER_SYNC:
                // Not used for now
                break;
            case MidiConst.CTRL_CHG_JJAZZ_CHORD_CHANGE:
                // Not used for now
                break;
            case MidiConst.CTRL_CHG_JJAZZ_ACTIVITY:
                fireMidiActivity(event.getChannel(), tick);
                break;
            case MidiConst.CTRL_CHG_JJAZZ_BEAT_CHANGE:
                int index = (int) (tick / MidiConst.PPQ_RESOLUTION);
                long remainder = tick % MidiConst.PPQ_RESOLUTION;
                index += (remainder <= MidiConst.PPQ_RESOLUTION / 2) ? 0 : 1;
                if (index >= playbackContext.naturalBeatPositions.size())
                {
                    index = playbackContext.naturalBeatPositions.size() - 1;
                }
                Position newPos = playbackContext.naturalBeatPositions.get(index);
                setCurrentBeatPosition(newPos.getBar(), newPos.getBeat());
                break;
            default:
                LOGGER.log(Level.WARNING, "controlChange() controller event not managed data1={0}", data1);
                break;
        }
    }

    //-----------------------------------------------------------------------
    // Implementation of the MetaEventListener interface
    //-----------------------------------------------------------------------
    @Override
    public void meta(MetaMessage meta)
    {
        if (meta.getType() == 47) // Meta Event for end of sequence
        {
            // This method  is called from the Sequencer thread, NOT from the EDT !
            // So if this method impacts the UI, it must use SwingUtilities.InvokeLater() (or InvokeAndWait())
            LOGGER.fine("Sequence end reached");
            Runnable doRun = new Runnable()
            {
                @Override
                public void run()
                {
                    stop();
                }
            };
            SwingUtilities.invokeLater(doRun);
        }
    }

    //-----------------------------------------------------------------------
    // Implementation of the PropertiesListener interface
    //-----------------------------------------------------------------------
    /**
     *
     * @param e
     */
    @SuppressWarnings(
            {
                "unchecked", "rawtypes"
            })
    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        LOGGER.log(Level.FINE, "propertyChange() e={0}", e);
        if (mgContext == null)
        {
            return;
        }
        if (e.getSource() == mgContext.getSong())
        {
            if (e.getPropertyName() == Song.PROP_MODIFIED_OR_SAVED)
            {
                if ((Boolean) e.getNewValue() == true)
                {
                    playbackContext.setDirty();
                }
            } else if (e.getPropertyName() == Song.PROP_TEMPO)
            {
                setTempo((Integer) e.getNewValue());
            } else if (e.getPropertyName() == Song.PROP_CLOSED)
            {
                stop();
            }
        } else if (e.getSource() == mgContext.getMidiMix() && null != e.getPropertyName())
        {
            switch (e.getPropertyName())
            {
                case MidiMix.PROP_INSTRUMENT_MUTE:
                    InstrumentMix insMix = (InstrumentMix) e.getOldValue();
                    updateTrackMuteState(insMix, playbackContext.mapRvTrackId);
                    break;
                case MidiMix.PROP_CHANNEL_DRUMS_REROUTED:
                case MidiMix.PROP_INSTRUMENT_TRANSPOSITION:
                case MidiMix.PROP_INSTRUMENT_VELOCITY_SHIFT:
                    // This can impact the sequence, make sure it is rebuilt
                    playbackContext.setDirty();
                    break;
                case MidiMix.PROP_CHANNEL_INSTRUMENT_MIX:
                    playbackContext.mapRvTrackId.clear();       // Mapping between RhythmVoice and Sequence tracks is no longer valid
                    playbackContext.setDirty();                 // Make sure sequence is rebuilt
                    break;
                case MidiMix.PROP_DRUMS_INSTRUMENT_KEYMAP:
                    // KeyMap has changed, need to regenerate the sequence
                    playbackContext.setDirty();
                    break;
                default:
                    // eg MidiMix.PROP_USER_CHANNEL: do nothing
                    break;
            }
        } else if (e.getSource() == ClickManager.getInstance())
        {
            // Make sure click track is recalculated if click features have changed
            if (e.getPropertyName() == ClickManager.PROP_CLICK_CHANNEL
                    || e.getPropertyName() == ClickManager.PROP_CLICK_PITCH
                    || e.getPropertyName() == ClickManager.PROP_CLICK_VELOCITY_HIGH
                    || e.getPropertyName() == ClickManager.PROP_CLICK_VELOCITY_LOW)
            {
                playbackContext.setDirty();
            }
        }

        if (playbackContext.isDirty() && playbackState == State.PLAYBACK_PAUSED)
        {
            stop();
        }
    }

    // =====================================================================================
    // Private methods
    // =====================================================================================
    /**
     *
     * Set the sequencer and model position to the specified bar.
     * <p>
     * Take into account the possible precount bars.
     * <p>
     * @param fromBar Must be &gt;=0
     */
    private void setPosition(int fromBar)
    {
        long tick = 0;       // Default when fromBar==0 and click precount is true
        if (mgContext != null)
        {
            int relativeBar = fromBar - mgContext.getRange().from;
            if (relativeBar > 0 || !ClickManager.getInstance().isClickPrecount())
            {
                // No precount
                tick = playbackContext.songTickStart + mgContext.getTick(new Position(relativeBar, 0));
            }
        }
        sequencer.setTickPosition(tick);
        setCurrentBeatPosition(fromBar, 0);
    }

    /**
     *
     * @param insMix
     * @param mapRvTrack The map which provides the track index corresponding to each rhythmvoice.
     */
    private void updateTrackMuteState(InstrumentMix insMix, HashMap<RhythmVoice, Integer> mapRvTrack)
    {
        boolean b = insMix.isMute();
        RhythmVoice rv = mgContext.getMidiMix().getKey(insMix);
        if (rv instanceof UserChannelRvKey)
        {
            return;
        }
        Integer trackIndex = mapRvTrack.get(rv);
        if (trackIndex != null)
        {
            sequencer.setTrackMute(trackIndex, b);
            if (sequencer.getTrackMute(trackIndex) != b)
            {
                LOGGER.log(Level.SEVERE, "updateTrackMuteState() can''t mute on/off track number: {0} mute={1} insMix={2}", new Object[]
                {
                    trackIndex, b, insMix
                });
            }
        } else
        {
            // Might be null if mapRvTrack was reset because of a MidiMix change and sequence has not been rebuilt yet
        }
    }

    private void fireBeatChanged(Position oldPos, Position newPos)
    {
        for (PlaybackListener pl : playbackListeners.toArray(new PlaybackListener[0]))
        {
            pl.beatChanged(oldPos, newPos);
        }
    }

    private void fireBarChanged(int oldBar, int newBar)
    {
        for (PlaybackListener pl : playbackListeners.toArray(new PlaybackListener[0]))
        {
            pl.barChanged(oldBar, newBar);
        }
    }

    private void fireMidiActivity(int channel, long tick)
    {
        for (PlaybackListener pl : playbackListeners.toArray(new PlaybackListener[0]))
        {
            pl.midiActivity(channel, tick);
        }
    }

    private void setCurrentBeatPosition(int bar, float beat)
    {
        Position oldPos = new Position(currentBeatPosition);
        currentBeatPosition.setBar(bar);
        currentBeatPosition.setBeat(beat);
        fireBeatChanged(oldPos, new Position(currentBeatPosition));
        if (beat == 0)
        {
            fireBarChanged(oldPos.getBar(), bar);
        }
    }

    private void setTempo(float bpm)
    {
        // Recommanded way instead of setTempoInBpm() : works OK in java 7
        // Plus in Java 5-6-7 there is a bug if calling setTempoInBpm() while in non-zero position, after one loop tempo is reset to 120 !!!
        sequencer.setTempoFactor(bpm / TEMPO_DEFAULT);
    }

    private void checkMidi() throws MusicGenerationException
    {
        if (JJazzMidiSystem.getInstance().getDefaultOutDevice() == null)
        {
            throw new MusicGenerationException("No MIDI Out device set. Go to menu Tools/Options/Midi and select a Midi device.");
        }
    }

    private void checkPlaybackNotStarted() throws MusicGenerationException
    {
        if (playbackState == State.PLAYBACK_STARTED)
        {
            throw new MusicGenerationException("A song is already playing.");
        }
    }

    /**
     * The playback data associated to a single version of a song.
     */
    private class PlaybackContext
    {

        MusicGenerationContext context;
        /**
         * The generated sequence.
         */
        Sequence sequence;
        /**
         * The position of each natural beat.
         */
        List<Position> naturalBeatPositions;
        int clickTrackId;
        int precountTrackId;
        long songTickStart;
        long songTickEnd;
        int controlTrackId;
        private boolean dirty;

        /**
         * The sequence track id (index) for each rhythm voice
         */
        private HashMap<RhythmVoice, Integer> mapRvTrackId;

        /**
         * Create a "dirty" object (needs to be updated).
         *
         * @param context
         */
        private PlaybackContext(MusicGenerationContext context)
        {
            if (context == null)
            {
                throw new NullPointerException("context");
            }
            this.context = context;
            dirty = true;
        }

        /**
         * Prepare the sequencer to play the specified song.
         * <p>
         * Create the sequence and load it in the sequencer. Store all the other related sequence-dependent data in this object.
         * Object is now "clean".
         *
         * @param song
         * @throws MusicGenerationException If problem occurs when creating the sequence.
         */
        void update() throws MusicGenerationException
        {
            try
            {
                // Build the sequence
                MidiSequenceBuilder seqBuilder = new MidiSequenceBuilder(context);
                sequence = seqBuilder.buildSequence(false);                  // Can raise MusicGenerationException
                mapRvTrackId = seqBuilder.getRvTrackIdMap();                 // Used to identify a RhythmVoice's track

                // Add the control track
                ControlTrackBuilder ctm = new ControlTrackBuilder(context);
                controlTrackId = ctm.addControlTrack(sequence);
                naturalBeatPositions = ctm.getNaturalBeatPositions();

                // Add the click track
                clickTrackId = prepareClickTrack(sequence, context);

                // Add the click precount track, this will shift all song events
                songTickStart = preparePrecountClickTrack(sequence, context);
                precountTrackId = sequence.getTracks().length - 1;

                // Update the sequence if rerouting needed
                rerouteDrumsChannels(sequence, context.getMidiMix());

                if (debugBuiltSequence)
                {
                    LOGGER.info("update() song=" + context.getSong().getName() + " sequence :");
                    LOGGER.info(MidiUtilities.toString(sequence));
                }
                // Can raise InvalidMidiDataException                                               
                sequencer.setSequence(sequence);

                // Update muted state for each track
                updateAllTracksMuteState(context.getMidiMix());
                sequencer.setTrackMute(controlTrackId, false);
                sequencer.setTrackMute(precountTrackId, false);
                sequencer.setTrackMute(clickTrackId, !isClickEnabled);

                // Set position and loop points
                sequencer.setLoopStartPoint(songTickStart);
                songTickEnd = songTickStart + mgContext.getSizeInBeats() * MidiConst.PPQ_RESOLUTION;
                sequencer.setLoopEndPoint(songTickEnd);
                dirty = false;
            } catch (MusicGenerationException | InvalidMidiDataException ex)
            {
                throw new MusicGenerationException(ex.getLocalizedMessage());
            }
        }

        private void updateAllTracksMuteState(MidiMix mm)
        {
            for (RhythmVoice rv : mm.getRvKeys())
            {
                if (!(rv instanceof UserChannelRvKey))
                {
                    InstrumentMix insMix = mm.getInstrumentMixFromKey(rv);
                    Integer trackId = mapRvTrackId.get(rv);
                    assert trackId != null : "rv=" + rv + " insMix=" + insMix + " mapRvTrackId=" + mapRvTrackId;
                    sequencer.setTrackMute(trackId, insMix.isMute());
                }
            }
        }

        /**
         *
         * @param sequence
         * @param mm
         * @param sg
         * @return The track id
         */
        private int prepareClickTrack(Sequence sequence, MusicGenerationContext context)
        {
            // Add the click track
            ClickManager cm = ClickManager.getInstance();
            int trackId = cm.addClickTrack(sequence, context);

            // Send a Drums program change if Click channel is not used in the current MidiMix
            int clickChannel = ClickManager.getInstance().getChannel();
            if (context.getMidiMix().getInstrumentMixFromChannel(clickChannel) == null)
            {
//                Instrument ins = DefaultInstruments.getInstance().getInstrument(RvType.Drums);
//                JJazzMidiSystem jms = JJazzMidiSystem.getInstance();
//                jms.sendMidiMessagesOnJJazzMidiOut(ins.getMidiMessages(clickChannel));  // Might not send anything if default instrument is Void Instrument
            }
            return trackId;
        }

        /**
         *
         * @param sequence
         * @param mm
         * @param sg
         * @return The tick position of the start of the song.
         */
        private long preparePrecountClickTrack(Sequence sequence, MusicGenerationContext context)
        {
            // Add the click track
            ClickManager cm = ClickManager.getInstance();
            long tickPos = cm.addPreCountClickTrack(sequence, context);

            // Send a Drums program change if Click channel is not used in the current MidiMix
            int clickChannel = ClickManager.getInstance().getChannel();
            if (context.getMidiMix().getInstrumentMixFromChannel(clickChannel) == null)
            {
//                Instrument ins = DefaultInstruments.getInstance().getInstrument(RvType.Drums);
//                JJazzMidiSystem jms = JJazzMidiSystem.getInstance();
//                jms.sendMidiMessagesOnJJazzMidiOut(ins.getMidiMessages(clickChannel));  // Might not send anything if default instrument is Void Instrument                            
            }
            return tickPos;
        }

        private void rerouteDrumsChannels(Sequence seq, MidiMix mm)
        {
            List<Integer> toBeRerouted = mm.getDrumsReroutedChannels();
            MidiUtilities.rerouteShortMessages(seq, toBeRerouted, MidiConst.CHANNEL_DRUMS);
        }

        /**
         * Make sure resources are released.
         */
        void close()
        {
            if (sequence == null)
            {
                return;
            }
            Track[] tracks = sequence.getTracks();
            if (tracks != null)
            {
                for (Track track : tracks)
                {
                    sequence.deleteTrack(track);
                }
            }
        }

        /**
         * The context song has been modified, should call update().
         *
         * @return
         */
        boolean isDirty()
        {
            return dirty;
        }

        void setDirty()
        {
            dirty = true;
        }
    }

}

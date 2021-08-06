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
package org.jjazz.musiccontrol.api.playbacksession;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.event.SwingPropertyChangeSupport;
import org.jjazz.leadsheet.chordleadsheet.api.item.Position;
import org.jjazz.midi.api.MidiUtilities;
import org.jjazz.phrase.api.Phrase;
import org.jjazz.rhythm.api.MusicGenerationException;
import org.jjazz.rhythm.api.RhythmVoice;
import org.jjazz.rhythmmusicgeneration.api.ContextChordSequence;
import org.jjazz.songcontext.api.SongContext;
import org.jjazz.util.api.IntRange;

/**
 * A SongContextSession-based session which allows the user to add/remove MidiEvents (without changing the Sequence size) while
 * the sequence is playing.
 * <p>
 * Use buffer tracks and mute/unmute tracks to enable on-the-fly sequence changes.
 */
public class DynamicSongSession implements PropertyChangeListener, PlaybackSession, PositionProvider, ChordSymbolProvider, SongContextProvider, EndOfPlaybackActionProvider
{

    private long originalTrackTickSize;
    private int nbPlayingTracks;
    private Map<RhythmVoice, Phrase> currentMapRvPhrase;
    private TrackSet trackSet;         // Exclude track 0 
    private final SongContextSession songContextSession;
    private Sequence sequence;
    private final HashMap<Integer, Boolean> mapTrackIdMuted = new HashMap<>();
    private final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);
    private static final Logger LOGGER = Logger.getLogger(DynamicSongSession.class.getSimpleName());  //NOI18N

    /**
     * Create a DynamicSongSession with the specified parameters.
     *
     * @param sgContext
     * @param enablePlaybackTransposition
     * @param enableClickTrack
     * @param enablePrecountTrack
     * @param enableControlTrack
     * @param loopCount Use SongContextSession.PLAYBACK_SETTINGS_LOOP_COUNT to rely on PlaybackSettings.
     * @param endOfPlaybackAction
     */
    public DynamicSongSession(SongContext sgContext, boolean enablePlaybackTransposition, boolean enableClickTrack, boolean enablePrecountTrack, boolean enableControlTrack,
            int loopCount,
            ActionListener endOfPlaybackAction)
    {
        songContextSession = new SongContextSession(sgContext,
                enablePlaybackTransposition, enableClickTrack, enablePrecountTrack, enableControlTrack,
                loopCount,
                endOfPlaybackAction
        );

        songContextSession.addPropertyChangeListener(this);
    }

    @Override
    public void generate(boolean silent) throws MusicGenerationException
    {
        songContextSession.generate(silent);
        sequence = songContextSession.getSequence();
        originalTrackTickSize = sequence.getTickLength();
        nbPlayingTracks = sequence.getTracks().length;
        currentMapRvPhrase = new HashMap<>(songContextSession.getRvPhraseMap());


        // Create the trackset to manage double-buffering at track level
        var originalMapIdMuted = songContextSession.getTracksMuteStatus(); // Track 0 is not included, but may contain click/precount/control tracks
        trackSet = new TrackSet(sequence);
        originalMapIdMuted.keySet().forEach(trackId -> trackSet.addTrack(trackId));


        // Initialize our own tracks mute state
        for (int trackId : originalMapIdMuted.keySet())
        {
            mapTrackIdMuted.put(trackId, originalMapIdMuted.get(trackId));
            mapTrackIdMuted.put(trackSet.getBufferTrackId(trackId), true);         // buffer track is muted
        }


//        LOGGER.info("generate() mapTrackIdMuted=" + mapTrackIdMuted);
//        LOGGER.info("generate() mapRvTrackId=" + songContextSession.getRvTrackIdMap());
//        LOGGER.info("generate() trackSet=" + trackSet.toString());
//        LOGGER.info(" Original sequence=" + MidiUtilities.toString(sequence));
    }

    /**
     * Get the original sequence and create additional empty tracks to allow "double buffering modification" via muting/unmuting
     * tracks.
     * <p>
     * The total number of tracks is 2 * getNbPlayingTracks().
     *
     * @return
     */
    @Override
    public Sequence getSequence()
    {
        return sequence;
    }


    /**
     * The number of playing tracks (excluding the additional double buffering extra tracks).
     *
     * @return
     */
    public int getNbPlayingTracks()
    {
        return nbPlayingTracks;
    }

    /**
     * The size in ticks of the original generated sequence.
     *
     * @return
     */
    public long getOriginalSequenceSize()
    {
        return originalTrackTickSize;
    }


    /**
     * Update the sequence by replacing the contents of one or more RhythmVoice tracks.
     * <p>
     * Update tracks for which there is an actual change. Changes are first applied to muted "buffer tracks", then we switch the
     * mute status between the buffer and the playing tracks. The transition might be noticeable if notes were still ringing when
     * tracks mute state is switched.
     *
     * @param newMapRvPhrase
     * @throws IllegalArgumentException If a MidiEvent tick position is beyond getOriginalSequenceSize(), or if session is not in
     * the GENERATED state.
     */
    public void updateSequence(Map<RhythmVoice, Phrase> newMapRvPhrase)
    {
//        LOGGER.info("updateSequence() ---- newMapRvPhrase.keySet()=" + newMapRvPhrase.keySet());

        if (!getState().equals(PlaybackSession.State.GENERATED))
        {
            throw new IllegalStateException("newMapRvPhrase=" + newMapRvPhrase + " getState()=" + getState());
        }


        // Update sequence with a phrase if it's modified compared to current one
        for (RhythmVoice rv : newMapRvPhrase.keySet())
        {
            var newPhrase = newMapRvPhrase.get(rv);


            if (currentMapRvPhrase.get(rv).equals(newPhrase))
            {
                // No change do nothing
                continue;
            } else
            {
                // Replace the current events
//                LOGGER.info("updateSequence()     changes detected for rv=" + rv + ", updating");
                currentMapRvPhrase.put(rv, newPhrase);
            }


            // Clear and update the buffer track with the passed events
            int trackId = getOriginalRvTrackIdMap().get(rv);
            Track bufferTrack = trackSet.getBufferTrack(trackId);
            MidiUtilities.clearTrack(bufferTrack);
            for (MidiEvent me : newPhrase.toMidiEvents())
            {
                if (me.getTick() > originalTrackTickSize)
                {
                    throw new IllegalArgumentException("me=" + MidiUtilities.toString(me.getMessage(), me.getTick()) + " originalTrackTickSize=" + originalTrackTickSize);
                }
                bufferTrack.add(me);
            }


            // Make sure size is not changed
            MidiUtilities.setEndOfTrackPosition(bufferTrack, originalTrackTickSize);


            // Update the track mute state : apply mute status of the active track to the buffer track, then mute the active track
            boolean activeTrackMuteState = mapTrackIdMuted.get(trackSet.getActiveTrackId(trackId));
            mapTrackIdMuted.put(trackSet.getBufferTrackId(trackId), activeTrackMuteState);
            mapTrackIdMuted.put(trackSet.getActiveTrackId(trackId), true);


            // Finally exchange the active and buffer tracks
            trackSet.swapBufferAndActiveTracks(trackId);
        }

//        LOGGER.info("updateSequence() AFTER: mapTrackIdMuted=" + mapTrackIdMuted);

        // Notify our listeners that tracks mute status has changed
        pcs.firePropertyChange(PlaybackSession.PROP_MUTED_TRACKS, null, mapTrackIdMuted);
    }

    /**
     * A map providing the original track id corresponding to each used RhythmVoice in the given context.
     * <p>
     * If a song uses rhythms R1 and R2 and context is only on R2 bars, then the map only contains R2 rhythm voices and track id.
     * <p>
     *
     * @return @see getOriginalTrackEvents()
     */
    public Map<RhythmVoice, Integer> getOriginalRvTrackIdMap()
    {
        return songContextSession.getRvTrackIdMap();
    }

    /**
     * A map giving the original resulting Phrase for each RhythmVoice, in the current context.
     * <p>
     *
     * @return
     */
    public Map<RhythmVoice, Phrase> getOriginalRvPhraseMap()
    {
        return songContextSession.getRvPhraseMap();
    }

    /**
     * Get the current Phrase for each RhythmVoice track.
     *
     * @return
     */
    public Map<RhythmVoice, Phrase> getCurrentRvPhraseMap()
    {
        return currentMapRvPhrase;
    }

    @Override
    public State getState()
    {
        return songContextSession.getState();
    }

    @Override
    public int getTempo()
    {
        return songContextSession.getTempo();
    }

    @Override
    public HashMap<Integer, Boolean> getTracksMuteStatus()
    {
        // Our version
        return mapTrackIdMuted;
    }

    @Override
    public long getLoopEndTick()
    {
        return songContextSession.getLoopEndTick();
    }

    @Override
    public long getLoopStartTick()
    {
        return songContextSession.getLoopStartTick();
    }

    @Override
    public int getLoopCount()
    {
        return songContextSession.getLoopCount();
    }

    @Override
    public IntRange getBarRange()
    {
        return songContextSession.getBarRange();
    }

    @Override
    public long getTick(int barIndex)
    {
        return songContextSession.getTick(barIndex);

    }

    @Override
    public void cleanup()
    {
        songContextSession.removePropertyChangeListener(this);
        songContextSession.cleanup();
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    // ==========================================================================================================
    // SongContextProvider implementation
    // ==========================================================================================================    
    @Override
    public SongContext getSongContext()
    {
        return songContextSession.getSongContext();
    }

    // ==========================================================================================================
    // PositionProvider implementation
    // ==========================================================================================================    
    @Override
    public List<Position> getPositions()
    {
        return songContextSession.getPositions();
    }

    // ==========================================================================================================
    // ChordSymbolProvider implementation
    // ==========================================================================================================    
    @Override
    public ContextChordSequence getContextChordGetSequence()
    {
        return songContextSession.getContextChordGetSequence();
    }

    // ==========================================================================================================
    // EndOfPlaybackActionProvider implementation
    // ==========================================================================================================   
    @Override
    public ActionListener getEndOfPlaybackAction()
    {
        return songContextSession.getEndOfPlaybackAction();
    }

    // ==========================================================================================================
    // PropertyChangeListener implementation
    // ==========================================================================================================
    @Override
    public void propertyChange(PropertyChangeEvent e)
    {

//        LOGGER.fine("propertyChange() e=" + e);

        if (e.getSource() == songContextSession)
        {

            PropertyChangeEvent newEvent;

            // Need to map the new mute status to the currently active tracks
            if (e.getPropertyName().equals(PlaybackSession.PROP_MUTED_TRACKS))
            {
                var originalMapTrackIdMuted = songContextSession.getTracksMuteStatus();
                for (int trackId : originalMapTrackIdMuted.keySet())
                {
                    boolean muted = originalMapTrackIdMuted.get(trackId);
                    mapTrackIdMuted.put(trackSet.getActiveTrackId(trackId), muted);
                }
                newEvent = new PropertyChangeEvent(this, e.getPropertyName(), e.getOldValue(), mapTrackIdMuted);
            } else
            {
                newEvent = new PropertyChangeEvent(this, e.getPropertyName(), e.getOldValue(), e.getNewValue());
            }

            // Forward the event and make this object the event source
            newEvent.setPropagationId(e.getPropagationId());
            pcs.firePropertyChange(newEvent);
        }


    }

    @Override
    public String toString()
    {
        return "DynamicSongSession=[" + songContextSession + "]";
    }

    // ==========================================================================================================
    // Private methods
    // ==========================================================================================================

    // ==========================================================================================================
    // Inner classes
    // ==========================================================================================================    
    /**
     * Manage a set of tracks of a sequence: N active tracks and N buffer tracks.
     */
    static private class TrackSet
    {

        private final Sequence trackSetSequence;
        private final Map<Integer, Integer> mapOriginalIdActiveId = new HashMap<>();
        private final Map<Integer, Integer> mapOriginalIdBufferId = new HashMap<>();

        public TrackSet(Sequence seq)
        {
            this.trackSetSequence = seq;
        }

        /**
         * Add a track to the TrackSet and create the corresponding buffer track.
         *
         * @param originalTrackId A value between 0 and getNbPlayingTracks()-1.
         */
        public void addTrack(int originalTrackId)
        {
            mapOriginalIdActiveId.put(originalTrackId, originalTrackId);

            // Create the corresponding buffer track
            Track bufferTrack = trackSetSequence.createTrack();
            int bufferTrackId = Arrays.asList(trackSetSequence.getTracks()).indexOf(bufferTrack);
            mapOriginalIdBufferId.put(originalTrackId, bufferTrackId);
        }

        /**
         * The buffer track for originalTrackId becomes the active track, and vice-versa.
         *
         * @param originalTrackId
         */
        public void swapBufferAndActiveTracks(int originalTrackId)
        {
            int tmp = getActiveTrackId(originalTrackId);
            mapOriginalIdActiveId.put(originalTrackId, getBufferTrackId(originalTrackId));
            mapOriginalIdBufferId.put(originalTrackId, tmp);
        }

        /**
         * The active track corresponding to the original track id.
         *
         * @param originalTrackId A value &lt; getNbPlayingTracks()
         * @return
         */
        public Track getActiveTrack(int originalTrackId)
        {
            return trackSetSequence.getTracks()[getActiveTrackId(originalTrackId)];
        }

        /**
         * The buffer track corresponding to the original track id.
         *
         * @param originalTrackId A value &lt; getNbPlayingTracks()
         * @return
         */
        public Track getBufferTrack(int originalTrackId)
        {
            return trackSetSequence.getTracks()[getBufferTrackId(originalTrackId)];
        }

        /**
         * The active track id corresponding to the original track id .
         *
         * @param originalTrackId A value &lt; getNbPlayingTracks()
         * @return
         */
        public int getActiveTrackId(int originalTrackId)
        {
            return mapOriginalIdActiveId.get(originalTrackId);
        }

        /**
         * The buffer track id corresponding to the original track id .
         *
         * @param originalTrackId A value &lt; getNbPlayingTracks()
         * @return
         */
        public int getBufferTrackId(int originalTrackId)
        {
            return mapOriginalIdBufferId.get(originalTrackId);
        }

        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Trackset:\n");
            sb.append("   mapOriginalIdActiveId=" + mapOriginalIdActiveId + "\n");
            sb.append("   mapOriginalIdBufferId=" + mapOriginalIdBufferId + "\n");
            return sb.toString();
        }

    }
}
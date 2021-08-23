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
package org.jjazz.phrase.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import org.jjazz.harmony.api.Note;
import org.jjazz.midi.api.MidiConst;
import org.jjazz.midi.api.MidiUtilities;
import org.jjazz.util.api.FloatRange;
import org.openide.util.Exceptions;

/**
 * A Note with a position and optional client properties.
 * <p>
 * This is an immutable class except for the client properties.
 */
public class NoteEvent extends Note implements Cloneable
{

    private float position;
    protected HashMap<String, Object> clientProperties;
    private static final Logger LOGGER = Logger.getLogger(NoteEvent.class.getSimpleName());

    public NoteEvent(int pitch, float duration, int velocity, float posInBeats)
    {
        super(pitch, duration, velocity);
        if (posInBeats < 0)
        {
            throw new IllegalArgumentException("posInBeats=" + posInBeats);   //NOI18N
        }
        position = posInBeats;
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified pitch.
     * <p>
     * Client properties are cloned from ne.
     *
     * @param ne
     * @param pitch
     */
    public NoteEvent(NoteEvent ne, int pitch)
    {
        this(pitch, ne.getDurationInBeats(), ne.getVelocity(), ne.getPositionInBeats());
        setClientProperties(ne);
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified duration.
     * <p>
     * Client properties are cloned from ne.
     *
     * @param ne
     * @param durationInBeats
     */
    public NoteEvent(NoteEvent ne, float durationInBeats)
    {
        this(ne.getPitch(), durationInBeats, ne.getVelocity(), ne.getPositionInBeats());
        setClientProperties(ne);
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified duration and position.
     * <p>
     * Client properties are cloned from ne.
     *
     * @param ne
     * @param durationInBeats
     * @param posInBeats
     */
    public NoteEvent(NoteEvent ne, float durationInBeats, float posInBeats)
    {
        this(ne.getPitch(), durationInBeats, ne.getVelocity(), posInBeats);
        setClientProperties(ne);
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified pitch, duration and position.
     * <p>
     * Client properties are cloned from ne.
     *
     * @param ne
     * @param pitch
     * @param durationInBeats
     * @param posInBeats
     */
    public NoteEvent(NoteEvent ne, int pitch, float durationInBeats, float posInBeats)
    {
        this(pitch, durationInBeats, ne.getVelocity(), posInBeats);
        setClientProperties(ne);
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified pitch, duration, velocity.
     * <p>
     * Client properties are cloned from ne.
     *
     * @param ne
     * @param pitch
     * @param durationInBeats
     * @param velocity
     */
    public NoteEvent(NoteEvent ne, int pitch, float durationInBeats, int velocity)
    {
        this(pitch, durationInBeats, velocity, ne.getPositionInBeats());
        setClientProperties(ne);
    }

    /**
     * Get NoteEvents from a list of NOTE_ON/OFF Midi events.
     * <p>
     *
     * @param midiEvents MidiEvents which are not ShortMessage.Note_ON/OFF are ignored. Must be ordered by tick position.
     * @param posInBeatsOffset The position in natural beats of the first tick of the track.
     * @return
     * @see MidiUtilities#getMidiEvents(javax.sound.midi.Track, java.util.function.Predicate, long, long)
     */
    static public List<NoteEvent> getNoteEvents(List<MidiEvent> midiEvents, float posInBeatsOffset)
    {
        List<NoteEvent> res = new ArrayList<>();


        // Build the NoteEvents
        MidiEvent[] lastNoteOn = new MidiEvent[128];
        for (MidiEvent me : midiEvents)
        {
            long tick = me.getTick();
            ShortMessage sm = MidiUtilities.getNoteMidiEvent(me.getMessage());
            if (sm == null)
            {
                // It's not a note ON/OFF message
                continue;
            }


            int pitch = sm.getData1();
            int velocity = sm.getData2();


            if (sm.getCommand() == ShortMessage.NOTE_ON && velocity > 0)
            {
                // NOTE_ON
                lastNoteOn[pitch] = me;

            } else
            {
                MidiEvent meOn = lastNoteOn[pitch];

                // NOTE_OFF
                if (meOn != null)
                {
                    // Create the NoteEvent
                    long tickOn = meOn.getTick();
                    ShortMessage smOn = (ShortMessage) meOn.getMessage();
                    float duration = ((float) tick - tickOn) / MidiConst.PPQ_RESOLUTION;
                    float posInBeats = posInBeatsOffset + ((float) tickOn / MidiConst.PPQ_RESOLUTION);
                    NoteEvent ne = new NoteEvent(pitch, duration, smOn.getData2(), posInBeats);
                    res.add(ne);

                    // Clean the last NoteOn
                    lastNoteOn[pitch] = null;
                } else
                {
                    // A note Off without a previous note On, do nothing
                }
            }
        }


        return res;
    }

    /**
     * Reset all current properties and copy all properties from ne.
     *
     * @param ne
     */
    public final void setClientProperties(NoteEvent ne)
    {
        clientProperties = (ne.clientProperties == null) ? null : new HashMap<>(ne.clientProperties);
    }

    /**
     * Put a client property.
     *
     * @param propertyName
     * @param value If null, the property is removed.
     */
    public void putClientProperty(String propertyName, Object value)
    {
        if (value == null)
        {
            if (clientProperties != null)
            {
                clientProperties.remove(propertyName);
            }
        } else
        {
            if (clientProperties == null)
            {
                clientProperties = new HashMap<>();
            }
            clientProperties.put(propertyName, value);
        }
    }

    /**
     * Get a client property.
     *
     * @param propertyName
     * @return Can be null.
     */
    public Object getClientProperty(String propertyName)
    {
        return clientProperties != null ? clientProperties.get(propertyName) : null;
    }

    /**
     * Convert a note into 2 MidiEvents (NoteON and NoteOFF).
     *
     * @param channel
     * @return
     */
    public List<MidiEvent> toMidiEvents(int channel)
    {
        List<MidiEvent> events = new ArrayList<>();
        try
        {
            ShortMessage smOn = new ShortMessage(ShortMessage.NOTE_ON, channel, getPitch(), getVelocity());
            long tickOn = Math.round(position * MidiConst.PPQ_RESOLUTION);
            ShortMessage smOff = new ShortMessage(ShortMessage.NOTE_OFF, channel, getPitch(), 0);
            long tickOff = Math.round((position + getDurationInBeats()) * MidiConst.PPQ_RESOLUTION);
            events.add(new MidiEvent(smOn, tickOn));
            events.add(new MidiEvent(smOff, tickOff));
        } catch (InvalidMidiDataException ex)
        {
            Exceptions.printStackTrace(ex);
        }
        return events;
    }

    public boolean isBefore(NoteEvent mne)
    {
        return position < mne.position;
    }

    public float getPositionInBeats()
    {
        return position;
    }

    public FloatRange getBeatRange()
    {
        return new FloatRange(position, position + getDurationInBeats());
    }

    /**
     * Client properties are ignored.
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o)
    {
        if (o == null || !(o instanceof NoteEvent))
        {
            return false;
        }
        NoteEvent ne = (NoteEvent) o;

        return ne.position == position && super.equals(o);
    }

    /**
     * Client properties are ignored.
     *
     * @return
     */
    @Override
    public int hashCode()
    {
        int hash = super.hashCode();
        hash = 29 * hash + Float.floatToIntBits(this.position);
        return hash;
    }

    /**
     * Also clone the client properties.
     *
     * @return
     */
    @Override
    public NoteEvent clone()
    {
        NoteEvent ne = new NoteEvent(this, getPitch());
        return ne;
    }

//    @Override
//    public String toString()
//    {
//        return "[" + super.toString() + ",p=" + String.format("%.3f", position) + ",d=" + String.format("%.3f", getDurationInBeats()) + "]";
//    }
    @Override
    public String toString()
    {
        return String.format("[%s, p=%.3f, d=%.3f, v=%d]", toAbsoluteNoteString(), position, getDurationInBeats(), getVelocity());
    }

    /**
     * Save the specified NoteEvent as a string.
     * <p>
     * NOTE: client properties are NOT saved.
     *
     * @param ne
     * @return
     * @see loadAsString(String)
     */
    static public String saveAsString(NoteEvent ne)
    {
        return Note.saveAsString(ne) + ":" + ne.position;
    }

    /**
     * Create a NoteEvent from the specified string.
     * <p>
     * Example "60,FLAT,102,2.5:1.25" means pitch=60, AlterationDisplay=FLAT, velocity=102, duration=2.5 beats, and position=1.25
     * beats
     *
     * @param s
     * @return
     * @throws IllegalArgumentException If s is not a valid string.
     * @see saveAsString(NoteEvent)
     */
    static public NoteEvent loadAsString(String s) throws IllegalArgumentException
    {
        NoteEvent ne = null;
        String[] strs = s.split(":");
        if (strs.length == 2)
        {
            try
            {
                Note n = Note.loadAsString(strs[0]);
                float pos = Float.parseFloat(strs[1]);
                ne = new NoteEvent(n.getPitch(), n.getDurationInBeats(), n.getVelocity(), pos);
            } catch (IllegalArgumentException ex)
            {
                // Nothing
                LOGGER.warning("loadAsString() Invalid string s=" + s);
            }
        }

        if (ne == null)
        {
            throw new IllegalArgumentException("loadAsString() Invalid NoteEvent string s=" + s);
        }
        return ne;
    }

}

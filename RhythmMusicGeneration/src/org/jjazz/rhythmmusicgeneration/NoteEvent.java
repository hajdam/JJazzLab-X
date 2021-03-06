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
package org.jjazz.rhythmmusicgeneration;

import java.util.HashMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import org.jjazz.harmony.Note;
import org.jjazz.midi.MidiConst;
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

    public NoteEvent(int pitch, float duration, int velocity, float posInBeats)
    {
        super(pitch, duration, velocity);
        if (posInBeats < 0)
        {
            throw new IllegalArgumentException("posInBeats=" + posInBeats);
        }
        position = posInBeats;
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified pitch.
     * <p>
     * Client properties are cloned from mne.
     *
     * @param mne
     * @param pitch
     */
    public NoteEvent(NoteEvent mne, int pitch)
    {
        this(pitch, mne.getDurationInBeats(), mne.getVelocity(), mne.getPositionInBeats());
        clientProperties = (mne.clientProperties == null) ? null : new HashMap<>(mne.clientProperties);
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified duration.
     * <p>
     * Client properties are cloned from mne.
     *
     * @param mne
     * @param durationInBeats
     */
    public NoteEvent(NoteEvent mne, float durationInBeats)
    {
        this(mne.getPitch(), durationInBeats, mne.getVelocity(), mne.getPositionInBeats());
        clientProperties = (mne.clientProperties == null) ? null : new HashMap<>(mne.clientProperties);
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified duration and position.
     * <p>
     * Client properties are cloned from mne.
     *
     * @param mne
     * @param durationInBeats
     * @param posInBeats
     */
    public NoteEvent(NoteEvent mne, float durationInBeats, float posInBeats)
    {
        this(mne.getPitch(), durationInBeats, mne.getVelocity(), posInBeats);
        clientProperties = (mne.clientProperties == null) ? null : new HashMap<>(mne.clientProperties);
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified pitch, duration and position.
     * <p>
     * Client properties are cloned from mne.
     *
     * @param mne
     * @param pitch
     * @param durationInBeats
     * @param posInBeats
     */
    public NoteEvent(NoteEvent mne, int pitch, float durationInBeats, float posInBeats)
    {
        this(pitch, durationInBeats, mne.getVelocity(), posInBeats);
        clientProperties = (mne.clientProperties == null) ? null : new HashMap<>(mne.clientProperties);
    }

    /**
     * Create a MidiNoteEvent from an existing one but using the specified pitch, duration, velocity.
     * <p>
     * Client properties are cloned from mne.
     *
     * @param mne
     * @param pitch
     * @param durationInBeats
     * @param velocity
     */
    public NoteEvent(NoteEvent mne, int pitch, float durationInBeats, int velocity)
    {
        this(pitch, durationInBeats, velocity, mne.getPositionInBeats());
        clientProperties = (mne.clientProperties == null) ? null : new HashMap<>(mne.clientProperties);
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
    public MidiEvent[] toMidiEvents(int channel)
    {
        MidiEvent[] events = new MidiEvent[2];
        try
        {
            ShortMessage smOn = new ShortMessage(ShortMessage.NOTE_ON, channel, getPitch(), getVelocity());
            long tickOn = Math.round(position * MidiConst.PPQ_RESOLUTION);
            ShortMessage smOff = new ShortMessage(ShortMessage.NOTE_OFF, channel, getPitch(), 0);
            long tickOff = Math.round((position + getDurationInBeats()) * MidiConst.PPQ_RESOLUTION);
            events[0] = new MidiEvent(smOn, tickOn);
            events[1] = new MidiEvent(smOff, tickOff);
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
        return "[" + super.toString() + ",p=" + position + ",d=" + getDurationInBeats() + "]";
    }

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright @2019 Jerome Lelasseux. All rights reserved.
 *
 * This file is part of the JJazzLab-X software.
 *
 * JJazzLab-X is free software: you can redistribute it and/or modify
 * it under the terms of the Lesser GNU General Public License (LGPLv3) 
 * as published by the Free Software Foundation, either version 3 of the License, 
 * or (at your option) any later version.
 *
 * JJazzLab-X is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JJazzLab-X.  If not, see <https://www.gnu.org/licenses/>
 *
 * Contributor(s): 
 *
 */
package org.jjazz.midi;

import org.jjazz.midi.keymap.KeyRange;
import java.util.List;
import java.util.Objects;
import org.jjazz.midi.keymap.KeyMapGM;

/**
 * The main parameters of a drum kit instrument: a drum/key map and its ambience type.
 * <p>
 * This is an immutable class.
 */
public class DrumKit
{

    /**
     * Defines a key map for a drumkit: associate a percussion name to a note pitch.
     * <p>
     * 36=Kick, 37=Rimshot, 38=Snare, etc.
     */
    public interface KeyMap
    {

        /**
         * The key getRange of this KeyMap.
         *
         * @return
         */
        public KeyRange getRange();

        /**
         * The name of the DrumKitKeyMap.
         *
         * @return
         */
        public String getName();

        /**
         * True if this KeyMap contains otherKeyMap.
         * <p>
         * E.g. the GM2 KeyMap contains the GM keymap (but not the other way around): a GM-keymap-based rhythm can be played on a
         * GM2-keymap-based drums instrument. Should return true if this keymap==otherKeyMap.
         *
         * @param otherKeyMap
         * @return
         */
        public boolean isContaining(KeyMap otherKeyMap);

        /**
         * The isntrument name, e.g. "Kick" for the given key.
         *
         * @param pitch
         * @return Can be null if pitch is not used by this DrumKitKeyMap.
         */
        public String getKeyName(int pitch);

        /**
         * The pitch corresponding to the note name.
         *
         * @param noteName
         * @return -1 if noteName is not used by this DrumKitKeyMap.
         */
        public int getKey(String noteName);

        /**
         * Get the typical notes used for a rhythmic accent with this DrumMap.
         * <p>
         * Usually contains at least kicks/snares/crash or splash cymbals etc.
         *
         * @return Can be an empty list.
         */
        public List<Integer> getAccentKeys();

    }

    /**
     * The main ambience types based on GM2 standard.
     */
    public enum Type
    {
        STANDARD, ROOM, POWER, ELECTRONIC, ANALOG, JAZZ, BRUSH, ORCHESTRA, SFX;
    }

    private Type type;
    private KeyMap map;

    /**
     * Create a DrumKit with type=STANDARD and keyMap=GM
     */
    public DrumKit()
    {
        type = Type.STANDARD;
        map = KeyMapGM.getInstance();
    }

    public DrumKit(Type type, KeyMap map)
    {
        if (type == null || map == null)
        {
            throw new IllegalArgumentException("type=" + type + " map=" + map);
        }
        this.type = type;
        this.map = map;
    }

    /**
     * @return the type
     */
    public Type getType()
    {
        return type;
    }

    /**
     * @return the map
     */
    public KeyMap getKeyMap()
    {
        return map;
    }

    @Override
    public String toString()
    {
        return "[Type:" + type + ", KeyMap:" + map.getName() + "]";
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.type);
        hash = 17 * hash + Objects.hashCode(this.map);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final DrumKit other = (DrumKit) obj;
        if (this.type != other.type)
        {
            return false;
        }
        if (!Objects.equals(this.map, other.map))
        {
            return false;
        }
        return true;
    }

}

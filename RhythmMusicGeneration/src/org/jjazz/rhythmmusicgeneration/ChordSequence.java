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

import java.util.ArrayList;
import java.util.logging.Logger;
import org.jjazz.harmony.TimeSignature;
import org.jjazz.leadsheet.chordleadsheet.api.item.CLI_ChordSymbol;
import org.jjazz.leadsheet.chordleadsheet.api.item.CLI_Factory;
import org.jjazz.leadsheet.chordleadsheet.api.item.ChordRenderingInfo;
import org.jjazz.leadsheet.chordleadsheet.api.item.ExtChordSymbol;
import org.jjazz.leadsheet.chordleadsheet.api.item.Position;
import org.jjazz.songstructure.api.SongStructure;
import org.jjazz.util.Range;

/*
 * A convenience class to manipulate chord symbols sequences.
 *
 * User is responsible to ensure CLI_ChordSymbols are added in the right position order and in the startBar/nbBars range.
 */
public class ChordSequence extends ArrayList<CLI_ChordSymbol> implements Comparable<ChordSequence>, Cloneable
{

    private static int HASH_CODE_COUNTER = 0;
    private int hashCode;
    private int startBar;
    private int nbBars;

    protected static final Logger LOGGER = Logger.getLogger(ChordSequence.class.getSimpleName());

    public ChordSequence(int startBar, int nbBars)
    {
        super();
        if (startBar < 0 || nbBars < 0)
        {
            throw new IllegalArgumentException("startBar=" + startBar + " nbBars=" + nbBars);
        }
        this.startBar = startBar;
        this.nbBars = nbBars;
        hashCode = HASH_CODE_COUNTER++;
    }

    /**
     *
     * @return A shallow copy (CLI_ChordSymbols are not cloned).
     */
    @Override
    public ChordSequence clone()
    {
        ChordSequence cSeq = new ChordSequence(startBar, nbBars);
        for (CLI_ChordSymbol cs : this)
        {
            cSeq.add(cs);
        }
        return cSeq;
    }

    public final int getNbBars()
    {
        return nbBars;
    }

    public final int getStartBar()
    {
        return startBar;
    }

    public final Range getRange()
    {
        return new Range(startBar, startBar + nbBars - 1);
    }

    /**
     * Override hashCode to use a constant for each object, ie it does not depend on ChordSequence contents.
     * <p>
     * This way ChordSequence objects can be used as HashMap keys.
     *
     * @return
     */
    @Override
    public int hashCode()
    {
        return hashCode;
    }

    /**
     * @param o
     * @return True if o==this;
     */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object o)
    {
        return this == o;
    }

    /**
     * Return the duration in natural beats of the chord at specified index.
     * <p>
     * This is the duration until next chord or the end of the chordsequence.
     *
     * @param chordIndex
     * @param ts
     * @return
     */
    public float getChordDuration(int chordIndex, TimeSignature ts)
    {
        if (chordIndex < 0 || chordIndex >= size())
        {
            throw new IllegalArgumentException("chordIndex=" + chordIndex);
        }
        Position pos = get(chordIndex).getPosition();
        Position nextPos;
        if (chordIndex == size() - 1)
        {
            // Duration until end of the sequence
            nextPos = new Position(startBar + nbBars, 0);
        } else
        {
            // Duration until next chord
            nextPos = get(chordIndex + 1).getPosition();
        }
        float duration = pos.getDuration(nextPos, ts);
        return duration;
    }

    /**
     * Get the CLI_ChordSymbol from this ChordSequence which is active at posInBeats for specified sgs.
     *
     * @param sgs
     * @param posInBeats
     * @return Can be null.
     */
    public CLI_ChordSymbol getChordSymbol(SongStructure sgs, float posInBeats)
    {
        for (int i = size() - 1; i >= 0; i--)
        {
            CLI_ChordSymbol cliCs = get(i);
            Position pos = cliCs.getPosition();
            float cliPosInBeats = sgs.getPositionInNaturalBeats(pos.getBar()) + pos.getBeat();
            if (posInBeats >= cliPosInBeats)
            {
                return cliCs;
            }
        }
        return null;
    }

    /**
     * A new sub-sequence from this sequence.
     *
     * @param subStartBar        Chords from startBar are included
     * @param subEndBar          Chords until endBar (included) are included
     * @param addInitChordSymbol If true, try to add an init chordsymbol if the resulting subsequence does not have one: reuse the
     *                           last chord symbol before subStartBar.
     * @return
     */
    public ChordSequence subSequence(int subStartBar, int subEndBar, boolean addInitChordSymbol)
    {
        // LOGGER.severe("subSequence() -- subStartBar=" + subStartBar + ", subEndBar=" + subEndBar + ", addInitChordSymbol=" + addInitChordSymbol + ", this=" + this);
        if (subStartBar < startBar || subEndBar > (startBar + nbBars - 1))
        {
            throw new IllegalArgumentException("subStartBar=" + subStartBar + " subEndBar=" + subEndBar + " this=" + this);
        }
        ChordSequence cSeq = new ChordSequence(subStartBar, subEndBar - subStartBar + 1);
        for (CLI_ChordSymbol cs : this)
        {
            int bar = cs.getPosition().getBar();
            if (bar < subStartBar)
            {
                continue;
            }
            if (bar > subEndBar)
            {
                break;
            }
            cSeq.add(cs);
        }
        // LOGGER.severe("subSequence()   cSeq=" + cSeq);
        if (subStartBar > startBar && (cSeq.isEmpty() || !cSeq.get(0).getPosition().equals(new Position(subStartBar, 0))))
        {
            int index = indexOfLastBeforeBar(subStartBar);
            // LOGGER.severe("subSequence()   index=" + index);
            if (index != -1)
            {
                CLI_ChordSymbol newCs = cSeq.getInitCopy(get(index));
                cSeq.add(0, newCs);
            }
        }
        return cSeq;
    }

    /**
     * Return the index of the first chord whose bar position is absoluteBarIndex or after.
     *
     * @param absoluteBarIndex
     * @return -1 if not found.
     */
    public int indexOfFirstFromBar(int absoluteBarIndex)
    {
        for (int i = 0; i < size(); i++)
        {
            if (get(i).getPosition().getBar() >= absoluteBarIndex)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get the last chord (with absolute position) whose bar position is before absoluteBarIndex
     *
     * @param absoluteBarIndex Must be &gt;= 1.
     * @return -1 if no chord found before absoluteBarIndex.
     */
    public int indexOfLastBeforeBar(int absoluteBarIndex)
    {
        if (absoluteBarIndex < 1)
        {
            throw new IllegalArgumentException("absoluteBarIndex=" + absoluteBarIndex);
        }
        int index = -1;
        int firstIndex = indexOfFirstFromBar(absoluteBarIndex);
        if (isEmpty())
        {
            // Do nothing
        } else if (firstIndex == -1)
        {
            index = size() - 1;
        } else if (firstIndex > 0)
        {
            index = firstIndex - 1;
        } else
        {
            // Do nothing
        }
        return index;
    }

    /**
     *
     * @return True if there is a chord on bar=startBar, beat=0.
     */
    public final boolean hasChordAtBeginning()
    {
        if (isEmpty())
        {
            return false;
        }
        Position pos = get(0).getPosition();
        return pos.getBar() == startBar && pos.getBeat() == 0;
    }

    /**
     * Comparison is only based on startBar.
     *
     * @param cSeq
     * @return
     */
    @Override
    public int compareTo(ChordSequence cSeq)
    {
        return Integer.valueOf(startBar).compareTo(cSeq.startBar);
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("cSeq<start=").append(startBar).append(", nbBars=").append(nbBars).append(":").append(super.toString()).append(">");
        return sb.toString();
    }

    // ====================================================================================
    // Private methods
    // ====================================================================================
    /**
     * Prepare a copy of the specified CLI_ChordSymbol to be put at first bar/beat of this chord sequence.
     * <p>
     * As it is a copy, reset the ChordRenderingInfo PlayStyle and OffBeatStyle, and the alternate chord symbol.
     *
     * @param cliCs
     * @return
     */
    protected CLI_ChordSymbol getInitCopy(CLI_ChordSymbol cliCs)
    {
        // Add a copy of the chord symbol
        ExtChordSymbol ecs = cliCs.getData();
        Position newPos = new Position(startBar, 0);
        ChordRenderingInfo newCri = new ChordRenderingInfo(ChordRenderingInfo.PlayStyle.NORMAL, true, ecs.getRenderingInfo().getScaleInstance());
        ExtChordSymbol newEcs = new ExtChordSymbol(ecs, newCri, null, null);
        CLI_ChordSymbol res = CLI_Factory.getDefault().createChordSymbol(cliCs.getContainer(), newEcs, newPos);
        return res;
    }

}

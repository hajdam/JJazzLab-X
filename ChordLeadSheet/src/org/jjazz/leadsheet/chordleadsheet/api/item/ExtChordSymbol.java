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
package org.jjazz.leadsheet.chordleadsheet.api.item;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.text.ParseException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jjazz.harmony.ChordSymbol;
import org.jjazz.harmony.ChordType;
import org.jjazz.harmony.ChordTypeDatabase;
import org.jjazz.harmony.Degree;
import org.jjazz.harmony.Note;
import org.jjazz.harmony.SymbolicDuration;

/**
 * An extended chord symbol with additionnal features:
 * <p>
 * - Chord rendering information<br>
 * - An optional conditionnally-enabled alternate chord symbol.
 * <p>
 * This is an immutable class.
 */
public class ExtChordSymbol extends ChordSymbol implements Serializable
{

    private ChordRenderingInfo renderingInfo;
    private AltExtChordSymbol altChordSymbol;
    private AltDataFilter altFilter;
    private static final Logger LOGGER = Logger.getLogger(ExtChordSymbol.class.getSimpleName());

    /**
     * Create a 'C' chord symbol with an empty rendering info and no alternate chord symbol.
     */
    public ExtChordSymbol()
    {
        this(new Note(0), ChordTypeDatabase.getInstance().getChordType(""));
    }

    public ExtChordSymbol(Note rootDg, ChordType ct)
    {
        this(rootDg, rootDg, ct);
    }

    /**
     * Create a ChordSymbol with an empty RenderingInfo and no alternate chord symbol.
     *
     * @param rootDg
     * @param bassDg
     * @param ct
     */
    public ExtChordSymbol(Note rootDg, Note bassDg, ChordType ct)
    {
        this(rootDg, bassDg, ct, null, null, null);
    }

    public ExtChordSymbol(ChordSymbol cs, ChordRenderingInfo rInfo, AltExtChordSymbol altChordSymbol, AltDataFilter altFilter)
    {
        this(cs.getRootNote(), cs.getBassNote(), cs.getChordType(), rInfo, altChordSymbol, altFilter);
    }

    /**
     * The full-parameter constructor.
     *
     * @param rootDg
     * @param bassDg
     * @param ct
     * @param rInfo          If null create an empty ChordRenderingInfo.
     * @param altChordSymbol Optional alternate chord symbol. If not null altFilter must be also non-null.
     * @param altFilter      Optional filter to enable the use of the alternate chord symbol. If not null altChordSymbol must be
     *                       also non-null.
     */
    public ExtChordSymbol(Note rootDg, Note bassDg, ChordType ct, ChordRenderingInfo rInfo, AltExtChordSymbol altChordSymbol, AltDataFilter altFilter)
    {
        super(rootDg, bassDg, ct);
        renderingInfo = (rInfo == null) ? new ChordRenderingInfo() : rInfo;
        if ((altChordSymbol == null && altFilter != null) || (altChordSymbol != null && altFilter == null))
        {
            throw new IllegalArgumentException("rootDg=" + rootDg + " bassDg=" + bassDg + " ct=" + ct + " rInfo=" + rInfo + " altChordSymbol=" + altChordSymbol + " altFilter=" + altFilter);
        }
        this.altChordSymbol = altChordSymbol;
        this.altFilter = altFilter;
    }

    /**
     * Create a ChordSymbol from a chord string specification, with an empty RenderingInfo and no alternate chord symbol.
     *
     * @param s Eg 'C7'
     * @throws ParseException
     */
    public ExtChordSymbol(String s) throws ParseException
    {
        this(s, null, null, null);
    }

    /**
     * Create a ChordSymbol from a chord string specification, with the specified RenderingInfo and alternate chord symbol.
     *
     * @param s              Eg 'C7'
     * @param rInfo          If null create an empty ChordRenderingInfo.
     * @param altChordSymbol Optional alternate chord symbol. If not null altFilter must be also non-null.
     * @param altFilter      Optional filter to enable the use of the alternate chord symbol. If not null altChordSymbol must be
     *                       also non-null.
     *
     * @throws ParseException
     */
    public ExtChordSymbol(String s, ChordRenderingInfo rInfo, AltExtChordSymbol altChordSymbol, AltDataFilter altFilter) throws ParseException
    {
        super(s);
        renderingInfo = (rInfo == null) ? new ChordRenderingInfo() : rInfo;
        if ((altChordSymbol == null && altFilter != null) || (altChordSymbol != null && altFilter == null))
        {
            throw new IllegalArgumentException("s=" + s + " rInfo=" + rInfo + " altChordSymbol=" + altChordSymbol + " altFilter=" + altFilter);
        }
        this.altChordSymbol = altChordSymbol;
        this.altFilter = altFilter;
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
        if (super.equals(obj) == false)
        {
            return false;
        }
        final ExtChordSymbol other = (ExtChordSymbol) obj;
        if (!Objects.equals(this.renderingInfo, other.renderingInfo))
        {
            return false;
        }
        if (!Objects.equals(this.altChordSymbol, other.altChordSymbol))
        {
            return false;
        }
        if (!Objects.equals(this.altFilter, other.altFilter))
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.renderingInfo);
        hash = 97 * hash + Objects.hashCode(this.altChordSymbol);
        hash = 97 * hash + Objects.hashCode(this.altFilter);
        return hash;
    }

    /**
     * Get this object or the alternate chord symbol, depending on the specified string.
     * <p>
     * Return this object if : <br>
     * - altDataFilterString is null, or <br>
     * - alternate ChordSymbol is null, or <br>
     * - the AltDataFilter is null, or <br>
     * - the non-null AltDataFilter does not accept the specified string.<p>
     * Otherwise return getAlternateChordSymbol().
     *
     * @param altDataFilterString String to be passed to the AltDataFilter
     * @return
     */
    public ExtChordSymbol getChordSymbol(String altDataFilterString)
    {
        if (altDataFilterString == null || altChordSymbol == null || altFilter == null || !altFilter.accept(altDataFilterString))
        {
            return this;
        } else
        {
            return getAlternateChordSymbol();
        }
    }

    /**
     * Get the optional alternate chord symbol.
     *
     * @return If null getAlternateFilter() will also return null.
     */
    public AltExtChordSymbol getAlternateChordSymbol()
    {
        return this.altChordSymbol;
    }

    /**
     * Get the optional filter used to check if we need to use the alternate chord symbol.
     * <p>
     *
     * @return If null getAlternateChordSymbol() will also return null.
     */
    public AltDataFilter getAlternateFilter()
    {
        return this.altFilter;
    }

    /**
     * Adapt a source note from this chord symbol to a destination chord symbol.
     * <p>
     * If source note is a degree of the source chord symbol (ex: G=b7 for A7) :<br>
     * - Try to reapply it to the destination chord symbol. Ex: G becomes B for C7M dest chord. <br>
     * - If NOK(Ex: G=b7 becomes ? for C dest chord), try to use the destination scales if present.<br>
     * - If scales NOK, make some assumptions to find the "best" possible note.
     * <p>
     * If source note is NOT a source chord symbol degree (ex: D for A7), return -1.
     *
     * @param srcRelPitch The relative pitch of the source note (eg 2 for note D)
     * @param destEcs
     * @return The relative pitch of the destination note, or -1 if source note could not be fitted.
     */
    public int fitNote(int srcRelPitch, ExtChordSymbol destEcs)
    {
        int destRelPitch = -1;
        int srcRelPitchToRoot = Note.getNormalizedRelPitch(srcRelPitch - getRootNote().getRelativePitch());
        Degree srcDegree = getChordType().getDegree(srcRelPitchToRoot);
        if (srcDegree != null)
        {
            // srcNote is a source chord note, eg G for Eb7
            Degree destDegree = destEcs.getChordType().fitDegreeAdvanced(srcDegree, destEcs.renderingInfo.getScaleInstance());
            destRelPitch = Note.getNormalizedRelPitch(destDegree.getPitch() + destEcs.getRootNote().getPitch());
        }
        return destRelPitch;
    }

    /**
     * @return Additional info to help music generation programs render this chord. Can't be null.
     */
    public ChordRenderingInfo getRenderingInfo()
    {
        return renderingInfo;
    }

    /**
     * Get a transposed ExtChordSymbol.
     *
     * @param t The amount of transposition in semi-tons.
     * @return A new transposed ExtChordSymbol.
     */
    @Override
    public ExtChordSymbol getTransposedChordSymbol(int t)
    {
        ChordSymbol cs = super.getTransposedChordSymbol(t);
        ChordRenderingInfo cri = getRenderingInfo().getTransposed(t);
        AltExtChordSymbol altCs = (altChordSymbol == null) ? null : altChordSymbol.getTransposedChordSymbol(t);
        ExtChordSymbol ecs = new ExtChordSymbol(cs.getRootNote(), cs.getBassNote(), cs.getChordType(), cri, altCs, altFilter);
        return ecs;
    }

    /**
     * @return ExtChordSymbol A random chord symbol (random degree, random chord type)
     */
    public static ExtChordSymbol createRandomChordSymbol()
    {
        int p = Note.OCTAVE_STD * 12 + (int) Math.round(Math.random() * 12f);
        Note.Alteration alt = (Math.random() < .5) ? Note.Alteration.FLAT : Note.Alteration.SHARP;
        Note n = new Note(p, SymbolicDuration.QUARTER.getBeatDuration(), 64, alt);
        ChordTypeDatabase ctb = ChordTypeDatabase.getInstance();
        int index = (int) (ctb.getSize() * Math.random());
        ChordType ct = ctb.getChordType(index);
        ExtChordSymbol ecs = new ExtChordSymbol(n, n, ct);
        return ecs;
    }

    // --------------------------------------------------------------------- 
    // Private methods
    // ---------------------------------------------------------------------    
    // --------------------------------------------------------------------- 
    // Serialization
    // ---------------------------------------------------------------------
    private Object writeReplace()
    {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream stream)
            throws InvalidObjectException
    {
        throw new InvalidObjectException("Serialization proxy required");
    }

    private static class SerializationProxy implements Serializable
    {

        private static final long serialVersionUID = -6112620289882L;
        private final int spVERSION = 1;
        private final String spName;
        private final ChordRenderingInfo spRenderingInfo;
        private final AltExtChordSymbol spAltChordSymbol;
        private final AltDataFilter spAltFilter;
        // XStream can't deserialize the ° char : little hack to avoid the issue
        private static final String DOT_REPLACEMENT = "_UpperDot_";

        private SerializationProxy(ExtChordSymbol cs)
        {
            spName = cs.getOriginalName().replace("°", DOT_REPLACEMENT);
            spRenderingInfo = cs.getRenderingInfo();
            spAltChordSymbol = cs.getAlternateChordSymbol();
            spAltFilter = cs.getAlternateFilter();
        }

        private Object readResolve() throws ObjectStreamException
        {
            String s = spName.replace(DOT_REPLACEMENT, "°");
            ChordSymbol cs = null;
            try
            {
                cs = new ExtChordSymbol(s, spRenderingInfo, spAltChordSymbol, spAltFilter);
            } catch (ParseException e)
            {
                LOGGER.log(Level.WARNING, spName + ": Invalid chord symbol. Using 'C' ChordSymbol instead.");
                cs = new ExtChordSymbol();
            }
            return cs;
        }
    }
}

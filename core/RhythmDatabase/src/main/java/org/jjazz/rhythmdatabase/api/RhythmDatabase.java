/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *  Copyright @2019 Jerome Lelasseux. All rights reserved.
 *
 *  This file is part of the JJazzLab software.
 *
 *  JJazzLab is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License (LGPLv3)
 *  as published by the Free Software Foundation, either version 3 of the License,
 *  or (at your option) any later version.
 *
 *  JJazzLab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JJazzLab.  If not, see <https://www.gnu.org/licenses/>
 *
 *  Contributor(s):
 */
package org.jjazz.rhythmdatabase.api;

import org.jjazz.rhythmdatabase.spi.RhythmDatabaseFactory;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.swing.event.ChangeListener;
import org.jjazz.harmony.api.TimeSignature;
import org.jjazz.rhythm.api.AdaptedRhythm;
import org.jjazz.rhythm.api.Rhythm;
import org.jjazz.rhythm.spi.RhythmProvider;
import org.jjazz.rhythm.spi.StubRhythmProvider;

/**
 * A RhythmDatabase is a collection of rhythms.
 * <p>
 * RhythmInfo instances are used to describe the available rhythms. Use getRhythmInstance(RhythmInfo) to get the Rhythm instance from a RhythmInfo instance.
 */
public interface RhythmDatabase
{

    /**
     * A helper method which just calls RhythmDatabaseFactory.getDefault().get().
     *
     * @return
     */
    static public RhythmDatabase getDefault()
    {
        var res = RhythmDatabaseFactory.getDefault().get();
        return res;
    }


    /**
     * Get a rhythm instance from its id.
     * <p>
     * If rId contains 2 instances of the AdaptedRhythm.RHYTHM_ID_DELIMITER, then this id represents an AdaptedRhythm which is created on demand, see
     * AdaptedRhythm.getUniqueId().In that case, the rhythm provider, the original rhythm and the time signature are obtained from rId, and the returned rhythm
     * instance is obtained by calling RhythmProvider.getAdaptedRhythmInstance(Rhythm, TimeSignature).
     * <p>
     * Rhythm instances can be cached.
     *
     * @param rId Unique rhythm id
     * @return The rhythm whose uniqueId matches rId
     * @throws org.jjazz.rhythmdatabase.api.UnavailableRhythmException
     * @see AdaptedRhythm#getUniqueId()
     * @see RhythmProvider#getAdaptedRhythm(org.jjazz.rhythm.api.Rhythm, org.jjazz.harmony.api.TimeSignature)
     */
    Rhythm getRhythmInstance(String rId) throws UnavailableRhythmException;


    /**
     * Get a rhythm instance from a RhythmInfo.
     * <p>
     * This might result in a lengthy operation (e.g. because of file reading). Rhythm instances are cached.
     *
     * @param rhythmInfo
     * @return
     * @throws org.jjazz.rhythmdatabase.api.UnavailableRhythmException
     */
    Rhythm getRhythmInstance(RhythmInfo rhythmInfo) throws UnavailableRhythmException;

    /**
     * Get the RhythmInfo instance from the specified rhythm unique id.
     * <p>
     * RhyhmtInfo instances are cached.
     *
     * @param rhythmId
     * @return Can be null if not found.
     */
    RhythmInfo getRhythm(String rhythmId);

    /**
     * Try to provide the rhythm instance which is an adapted version of r for a different time signature.
     * <p>
     * If the adapted rhythm could be obtained it is added in the database. AdaptedRhythm instances are cached.
     *
     * @param r
     * @param ts
     * @return Can be null if no adapted rhythm is available.
     * @throws IllegalArgumentException If ts is the time signature of r, or if r is not a rhythm of this database.
     */
    AdaptedRhythm getAdaptedRhythmInstance(Rhythm r, TimeSignature ts);

    /**
     * Get the rhythms which are tested OK.
     *
     * @param tester
     * @return
     */
    List<RhythmInfo> getRhythms(Predicate<RhythmInfo> tester);

    /**
     * Get the rhythms which match the specified time signature.
     *
     * @param ts TimeSignature
     * @return All rhythms corresponding to TimeSignature ts.
     */
    default List<RhythmInfo> getRhythms(TimeSignature ts)
    {
        Objects.requireNonNull(ts);
        return getRhythms(ri -> ri.timeSignature().equals(ts));
    }

    /**
     * The rhythms associated to the specified RhythmProvider
     *
     * @param rp
     * @return
     * @exception IllegalArgumentException If rp is not a RhythmProvider available.
     */
    List<RhythmInfo> getRhythms(RhythmProvider rp);

    /**
     * Try to find a rhythm in the database which is "similar" to the specified rhythm info.
     * <p>
     * "Similar" means at least share the same time signature. The default implementation uses RhythmFeatures.getMatchingScore() to help identify the most
     * similar rhythm.
     *
     * @param ri
     * @return A "similar" rhythm which at least share the same timesignature. Null if nothing relevant found.
     */
    default RhythmInfo getSimilarRhythm(RhythmInfo ri)
    {
        int max = -1;
        RhythmInfo res = null;
        for (RhythmInfo rii : getRhythms())
        {
            if (!rii.timeSignature().equals(ri.timeSignature()) || rii == ri)
            {
                continue;
            }
            int score = ri.rhythmFeatures().getMatchingScore(rii.rhythmFeatures());
            if (score > max)
            {
                max = score;
                res = rii;
            }
        }

        return res;
    }

    /**
     * @return All rhythms stored in the database.
     */
    default List<RhythmInfo> getRhythms()
    {
        return getRhythms(r -> true);
    }

    /**
     * @param rhythm
     * @return The RhythmProvider of the specified rhythm. Null if not found.
     */
    RhythmProvider getRhythmProvider(Rhythm rhythm);

    /**
     * Get the RhythmProvider for specified RhythmInfo.
     *
     * @param ri
     * @return The RhythmProvider of the specified RhythmInfo. Can be null.
     */
    RhythmProvider getRhythmProvider(RhythmInfo ri);


    /**
     * The RhythmProviders used by this database.
     *
     * @return
     */
    List<RhythmProvider> getRhythmProviders();

    /**
     * @return The list of TimeSignature for which we have at least 1 rhythm in the database
     */
    List<TimeSignature> getTimeSignatures();

    /**
     * Get the default Rhythm for TimeSignature ts.
     *
     * @param ts TimeSignature
     * @return Can not be null, but there is no guarantee that getRhythmInstance() on the returned value will work (e.g. if this RhythmInfo depends on a file
     *         which is no more available).
     */
    RhythmInfo getDefaultRhythm(TimeSignature ts);


    /**
     * Get the default stub rhythm for the specified TimeSignature.
     *
     * @param ts
     * @return Can't be null.
     */
    default Rhythm getDefaultStubRhythmInstance(TimeSignature ts)
    {
        var srp = StubRhythmProvider.getDefault();
        return srp.getStubRhythm(ts);
    }


    /**
     * Set the default rhythm for this TimeSignature.
     *
     * @param ts TimeSignature
     * @param ri
     * @exception IllegalArgumentException If rhythm is not part of this database or if ri is an AdaptedRhythm
     */
    void setDefaultRhythm(TimeSignature ts, RhythmInfo ri);

    /**
     * @return The number of rhythms in the database.
     */
    int size();

    /**
     * Add one RhythmInfo to the database for the specified RhythmProvider.
     * <p>
     * <p>
     * Fire a state changed event if RhythmInfo is actually added.
     *
     * @param rp
     * @param rInfo
     * @return True if rInfo was actually added.
     */
    boolean addRhythm(RhythmProvider rp, RhythmInfo rInfo);

    /**
     * Add one Rhythm instance to the database for the specified RhythmProvider.
     * <p>
     * Fire a state changed event if Rhythm is actually added.
     *
     * @param rp
     * @param r
     * @return True if r was actually added.
     */
    boolean addRhythmInstance(RhythmProvider rp, Rhythm r);

    /**
     * Listeners are notified when the rhythm database has changed.
     * <p>
     * Note that listeners might be notified out of the Event Dispatch Thread.
     *
     * @param l
     */
    void addChangeListener(ChangeListener l);

    void removeChangeListener(ChangeListener l);


    /**
     * Return a string with the contents of the database.
     *
     * @return
     */
    default String toContentString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("--------- rhythm database size=").append(size()).append("\n");
        for (RhythmInfo ri : getRhythms())
        {
            sb.append("  ").append(ri.toString());
            try
            {
                Rhythm r = getRhythmInstance(ri);
                sb.append(" --> ").append(r.getClass().getSimpleName());
                if (r.isResourcesLoaded())
                {
                    sb.append(" (resources loaded)");
                }
            } catch (UnavailableRhythmException ex)
            {
                // Nothing
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * A string showing statistics about the specified database.
     *
     * @return
     */
    default String toStatsString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("-------- Rhythm Database stats - total=%d\n", size()));

        for (RhythmProvider rp : getRhythmProviders())
        {
            var rhythms = getRhythms(rp);

            long nbBuiltins = rhythms.stream()
                    .filter(ri -> ri.file().getName().equals(""))
                    .count();

            long nbFiles = rhythms.size() - nbBuiltins;
            String firstRhythm = rhythms.isEmpty() ? "" : "first=" + rhythms.get(0).toString() + "...";

            String s = String.format("  > %s: total=%d builtin=%d file=%d %s\n", rp.getInfo().getName(), rhythms.size(), nbBuiltins, nbFiles, firstRhythm);
            sb.append(s);
        }

        return sb.toString();
    }

}

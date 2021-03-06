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
package org.jjazz.midimix;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.event.SwingPropertyChangeSupport;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.UndoableEdit;
import org.jjazz.leadsheet.chordleadsheet.api.UnsupportedEditException;
import org.jjazz.midi.DrumKit;
import org.jjazz.midi.Instrument;
import org.jjazz.midi.synths.GM1Instrument;
import org.jjazz.midi.InstrumentMix;
import org.jjazz.midi.InstrumentSettings;
import org.jjazz.midi.MidiConst;
import org.jjazz.midi.synths.Family;
import static org.jjazz.midimix.Bundle.ERR_NotEnoughChannels;
import org.jjazz.rhythm.api.Rhythm;
import org.jjazz.rhythm.api.RhythmVoice;
import org.jjazz.rhythm.database.api.RhythmDatabase;
import org.jjazz.songstructure.api.event.SgsChangeEvent;
import org.jjazz.songstructure.api.event.SptAddedEvent;
import org.jjazz.songstructure.api.event.SptRemovedEvent;
import org.jjazz.songstructure.api.event.SptReplacedEvent;
import org.jjazz.song.api.Song;
import org.openide.util.NbBundle.Messages;
import org.jjazz.songstructure.api.SongStructure;
import org.jjazz.songstructure.api.SgsChangeListener;
import org.jjazz.songstructure.api.SongPart;
import org.jjazz.undomanager.JJazzUndoManager;
import org.jjazz.undomanager.JJazzUndoManagerFinder;
import org.jjazz.undomanager.SimpleEdit;
import org.jjazz.util.Utilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;

/**
 * A set of up to 16 InstrumentMixes, 1 per Midi channel with 1 RhythmVoice associated.
 * <p>
 * The object manages the solo functionality between the InstrumentMixes.<p>
 * A Song can be associated to the MidiMix so that InstrumentMixes are kept up to date with song's songStructure changes.<p>
 * If MidiMix is modified the corresponding property change event is fired (e.g. PROP_INSTRUMENT_MUTE) then the
 * PROP_MODIFIED_OR_SAVED change event is also fired.
 * <p>
 */
@Messages(
        {
            "ERR_NotEnoughChannels=Not enough available Midi channels"
        })
public class MidiMix implements SgsChangeListener, PropertyChangeListener, Serializable
{

    public static final String MIX_FILE_EXTENSION = "mix";
    /**
     * New or removed InstrumentMix.
     * <p>
     * OldValue=the old InstrumentMix, newValue=channel
     */
    public static final String PROP_CHANNEL_INSTRUMENT_MIX = "ChannelInstrumentMix";
    /**
     * oldValue=channel, newValue=true/false.
     */
    public static final String PROP_CHANNEL_DRUMS_REROUTED = "ChannelDrumsRerouted";
    /**
     * oldValue=InstumentMix, newValue=mute boolean state.
     */
    public static final String PROP_INSTRUMENT_MUTE = "InstrumentMute";
    /**
     * A drums instrument has changed with different keymap.
     * <p>
     * oldValue=channel, newValue=old keymap (may be null)
     */
    public static final String PROP_DRUMS_INSTRUMENT_KEYMAP = "DrumsInstrumentKeyMap";
    /**
     * oldValue=InstumentMix, newValue=transposition value.
     */
    public static final String PROP_INSTRUMENT_TRANSPOSITION = "InstrumentTransposition";
    /**
     * oldValue=InstumentMix, newValue=velocity shift value.
     */
    public static final String PROP_INSTRUMENT_VELOCITY_SHIFT = "InstrumentVelocityShift";
    /**
     * This property changes when the MidiMix is modified (false-&gt;true) or saved (true-&gt;false).
     */
    public static final String PROP_MODIFIED_OR_SAVED = "PROP_MODIFIED_OR_SAVED";
    public static final int NB_AVAILABLE_CHANNELS = MidiConst.CHANNEL_MAX - MidiConst.CHANNEL_MIN + 1;

    /**
     * Store the instrumentMixes, one per Midi Channel.
     */
    private InstrumentMix[] instrumentMixes = new InstrumentMix[MidiConst.CHANNEL_MIN + NB_AVAILABLE_CHANNELS];
    /**
     * Optional RhythmVoice associated to an instrumentMix
     */
    private RhythmVoice[] rvKeys = new RhythmVoice[MidiConst.CHANNEL_MIN + NB_AVAILABLE_CHANNELS];
    /**
     * The InstrumentMixes with Solo ON
     */
    private transient HashSet<InstrumentMix> soloedInsMixes = new HashSet<>();
    /**
     * The channels which should be rerouted to the GM DRUMS channel, and the related saved config.
     */
    private transient HashMap<Integer, InstrumentMix> drumsReroutedChannels = new HashMap<>();
    /**
     * Saved Mute configuration on first soloed channel
     */
    private transient boolean[] saveMuteConfiguration = new boolean[MidiConst.CHANNEL_MIN + NB_AVAILABLE_CHANNELS];
    private transient List<UndoableEditListener> undoListeners = new ArrayList<>();
    /**
     * The file where MidiMix was saved.
     */
    private transient File file;
    private transient Song song;
    private transient boolean needSave = false;
    private SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);
    private static final Logger LOGGER = Logger.getLogger(MidiMix.class.getSimpleName());

    /**
     * Create an empty MidiMix.
     */
    public MidiMix()
    {

    }

    /**
     * create an empty MidiMix and associate it to specified song.
     *
     * @param s
     */
    public MidiMix(Song s)
    {
        setSong(s);
    }

    /**
     * Create a new MidiMix from an existing one.
     * <p>
     * New MidiMix will be initialized with copies of the InstrumentMixes of mm. RhythmVoice keys are directly reused.
     *
     * @param mm
     * @param r  If not null, keep only the data RhythmVoices/InstrumentMixes related to r
     */
    public MidiMix(MidiMix mm, Rhythm r)
    {
        try
        {
            addInstrumentMixes(mm, r);
        } catch (MidiUnavailableException ex)
        {
            LOGGER.log(Level.SEVERE, "MidiMix(MidiMix, Rhythm) unexpected exception!", ex);
        }
    }

    /**
     * Associate a song to this MidiMix : rhythm changes in the song's songStructure are listened to keep our instrumentMix up to
     * date.
     * <p>
     * Throw an exception if one of the current RhythmVoice keys does not belong to specified song.
     *
     * @param sg Can be null.
     */
    public final void setSong(Song sg)
    {
        if (song == sg)
        {
            return;
        }
        if (song != null)
        {
            song.getSongStructure().removeSgsChangeListener(this);
        }

        this.song = sg;
        if (song != null)
        {
            SongStructure sgs = song.getSongStructure();
            sgs.addSgsChangeListener(this);
            // Check consistency with existing data
            for (Integer channel : getUsedChannels())
            {
                RhythmVoice rvKey = getKey(channel);
                List<RhythmVoice> songRvs = SongStructure.Util.getUniqueRhythmVoices(sgs);
                if (!(rvKey instanceof UserChannelRvKey) && !songRvs.contains(rvKey))
                {
                    throw new IllegalArgumentException("channel=" + channel + " rvKey=" + rvKey + " songRvs=" + songRvs);
                }
            }
        }
    }

    public Song getSong()
    {
        return song;
    }

    /**
     * Add the special user channel/InstrumentMix to the mix.
     * <p>
     * Use the PREF_USER_CHANNEL if possible. If channel is already used, find the first channel available. The user channel will
     * use the UserChannelRhythmVoiceKey instance as key.
     *
     * @param insMix The instrument mix to be used for the user channel
     * @throws MidiUnavailableException If no Midi channel available.
     * @throws IllegalStateException    If a user channel is already added.
     */
    public void addUserChannel(final InstrumentMix insMix) throws MidiUnavailableException
    {
        if (insMix == null)
        {
            throw new NullPointerException("insMix");
        }
        if (getCurrentUserChannel() != -1)
        {
            throw new IllegalStateException("User channel already enabled on channel " + getCurrentUserChannel());
        }
        int prefUserChannel = UserChannelRvKey.getInstance().getPreferredUserChannel();
        final int channel = getUsedChannels().contains(prefUserChannel) ? findFreeChannel(false) : prefUserChannel;
        if (channel == -1)
        {
            throw new MidiUnavailableException("No Midi channels available");
        }
        // User channel should never be muted/soloed
        insMix.setMute(false);
        insMix.setSolo(false);

        // Perform the change
        changeInstrumentMix(channel, insMix, UserChannelRvKey.getInstance());

    }

    /**
     * Remove the special user channel/InstrumentMix.
     */
    public void removeUserChannel()
    {
        int channel = getCurrentUserChannel();
        if (channel != -1)
        {
            changeInstrumentMix(channel, null, null);
        }
    }

    /**
     * Return the Midi channel currently used when user channel is enabled.
     *
     * @return -1 if there is no user channel
     */
    public int getCurrentUserChannel()
    {
        int channel = getChannel(UserChannelRvKey.getInstance());
        return channel;
    }

    /**
     * Assign an InstrumentMix to a midi channel and to a key.
     * <p>
     * Replace any existing InstrumentMix associated to the midi channel. The solo and "drums rerouted channel" status of the new
     * InstrumentMix are reset to off. <br>
     * Fire a PROP_CHANNEL_INSTRUMENT_MIX change event for this channel, and one UndoableEvent.
     *
     * @param channel A valid midi channel number.
     * @param rvKey   Can be null if insMix is also null. If a song is set, must be a RhythmVoice of song's rhythms.
     * @param insMix  Can be null if rvKey is also null.
     * @throws IllegalArgumentException if insMix is already part of this MidiMix for a different channel
     */
    public void setInstrumentMix(int channel, RhythmVoice rvKey, InstrumentMix insMix)
    {
        if (!MidiConst.checkMidiChannel(channel) || (rvKey instanceof UserChannelRvKey) || (rvKey == null && insMix != null) || (rvKey != null && insMix == null))
        {
            throw new IllegalArgumentException("channel=" + channel + " rvKey=" + rvKey + " insMix=" + insMix);
        }
        LOGGER.fine("setInstrumentMix() channel=" + channel + " rvKey=" + rvKey + " insMix=" + insMix);
        if (rvKey != null && song != null)
        {
            // Check that rvKey belongs to song
            if (!SongStructure.Util.getUniqueRhythmVoices(song.getSongStructure()).contains(rvKey))
            {
                throw new IllegalArgumentException("channel=" + channel + " rvKey=" + rvKey + " insMix=" + insMix + ". rvKey does not belong to any of the song's rhythms.");
            }
        }
        if (insMix != null)
        {
            // Check the InstrumentMix is not already used for a different channel
            int ch = getInstrumentMixesPerChannel().indexOf(insMix);
            if (ch != -1 && ch != channel)
            {
                throw new IllegalArgumentException("channel=" + channel + " rvKey=" + rvKey + " im=" + insMix + ". im is already present in MidiMix at channel " + ch);
            }
        }

        changeInstrumentMix(channel, insMix, rvKey);
    }

    /**
     * @param channel A valid midi channel number
     * @return The InstrumentMix assigned to the specified Midi channel, or null if no InstrumentMix for this channel.
     */
    public InstrumentMix getInstrumentMixFromChannel(int channel)
    {
        if (!MidiConst.checkMidiChannel(channel))
        {
            throw new IllegalArgumentException("channel=" + channel);
        }
        return instrumentMixes[channel];
    }

    /**
     * @param rvKey
     * @return The InstrumentMix associated to rvKey. Null if no InstrumentMix found.
     */
    public InstrumentMix getInstrumentMixFromKey(RhythmVoice rvKey)
    {
        if (rvKey == null)
        {
            return null;
        }
        int index = Arrays.asList(rvKeys).indexOf(rvKey);
        return index == -1 ? null : instrumentMixes[index];
    }

    /**
     *
     * @param im
     * @return -1 if InstrumentMix not found.
     */
    public int getChannel(InstrumentMix im)
    {
        return Arrays.asList(instrumentMixes).indexOf(im);
    }

    /**
     *
     * @param im
     * @return null if InstrumentMix not found.
     */
    public RhythmVoice getKey(InstrumentMix im)
    {
        int index = getChannel(im);
        return index == -1 ? null : rvKeys[index];
    }

    /**
     *
     * @param channel
     * @return The RhythmVoice key corresponding to specified channel. Can be null.
     */
    public RhythmVoice getKey(int channel)
    {
        if (!MidiConst.checkMidiChannel(channel))
        {
            throw new IllegalArgumentException("channel=" + channel);
        }
        return rvKeys[channel];
    }

    /**
     *
     * @param rvKey
     * @return -1 if key not found.
     */
    public int getChannel(RhythmVoice rvKey)
    {
        if (rvKey == null)
        {
            throw new IllegalArgumentException("key=" + rvKey);
        }
        return Arrays.asList(rvKeys).indexOf(rvKey);
    }

    /**
     * Get the list of used channels in this MidiMix.
     *
     * @return The list of Midi channel numbers for which a non-null InstrumentMix is assigned.
     */
    public List<Integer> getUsedChannels()
    {
        ArrayList<Integer> channels = new ArrayList<>();
        for (int i = MidiConst.CHANNEL_MIN; i <= MidiConst.CHANNEL_MAX; i++)
        {
            if (instrumentMixes[i] != null)
            {
                channels.add(i);
            }
        }
        return channels;
    }

    /**
     * Get the list of used channels for specified rhythm in this MidiMix.
     *
     * @param r If null delegate to getUsedChannels()
     * @return The list of Midi channel numbers for rhythm r and for which a non-null InstrumentMix is assigned.
     */
    public List<Integer> getUsedChannels(Rhythm r)
    {
        if (r == null)
        {
            return getUsedChannels();
        }
        ArrayList<Integer> channels = new ArrayList<>();
        for (int i = MidiConst.CHANNEL_MIN; i <= MidiConst.CHANNEL_MAX; i++)
        {
            if (instrumentMixes[i] != null && rvKeys[i].getContainer() == r)
            {
                channels.add(i);
            }
        }
        return channels;
    }

    /**
     * @return The list of Midi channel numbers for which no InstrumentMix is assigned.
     */
    public List<Integer> getUnusedChannels()
    {
        ArrayList<Integer> channels = new ArrayList<>();
        for (int i = MidiConst.CHANNEL_MIN; i <= MidiConst.CHANNEL_MAX; i++)
        {
            if (instrumentMixes[i] == null)
            {
                channels.add(i);
            }
        }
        return channels;
    }

    /**
     * @return The list of InstrumentMix non-null keys.
     */
    public List<RhythmVoice> getRvKeys()
    {
        ArrayList<RhythmVoice> keys = new ArrayList<>();
        for (int i = 0; i < rvKeys.length; i++)
        {
            if (rvKeys[i] != null)
            {
                keys.add(rvKeys[i]);
            }
        }
        return keys;
    }

    /**
     * The channels which should be rerouted to the GM Drums channel.
     *
     * @return Note that returned list is not ordered. Can be empty.
     */
    public List<Integer> getDrumsReroutedChannels()
    {
        return new ArrayList<>(drumsReroutedChannels.keySet());
    }

    /**
     * Enable or disable the rerouting of specified channel to the GM Drums channel.
     * <p>
     * If enabled, the related InstrumentMix/Settings will be disabled, and vice versa.
     *
     * @param b
     * @param channel
     */
    public void setDrumsReroutedChannel(boolean b, int channel)
    {
        if (instrumentMixes[channel] == null)
        {
            throw new IllegalArgumentException("b=" + b + " channel=" + channel + " instrumentMixes=" + getInstrumentMixesPerChannel());
        }
        if (b == drumsReroutedChannels.keySet().contains(channel) || channel == MidiConst.CHANNEL_DRUMS)
        {
            return;
        }
        InstrumentMix insMix = instrumentMixes[channel];
        if (b)
        {
            // Save state
            InstrumentMix saveMixData = new InstrumentMix(insMix);
            drumsReroutedChannels.put(channel, saveMixData);
            // Disable all parameters since it's rerouted
            insMix.setInstrumentEnabled(false);
            insMix.getSettings().setChorusEnabled(false);
            insMix.getSettings().setReverbEnabled(false);
            insMix.getSettings().setPanoramicEnabled(false);
            insMix.getSettings().setVolumeEnabled(false);
        } else
        {
            InstrumentMix saveMixData = drumsReroutedChannels.get(channel);
            assert saveMixData != null : "b=" + b + " channel=" + channel + " this=" + this;
            drumsReroutedChannels.remove(channel);
            // Restore parameters enabled state
            insMix.setInstrumentEnabled(saveMixData.isInstrumentEnabled());
            insMix.getSettings().setChorusEnabled(saveMixData.getSettings().isChorusEnabled());
            insMix.getSettings().setReverbEnabled(saveMixData.getSettings().isReverbEnabled());
            insMix.getSettings().setPanoramicEnabled(saveMixData.getSettings().isPanoramicEnabled());
            insMix.getSettings().setVolumeEnabled(saveMixData.getSettings().isVolumeEnabled());
        }
        pcs.firePropertyChange(PROP_CHANNEL_DRUMS_REROUTED, channel, b);
        fireIsModified();
    }

    /**
     * Return a free channel to be used in this MidiMix.
     * <p>
     * Try to keep channels in one section above the drums channel reserved to Drums. If not enough channels extend to channel
     * below the drums channel.
     *
     * @param findDrumsChannel If true try to use CHANNEL_DRUMS if it is available.
     * @return -1 if no channel found
     */
    public int findFreeChannel(boolean findDrumsChannel)
    {
        List<Integer> usedChannels = getUsedChannels();
        if (findDrumsChannel && !usedChannels.contains(MidiConst.CHANNEL_DRUMS))
        {
            return MidiConst.CHANNEL_DRUMS;
        }

        // First search channels above Drums channel
        for (int channel = MidiConst.CHANNEL_DRUMS + 1; channel <= MidiConst.CHANNEL_MAX; channel++)
        {
            if (!usedChannels.contains(channel))
            {
                return channel;
            }
        }
        for (int channel = MidiConst.CHANNEL_DRUMS - 1; channel >= MidiConst.CHANNEL_MIN; channel--)
        {
            if (!usedChannels.contains(channel))
            {
                return channel;
            }
        }
        return -1;
    }

    /**
     * Add new InstrumentMixes and related keys from mm into this MidiMix.
     * <p>
     * Create copies of each added InstrumentMix. Copies have solo/drumsRerouting set to OFF.<p>
     * Method uses findFreeChannel() to allocate the new channels of mm if they are not free in this MidiMix.
     * <p>
     * The operation will fire UndoableEvent edits.
     *
     * @param fromMm
     * @param r      If non null, copy fromMm instrumentMixes only if they belong to rhythm r
     * @throws MidiUnavailableException If not enough channels available to accommodate mm instruments.
     */
    public final void addInstrumentMixes(MidiMix fromMm, Rhythm r) throws MidiUnavailableException
    {
        LOGGER.log(Level.FINE, "copyInstrumentMixes() -- rvKeys={0} mm.rvKeys={1}", new Object[]
        {
            getRvKeys(), fromMm.getRvKeys()
        });
        List<Integer> fromUsedChannels = (r == null) ? fromMm.getUsedChannels() : fromMm.getUsedChannels(r);
        if (getUnusedChannels().size() < fromUsedChannels.size())
        {
            throw new MidiUnavailableException(ERR_NotEnoughChannels());
        }
        List<Integer> usedChannels = getUsedChannels();
        for (Integer fromChannel : fromUsedChannels)
        {
            RhythmVoice fromRvKey = fromMm.getKey(fromChannel);
            InstrumentMix fromInsMix = fromMm.getInstrumentMixFromChannel(fromChannel);
            if (fromRvKey == UserChannelRvKey.getInstance())
            {
                // Copy the user channel by remove/add
                removeUserChannel();
                addUserChannel(new InstrumentMix(fromInsMix));
            } else
            {
                // Normal channel
                int newChannel = usedChannels.contains(fromChannel) ? findFreeChannel(fromRvKey.isDrums()) : fromChannel;
                assert newChannel != -1 : " usedChannels=" + usedChannels;
                usedChannels.add(newChannel);
                setInstrumentMix(newChannel, fromRvKey, new InstrumentMix(fromInsMix));
            }
        }
        LOGGER.log(Level.FINE, "copyInstrumentMixes()     exit : rvKeys={0}", getRvKeys());
    }

    /**
     * The file where this object is stored.
     *
     * @return Null if not set.
     */
    public File getFile()
    {
        return file;
    }

    public void setFile(File f)
    {
        file = f;
    }

    /**
     * Import InstrumentMixes from mm into this object.
     * <p>
     * Import is first done on matching RhythmVoices from the same rhythm. Then import only when RvTypes match.<br>
     * Create new copy instances of Instruments Mixes with solo OFF.
     * <p>
     * The operation will fire UndoableEvent(s).
     *
     * @param mm
     */
    public void importInstrumentMixes(MidiMix mm)
    {
        if (mm == null)
        {
            throw new NullPointerException("mm");
        }
        if (mm.rvKeys.length != rvKeys.length)
        {
            throw new IllegalStateException("mm.rvKeys.length=" + mm.rvKeys.length + " rvKeys.length=" + rvKeys.length);
        }

        // Find the matching voices except the USER channel
        HashMap<RhythmVoice, RhythmVoice> mapMatchingVoices = new HashMap<>();
        List<RhythmVoice> rvs = getRvKeys();
        List<RhythmVoice> mmRvs = mm.getRvKeys();
        for (RhythmVoice mmRv : mmRvs)
        {
            boolean matched = false;

            // Try first on matching RhythmVoices from same rhythm
            for (RhythmVoice rv : rvs.toArray(new RhythmVoice[0]))
            {
                if (!(rv instanceof UserChannelRvKey) && mmRv == rv)
                {
                    mapMatchingVoices.put(mmRv, rv);
                    rvs.remove(rv);
                    matched = true;
                    break;
                }
            }
            if (!matched)
            {
                // If no match, try any channel with the same RvType
                for (RhythmVoice rv : rvs.toArray(new RhythmVoice[0]))
                {
                    if (!(rv instanceof UserChannelRvKey) && !(mmRv instanceof UserChannelRvKey) && mmRv.getType().equals(rv.getType()))
                    {
                        mapMatchingVoices.put(mmRv, rv);
                        rvs.remove(rv);
                        matched = true;
                        break;
                    }
                }
            }
        }

        // Copy the InstrumentMixes
        for (RhythmVoice mmRv : mapMatchingVoices.keySet())
        {
            InstrumentMix mmInsMix = mm.getInstrumentMixFromKey(mmRv);
            RhythmVoice rv = mapMatchingVoices.get(mmRv);
            int channel = getChannel(rv);
            setInstrumentMix(channel, rv, new InstrumentMix(mmInsMix));
        }

        // Copy the instrumentmix of the User channel if present 
        if (getCurrentUserChannel() != -1 && mm.getCurrentUserChannel() != -1)
        {
            // Replace the existing user intrumentMix
            removeUserChannel();
            InstrumentMix mmInsMix = mm.getInstrumentMixFromChannel(mm.getCurrentUserChannel());
            try
            {
                addUserChannel(new InstrumentMix(mmInsMix));
            } catch (MidiUnavailableException ex)
            {
                // Should never happen since we had enough Midi channels
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * @return True if MidiMix has some unsaved changes.
     */
    public boolean needSave()
    {
        return needSave;
    }

    /**
     * Same as saveToFile() but notify user if problems.
     *
     * @param f
     * @param isCopy
     * @return False if a problem occured
     */
    public boolean saveToFileNotify(File f, boolean isCopy)
    {
        if (f == null)
        {
            throw new IllegalArgumentException("f=" + f);
        }

        boolean b = true;
        if (f.exists() && !f.canWrite())
        {
            String msg = "Can not overwrite " + f.getAbsolutePath();
            LOGGER.log(Level.WARNING, "saveToFileNotify() " + msg);
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            b = false;
        }
        if (b)
        {
            try
            {
                saveToFile(f, isCopy);
            } catch (IOException ex)
            {
                String msg = "Problem saving song mix file " + f.getAbsolutePath() + " : " + ex.getLocalizedMessage();
                if (ex.getCause() != null)
                {
                    msg += "\n" + ex.getCause().getLocalizedMessage();
                }
                LOGGER.warning("saveToFileNotify() " + msg);
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                b = false;
            }
        }
        return b;
    }

    /**
     * Save this MidiMix to a file.
     * <p>
     * This will fire a PROP_MODIFIED_OR_SAVED change event (true=&gt;false).
     *
     * @param f
     * @param isCopy Indicate that we save a copy, ie perform the file save but nothing else (eg no PROP_MODIFIED_OR_SAVED state
     *               change)
     * @throws java.io.IOException
     */
    public void saveToFile(File f, boolean isCopy) throws IOException
    {
        if (f == null)
        {
            throw new IllegalArgumentException("f=" + f + " isCopy=" + isCopy);
        }
        LOGGER.fine("saveToFile() f=" + f.getAbsolutePath() + " isCopy=" + isCopy);

        if (!isCopy)
        {
            file = f;
        }

        try (FileOutputStream fos = new FileOutputStream(f))
        {
            XStream xstream = new XStream();
            xstream.alias("MidiMix", MidiMix.class);
            xstream.toXML(this, fos);
            if (!isCopy)
            {
                pcs.firePropertyChange(PROP_MODIFIED_OR_SAVED, true, false);
            }
        } catch (IOException e)
        {
            if (!isCopy)
            {
                file = null;
            }
            throw new IOException(e);
        } catch (XStreamException e)
        {
            if (!isCopy)
            {
                file = null;
            }
            LOGGER.warning("saveToFile() exception=" + e.getLocalizedMessage());
            // Translate into an IOException to be handled by the Netbeans framework 
            throw new IOException("XStream XML unmarshalling error", e);
        }
    }

    /**
     * All InstrumentMixes ordered by channel.
     *
     * @return A 16 items list, one instrumentMix per channel (some items can be null)
     */
    public List<InstrumentMix> getInstrumentMixesPerChannel()
    {
        return Arrays.asList(instrumentMixes);
    }

    /**
     * The non-null instrument mixes ordered by channel.
     *
     * @return Can be an empty list.
     */
    public List<InstrumentMix> getInstrumentMixes()
    {
        ArrayList<InstrumentMix> insMixes = new ArrayList<>();
        for (InstrumentMix im : instrumentMixes)
        {
            if (im != null)
            {
                insMixes.add(im);
            }
        }
        return insMixes;
    }

    public void addUndoableEditListener(UndoableEditListener l)
    {
        if (l == null)
        {
            throw new NullPointerException("l=" + l);
        }
        undoListeners.remove(l);
        undoListeners.add(l);
    }

    public void removeUndoableEditListener(UndoableEditListener l)
    {
        if (l == null)
        {
            throw new NullPointerException("l=" + l);
        }
        undoListeners.remove(l);
    }

    public void addPropertyListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    @Override
    public String toString()
    {
        return "MidiMix[song=" + song + ", channels=" + Arrays.toString(getUsedChannels().toArray(new Integer[0])) + "]";
    }

    /**
     *
     * @param f
     * @return Null if MidiMix could not be created for some reason.
     * @throws java.io.IOException If problem occured while reading file
     */
    public static MidiMix loadFromFile(File f) throws IOException
    {
        if (f == null)
        {
            throw new IllegalArgumentException("f=" + f);
        }
        MidiMix mm = null;
        try
        {
            XStream xstream = new XStream();
            mm = (MidiMix) xstream.fromXML(f);
            mm.setFile(f);
        } catch (XStreamException e)
        {
            LOGGER.warning("loadFromFile() XStreamException e=" + e);   // Important in order to get the details of the XStream error
            throw new IOException("XStream loading error", e);         // Translate into an IOException to be handled by the Netbeans framework 
        }
        return mm;
    }

    //-----------------------------------------------------------------------
    // Implementation of the SgsChangeListener interface
    //-----------------------------------------------------------------------
    @Override
    public void songStructureChanged(SgsChangeEvent e) throws UnsupportedEditException
    {
        LOGGER.fine("songStructureChanged() -- e=" + e);

        JJazzUndoManager um = JJazzUndoManagerFinder.getDefault().get(song);
        if (um != null && um.isUndoRedoInProgress())
        {
            // IMPORTANT : MidiMix generates his own undoableEdits
            // so we must not listen to SongStructure changes if undo/redo in progress, otherwise 
            // the "undo/redo" restore operations will be performed twice !
            return;
        }

        List<Rhythm> songRhythms = SongStructure.Util.getUniqueRhythms(song.getSongStructure());
        List<Rhythm> mixRhythms = getUniqueRhythms();

        if (e instanceof SptAddedEvent)
        {
            if (SongStructure.Util.getUniqueRhythmVoices(song.getSongStructure()).size() > NB_AVAILABLE_CHANNELS)
            {
                // No need to go further, we don't have enough Midi channels
                throw new UnsupportedEditException(ERR_NotEnoughChannels());
            }
            SptAddedEvent e2 = (SptAddedEvent) e;
            for (SongPart spt : e2.getSongParts())
            {
                Rhythm r = spt.getRhythm();
                if (!mixRhythms.contains(r))
                {
                    try
                    {
                        // It's a new rhythm in the MidiMix
                        addRhythm(r);
                    } catch (MidiUnavailableException ex)
                    {
                        // Should not be here since we made a test just above to avoid this
                        throw new IllegalStateException("Unexpected MidiUnavailableException !", ex);
                    }
                    mixRhythms.add(r);
                }
            }
        } else if (e instanceof SptRemovedEvent)
        {
            SptRemovedEvent e2 = (SptRemovedEvent) e;
            for (SongPart spt : e2.getSongParts())
            {
                Rhythm r = spt.getRhythm();
                if (!songRhythms.contains(r) && mixRhythms.contains(r))
                {
                    // There is no more such rhythm in the song, we can remove it from the midimix
                    removeRhythm(r);
                    mixRhythms.remove(r);
                }
            }
        } else if (e instanceof SptReplacedEvent)
        {
            if (SongStructure.Util.getUniqueRhythmVoices(song.getSongStructure()).size() > NB_AVAILABLE_CHANNELS)
            {
                // No need to go further, we don't have enough Midi channels
                throw new UnsupportedEditException(ERR_NotEnoughChannels());
            }
            SptReplacedEvent e2 = (SptReplacedEvent) e;
            List<SongPart> oldSpts = e2.getSongParts();
            List<SongPart> newSpts = e2.getNewSpts();

            // Important : remove rhythm parts before adding (otherwise we could have a "not enough midi channels
            // available" in the loop).
            for (SongPart oldSpt : oldSpts)
            {
                Rhythm rOld = oldSpt.getRhythm();
                if (!songRhythms.contains(rOld))
                {
                    // Rhythm is no more present in the song, remove it also from the MidiMix
                    removeRhythm(rOld);
                    mixRhythms.remove(rOld);
                }
            }
            // Add the new rhythms
            for (SongPart spt : newSpts)
            {
                Rhythm rNew = spt.getRhythm();
                if (!mixRhythms.contains(rNew))
                {
                    // New song rhythm is not yet in the midimix, add it
                    try
                    {
                        addRhythm(rNew);
                    } catch (MidiUnavailableException ex)
                    {
                        // Should not be here since we made a test earlier to avoid this
                        throw new IllegalStateException("Unexpected MidiUnavailableException !", ex);
                    }
                    mixRhythms.add(rNew);
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    // Implementation of the PropertiesListener interface
    //-----------------------------------------------------------------------
    @SuppressWarnings(
            {
                "unchecked", "rawtypes"
            })
    @Override
    public void propertyChange(PropertyChangeEvent e)
    {
        LOGGER.fine("propertyChange() e=" + e);
        if (e.getSource() instanceof InstrumentMix)
        {
            InstrumentMix insMix = (InstrumentMix) e.getSource();
            int channel = getChannel(insMix);
            if (e.getPropertyName().equals(InstrumentMix.PROP_SOLO))
            {
                boolean b = (boolean) e.getNewValue();
                LOGGER.fine("propertyChange() channel=" + channel + " solo=" + b);
                if (b)
                {
                    // Solo switched to ON
                    if (soloedInsMixes.isEmpty())
                    {
                        // Fist solo ! 
                        soloedInsMixes.add(insMix);
                        // Save config
                        saveMuteConfig();
                        // Switch other channels to ON (ezxcept soloed one)
                        for (InstrumentMix im : instrumentMixes)
                        {
                            if (im != null && im != insMix)
                            {
                                im.setMute(true);
                            }
                        }
                    } else
                    {
                        // It's another solo
                        soloedInsMixes.add(insMix);
                    }
                } else // Solo switched to OFF
                {
                    soloedInsMixes.remove(insMix);
                    if (soloedInsMixes.isEmpty())
                    {
                        // This was the last SOLO OFF, need to restore Mute config
                        restoreMuteConfig();
                    } else
                    {
                        // There are still other Solo ON channels, put it in mute again
                        insMix.setMute(true);
                    }
                }
            } else if (e.getPropertyName().equals(InstrumentMix.PROP_MUTE))
            {
                boolean b = (boolean) e.getNewValue();
                // If in solo mode, pressing unmute of a muted channel turns it in solo mode
                if (b == false && !soloedInsMixes.isEmpty())
                {
                    insMix.setSolo(true);
                }
                // Forward the MUTE change event
                pcs.firePropertyChange(MidiMix.PROP_INSTRUMENT_MUTE, insMix, b);
            } else if (e.getPropertyName().equals(InstrumentMix.PROP_INSTRUMENT))
            {
                // If drums instrument change with different KeyMap
                Instrument oldIns = (Instrument) e.getOldValue();
                Instrument newIns = (Instrument) e.getNewValue();
                RhythmVoice rv = getKey(channel);
                if (rv.isDrums())
                {
                    DrumKit oldKit = oldIns.getDrumKit();
                    DrumKit newKit = newIns.getDrumKit();
                    if ((oldKit != null && newKit != null && oldKit.getKeyMap() != newKit.getKeyMap())
                            || (oldKit == null && newKit != null)
                            || (oldKit != null && newKit == null))
                    {
                        pcs.firePropertyChange(MidiMix.PROP_DRUMS_INSTRUMENT_KEYMAP, channel, oldKit != null ? oldKit.getKeyMap() : null);
                    }
                }
            }
            fireIsModified();
        } else if (e.getSource() instanceof InstrumentSettings)
        {
            // Forward some change events
            InstrumentSettings insSet = (InstrumentSettings) e.getSource();
            InstrumentMix insMix = insSet.getContainer();
            if (e.getPropertyName().equals(InstrumentSettings.PROPERTY_TRANSPOSITION))
            {
                int value = (Integer) e.getNewValue();
                pcs.firePropertyChange(MidiMix.PROP_INSTRUMENT_TRANSPOSITION, insMix, value);
            } else if (e.getPropertyName().equals(InstrumentSettings.PROPERTY_VELOCITY_SHIFT))
            {
                int value = (Integer) e.getNewValue();
                pcs.firePropertyChange(MidiMix.PROP_INSTRUMENT_VELOCITY_SHIFT, insMix, value);
            }
            fireIsModified();
        }
    }
//-----------------------------------------------------------------------
// Private methods
//-----------------------------------------------------------------------

    /**
     * Perform the InstrumentMix change operation.
     * <p>
     * There is no validity check done on parameters, this must be done by the caller method.
     * <p>
     * This method fires an UndoableEvent(s).
     *
     * @param channel
     * @param insMix
     * @param rvKey
     */
    private void changeInstrumentMix(final int channel, final InstrumentMix insMix, final RhythmVoice rvKey)
    {
        final InstrumentMix oldInsMix = instrumentMixes[channel];
        final RhythmVoice oldRvKey = rvKeys[channel];
        if (oldInsMix == null && insMix == null)
        {
            return;
        } else if (oldInsMix != null && oldInsMix.equals(insMix))
        {
            return;
        } else if (oldInsMix != null)
        {
            oldInsMix.setSolo(false);  // So it does not mess if we were in solo mode
            oldInsMix.removePropertyChangeListener(this);
            oldInsMix.getSettings().removePropertyChangeListener(this);
            setDrumsReroutedChannel(false, channel);
        }
        if (insMix != null)
        {
            // insMix.setMute(false);       // Don't change
            insMix.setSolo(false);
            insMix.addPropertyChangeListener(this);
            insMix.getSettings().addPropertyChangeListener(this);
        }

        // Update our internal state
        instrumentMixes[channel] = insMix;
        rvKeys[channel] = rvKey;

        // Prepare the undoable edit
        UndoableEdit edit = new SimpleEdit("Change instrumemt mix")
        {
            @Override
            public void undoBody()
            {
                instrumentMixes[channel] = oldInsMix;
                rvKeys[channel] = oldRvKey;
                if (insMix != null)
                {
                    insMix.removePropertyChangeListener(MidiMix.this);
                    insMix.getSettings().removePropertyChangeListener(MidiMix.this);
                }
                if (oldInsMix != null)
                {
                    oldInsMix.addPropertyChangeListener(MidiMix.this);
                    oldInsMix.getSettings().addPropertyChangeListener(MidiMix.this);
                }
                pcs.firePropertyChange(PROP_CHANNEL_INSTRUMENT_MIX, insMix, channel);
                fireIsModified();
            }

            @Override
            public void redoBody()
            {
                instrumentMixes[channel] = insMix;
                rvKeys[channel] = rvKey;
                if (oldInsMix != null)
                {
                    oldInsMix.removePropertyChangeListener(MidiMix.this);
                    oldInsMix.getSettings().removePropertyChangeListener(MidiMix.this);
                }
                if (insMix != null)
                {
                    insMix.addPropertyChangeListener(MidiMix.this);
                    insMix.getSettings().addPropertyChangeListener(MidiMix.this);
                }
                pcs.firePropertyChange(PROP_CHANNEL_INSTRUMENT_MIX, oldInsMix, channel);
                fireIsModified();
            }
        };
        fireUndoableEditHappened(edit);

        pcs.firePropertyChange(PROP_CHANNEL_INSTRUMENT_MIX, oldInsMix, channel);
        fireIsModified();

    }

    private void fireIsModified()
    {
        needSave = true;
        pcs.firePropertyChange(PROP_MODIFIED_OR_SAVED, false, true);
    }

    private void saveMuteConfig()
    {
        int i = 0;
        for (InstrumentMix im : instrumentMixes)
        {
            saveMuteConfiguration[i++] = (im == null) ? false : im.isMute();
        }
    }

    private void restoreMuteConfig()
    {
        int i = 0;
        for (InstrumentMix im : instrumentMixes)
        {
            if (im != null)
            {
                im.setMute(saveMuteConfiguration[i]);
            }
            i++;
        }
    }

    /**
     * Add a rhythm to this MidiMix.
     * <p>
     * Manage the case where r is not the unique rhythm of the MidiMix: need to maintain instruments consistency to avoid
     * poor-sounding rhythms transitions.
     *
     * @param r
     * @throws MidiUnavailableException
     */
    private void addRhythm(Rhythm r) throws MidiUnavailableException
    {
        LOGGER.log(Level.FINE, "addRhythm() -- r={0} rvKeys={1}", new Object[]
        {
            r, Arrays.asList(rvKeys)
        });
        MidiMix mm = MidiMixManager.getInstance().findMix(r);
        if (!getUniqueRhythms().isEmpty())
        {
            // Adapt mm to sound like the InstrumentMixes of r0           
            Rhythm r0 = getUniqueRhythms().get(0);
            adaptInstrumentMixes(mm, r0);
        }
        addInstrumentMixes(mm, r);

    }

    /**
     * Adapt the InstrumentMixes of mm to "sound" like the InstrumentMixes of r0 in this MidiMix.
     *
     * @param mm
     * @param r0
     */
    private void adaptInstrumentMixes(MidiMix mm, Rhythm r0)
    {
        HashMap<String, InstrumentMix> mapKeyMix = new HashMap<>();
        HashMap<Family, InstrumentMix> mapFamilyMix = new HashMap<>();
        InstrumentMix r0InsMixDrums = null;
        InstrumentMix r0InsMixPerc = null;
        // First try to match InstrumentMixes using a "key" = "3 first char of Rv.getName() + GM1 family"
        for (int channel : getUsedChannels(r0))
        {
            // Build the keys from r0
            RhythmVoice rv = rvKeys[channel];
            InstrumentMix insMix = instrumentMixes[channel];
            if (rv.getType().equals(RhythmVoice.Type.DRUMS))
            {
                r0InsMixDrums = insMix;
                continue;
            } else if (rv.getType().equals(RhythmVoice.Type.PERCUSSION))
            {
                r0InsMixPerc = insMix;
                continue;
            }
            GM1Instrument insGM1 = insMix.getInstrument().getSubstitute();  // Might be null            
            Family family = insGM1 != null ? insGM1.getFamily() : null;
            String mapKey = Utilities.truncate(rv.getName().toLowerCase(), 3) + "-" + ((family != null) ? family.name() : "");
            if (mapKeyMix.get(mapKey) == null)
            {
                mapKeyMix.put(mapKey, insMix);  // If several instruments have the same Type, save only the first one
            }
            if (family != null && mapFamilyMix.get(family) == null)
            {
                mapFamilyMix.put(family, insMix);       // If several instruments have the same family, save only the first one
            }
        }

        // Try to convert using the keys
        HashSet<Integer> doneChannels = new HashSet<>();
        for (int mmChannel : mm.getUsedChannels())
        {
            RhythmVoice mmRv = mm.rvKeys[mmChannel];
            InstrumentMix mmInsMix = mm.instrumentMixes[mmChannel];
            InstrumentMix insMix = null;
            if (mmRv.getType().equals(RhythmVoice.Type.DRUMS))
            {
                insMix = r0InsMixDrums;
            } else if (mmRv.getType().equals(RhythmVoice.Type.PERCUSSION))
            {
                insMix = r0InsMixPerc;
            } else
            {
                GM1Instrument mmInsGM1 = mmInsMix.getInstrument().getSubstitute();  // Can be null            
                Family mmFamily = mmInsGM1 != null ? mmInsGM1.getFamily() : null;
                String mapKey = Utilities.truncate(mmRv.getName().toLowerCase(), 3) + "-" + ((mmFamily != null) ? mmFamily.name() : "");
                insMix = mapKeyMix.get(mapKey);
            }
            if (insMix != null)
            {
                // Copy InstrumentMix data
                mmInsMix.setInstrument(insMix.getInstrument());
                mmInsMix.getSettings().set(insMix.getSettings());
                doneChannels.add(mmChannel);
            }
        }

        // Try to convert also the other channels by matching only the instrument family
        for (int mmChannel : mm.getUsedChannels())
        {
            if (doneChannels.contains(mmChannel))
            {
                continue;
            }
            InstrumentMix mmInsMix = mm.instrumentMixes[mmChannel];
            GM1Instrument mmInsGM1 = mmInsMix.getInstrument().getSubstitute();  // Can be null          
            if (mmInsGM1 == null)
            {
                continue;
            }
            Family mmFamily = mmInsMix.getInstrument().getSubstitute().getFamily();
            InstrumentMix insMix = mapFamilyMix.get(mmFamily);
            if (insMix != null)
            {
                // Copy InstrumentMix data
                mmInsMix.setInstrument(insMix.getInstrument());
                mmInsMix.getSettings().set(insMix.getSettings());
            }
        }
    }

    private void removeRhythm(Rhythm r)
    {
        LOGGER.log(Level.FINE, "removeRhythm() -- r={0} rvKeys={1}", new Object[]
        {
            r, Arrays.asList(rvKeys)
        });

        for (RhythmVoice rvKey : rvKeys)
        {
            if (rvKey != null && rvKey.getContainer() == r)
            {
                int channel = getChannel(rvKey);
                setInstrumentMix(channel, null, null);
            }
        }
    }

    /**
     * The unique rhythms list for which this MidiMix has InstrumentMixes.
     *
     * @return
     */
    private List<Rhythm> getUniqueRhythms()
    {
        ArrayList<Rhythm> result = new ArrayList<>();
        for (RhythmVoice rv : rvKeys)
        {
            if (rv != null && !result.contains(rv.getContainer()))
            {
                result.add(rv.getContainer());
            }
        }
        return result;
    }

    private void fireUndoableEditHappened(UndoableEdit edit)
    {
        if (edit == null)
        {
            throw new IllegalArgumentException("edit=" + edit);
        }
        UndoableEditEvent event = new UndoableEditEvent(this, edit);
        for (UndoableEditListener l : undoListeners.toArray(new UndoableEditListener[undoListeners.size()]))
        {
            l.undoableEditHappened(event);
        }
    }

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

    /**
     * RhythmVoices depend on a (potentially system dependent) rhythm, therefore it must be stored in a special way: just save
     * rhythm serial id + RhythmVoice name, and it will be reconstructed at deserialization.
     * <p>
     * MidiMix is saved with Drums rerouting disabled and all solo status OFF, but all Mute status are saved.
     */
    private static class SerializationProxy implements Serializable
    {

        private static final long serialVersionUID = -344448971122L;
        private static final String SP_USER_CHANNEL_RHYTHM_ID = "SpUserChannelRhythmID";
        private final int spVERSION = 1;
        private final InstrumentMix[] spInsMixes;
        private RvStorage[] spKeys;

        private SerializationProxy(MidiMix mm)
        {
            // Use a copy because we want to disable drums rerouting
            MidiMix mmCopy = new MidiMix();         // Drums rerouting disabled by default
            try
            {
                mmCopy.addInstrumentMixes(mm, null);        // This also sets solo/mute off
            } catch (MidiUnavailableException ex)
            {
                throw new IllegalStateException("Should never be here because mmCopy was empty before copy operation!");
            }

            // If mm had some rerouted channels, apply the saved settings in the copy
            for (Integer channel : mm.getDrumsReroutedChannels())
            {
                InstrumentMix saveInsMix = mm.drumsReroutedChannels.get(channel);
                mmCopy.setInstrumentMix(channel, mm.getKey(channel), saveInsMix);           // This lso sets solo/mute off
            }

            spInsMixes = mmCopy.instrumentMixes;
            spKeys = new RvStorage[mmCopy.rvKeys.length];
            for (int i = 0; i < mmCopy.rvKeys.length; i++)
            {
                RhythmVoice rv = mmCopy.rvKeys[i];
                if (rv != null)
                {
                    // Restore the mute status so it can be saved
                    InstrumentMix originalInsMix = mm.getInstrumentMixFromChannel(i);
                    if (!(rv instanceof UserChannelRvKey))
                    {
                        spInsMixes[i].setMute(originalInsMix.isMute());
                    }

                    // Store the RhythmVoice using a RvStorage object
                    RvStorage rvs;
                    String rhythmId;
                    if (rv instanceof UserChannelRvKey)
                    {
                        rhythmId = SP_USER_CHANNEL_RHYTHM_ID;
                    } else
                    {
                        rhythmId = rv.getContainer().getUniqueId();
                    }
                    rvs = new RvStorage(rhythmId, rv.getName());
                    spKeys[i] = rvs;
                }
            }
        }

        private Object readResolve() throws ObjectStreamException
        {
            assert spKeys.length == this.spInsMixes.length : "spKeys=" + Arrays.asList(spKeys) + " spInsMixes=" + Arrays.asList(spInsMixes);
            MidiMix mm = new MidiMix();
            StringBuilder msg = new StringBuilder();

            for (int channel = 0; channel < spKeys.length; channel++)
            {
                RvStorage rvs = spKeys[channel];
                RhythmVoice rv = null;
                InstrumentMix insMix = spInsMixes[channel];

                if (insMix == null && rvs == null)
                {
                    // Normal case: no instrument
                    continue;
                }
                if (insMix == null || rvs == null)
                {
                    msg.append("Mix file error, unexpected null value for channel " + channel + ":  rvs=" + rvs + " insMix=" + insMix);
                    throw new XStreamException(msg.toString());
                }
                if (rvs.rhythmId.equals(SP_USER_CHANNEL_RHYTHM_ID))
                {
                    try
                    {
                        // Special case : it's a user channel, just re-enable it
                        mm.addUserChannel(insMix);
                    } catch (MidiUnavailableException ex)
                    {
                        msg.append("Mix file error, can't enable user channel=" + channel + ", exception=" + ex.getLocalizedMessage());
                        throw new XStreamException(msg.toString());
                    }
                } else
                {
                    // Standard case, retrieve the RhythmVoice
                    rv = rebuildRhythmVoice(rvs.rhythmId, rvs.rvName);
                    if (rv == null)
                    {
                        msg.append("Mix file error, can't rebuild RhythmVoice for channel=" + channel + ", rhythmId=" + rvs.rhythmId + " and RhythmVoiceName=" + rvs.rvName);
                        throw new XStreamException(msg.toString());
                    }
                    // Update the created MidiMix with the deserialized data
                    // Need a copy of insMix because setInstrumentMix() will make modifications on the object (e.g. setMute(false))
                    // and we can not modify spInsMixes during the deserialization process.
                    InstrumentMix insMixNew = new InstrumentMix(insMix);
                    mm.setInstrumentMix(channel, rv, insMixNew);
                }
            }
            return mm;
        }

        private RhythmVoice rebuildRhythmVoice(String rhythmId, String rvName)
        {
            RhythmDatabase rdb = RhythmDatabase.Utilities.getDefault();
            Rhythm r = rdb.getRhythm(rhythmId);
            if (r != null)
            {
                for (RhythmVoice rv : r.getRhythmVoices())
                {
                    if (rv.getName().equals(rvName))
                    {
                        return rv;
                    }
                }
            }
            return null;
        }

        private class RvStorage
        {

            private String rhythmId;
            private String rvName;

            public RvStorage(String rid, String rvName)
            {
                rhythmId = rid;
                this.rvName = rvName;
            }
        }
    }

}

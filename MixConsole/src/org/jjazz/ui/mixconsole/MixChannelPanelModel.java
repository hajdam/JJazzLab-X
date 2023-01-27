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
package org.jjazz.ui.mixconsole;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Icon;
import org.jjazz.midi.api.Instrument;
import org.jjazz.rhythm.api.RhythmVoice;

/**
 * Model of a MixChannelPanel + controller.
 * <p>
 * - provide all variables that need to be listened to<br>
 * - provide all methods to handle actions not directly managed by the MixChannelPanel itself.<br>
 */
interface MixChannelPanelModel
{

    static final String PROP_PANORAMIC = "PropPanoramic";   //NOI18N 
    static final String PROP_REVERB = "PropReverb";   //NOI18N 
    static final String PROP_CHORUS = "PropChorus";   //NOI18N 
    static final String PROP_PANORAMIC_ENABLED = "PropPanoramicEnabled";   //NOI18N 
    static final String PROP_REVERB_ENABLED = "PropReverbEnabled";   //NOI18N 
    static final String PROP_CHORUS_ENABLED = "PropChorusEnabled";   //NOI18N 
    static final String PROP_VOLUME_ENABLED = "PropVolumeEnabled";   //NOI18N 
    static final String PROP_INSTRUMENT_ENABLED = "PropInstrumentEnabled";   //NOI18N 
    static final String PROP_DRUMS_CHANNEL_REROUTED = "PropDrumsChannelRerouted";   //NOI18N 
    static final String PROP_MUTE = "PropMute";   //NOI18N 
    static final String PROP_SOLO = "PropSolo";   //NOI18N 
    static final String PROP_VOLUME = "PropVolume";   //NOI18N 
    static final String PROP_INSTRUMENT = "PropInstrument";   //NOI18N 
    static final String PROP_CHANNEL_ID = "PropChannelId";   //NOI18N 
    static final String PROP_RHYTHM_VOICE = "PropRhythmVoice";   //NOI18N 
    static final String PROP_CHANNEL_NAME = "PropChannelName";   //NOI18N 
    static final String PROP_ICON = "PropIcon";   //NOI18N 
    static final String PROP_CHANNEL_NAME_TOOLTIP = "PropChannelNameTooltip";   //NOI18N 
    static final String PROP_ICON_TOOLTIP = "PropIconTooltip";   //NOI18N 

    void addPropertyChangeListener(PropertyChangeListener l);

    void removePropertyChangeListener(PropertyChangeListener l);

    void setPanoramicEnabled(boolean b);

    boolean isPanoramicEnabled();

    void setChorusEnabled(boolean b);

    boolean isChorusEnabled();

    void setVolumeEnabled(boolean b);

    boolean isVolumeEnabled();

    void setInstrumentEnabled(boolean b);

    boolean isInstrumentEnabled();

    void setReverbEnabled(boolean b);

    boolean isReverbEnabled();

    void setReverb(int value);

    int getReverb();

    void setChorus(int value);

    int getChorus();

    void setPanoramic(int value);

    int getPanoramic();

    boolean isDrumsReroutingEnabled();

    RhythmVoice getRhythmVoice();

    String getChannelName();

    Color getChannelColor();

    String getChannelNameTooltip();

    String getIconTooltip();

    Icon getIcon();

    /**
     * @param oldValue
     * @param newValue
     * @param e The MouseEvent representing the user action that changed the volume. e will be null if change did not result from
     * a mouse drag or wheel event.
     */
    void setVolume(int oldValue, int newValue, MouseEvent e);

    int getVolume();

    void setMute(boolean b);

    boolean isMute();

    void setSolo(boolean b);

    boolean isSolo();

    int getChannelId();

    Instrument getInstrument();

    void cleanup();

}

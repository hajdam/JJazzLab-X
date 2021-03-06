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
package org.jjazz.ui.cl_editor.barbox;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.SwingPropertyChangeSupport;
import static org.jjazz.ui.cl_editor.barbox.Bundle.CTL_Bar;
import static org.jjazz.ui.cl_editor.barbox.Bundle.CTL_PlayedBar;
import static org.jjazz.ui.cl_editor.barbox.Bundle.CTL_SelectedBar;
import org.jjazz.ui.cl_editor.barbox.api.BarBoxSettings;
import org.jjazz.ui.colorsetmanager.api.ColorSetManager;
import org.jjazz.util.Utilities;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;
import org.jjazz.ui.utilities.FontColorUserSettingsProvider;

@ServiceProviders(value =
{
    @ServiceProvider(service = BarBoxSettings.class),
    @ServiceProvider(service = FontColorUserSettingsProvider.class)
}
)
@NbBundle.Messages(
        {
            "CTL_Bar=Bar",
            "CTL_SelectedBar=Selected bar",
            "CTL_PlayedBar=Played bar",
        }
)
public class BarBoxSettingsImpl extends BarBoxSettings implements FontColorUserSettingsProvider
{

    /**
     * The Preferences of this object.
     */
    private static Preferences prefs = NbPreferences.forModule(BarBoxSettingsImpl.class);
    /**
     * The listeners for changes of this object.
     */
    private SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);
    ArrayList<FontColorUserSettingsProvider.FCSetting> fcSettings = new ArrayList<>();

    public BarBoxSettingsImpl()
    {
        // The settings we want to expose for user modifications
        FontColorUserSettingsProvider.FCSetting fcs = new FontColorUserSettingsProvider.FCSettingAdapter("BarId", CTL_Bar())
        {
            @Override
            public void setColor(Color c)
            {
                setDefaultColor(c);
            }

            @Override
            public Color getColor()
            {
                return getDefaultColor();
            }
        };
        fcSettings.add(fcs);
        fcs = new FontColorUserSettingsProvider.FCSettingAdapter("SelectedBarId", CTL_SelectedBar())
        {
            @Override
            public void setColor(Color c)
            {
                setSelectedColor(c);
            }

            @Override
            public Color getColor()
            {
                return getSelectedColor();
            }
        };
        fcSettings.add(fcs);
        fcs = new FontColorUserSettingsProvider.FCSettingAdapter("PlaybackBarId", CTL_PlayedBar())
        {
            @Override
            public void setColor(Color c)
            {
                setPlaybackColor(c);
            }

            @Override
            public Color getColor()
            {
                return getPlaybackColor();
            }
        };
        fcSettings.add(fcs);
    }

    @Override
    public void setBorderFont(Font font)
    {
        Font old = getBorderFont();
        String strFont = Utilities.fontAsString(font);
        prefs.put(PROP_BORDER_FONT, strFont);
        pcs.firePropertyChange(PROP_BORDER_FONT, old, font);
    }

    @Override
    public Font getBorderFont()
    {
        String strFont = prefs.get(PROP_BORDER_FONT, "Arial-BOLD-8");
        return Font.decode(strFont);
    }

    @Override
    public void setBorderColor(Color color)
    {
        Color old = getBorderColor();
        prefs.putInt(PROP_BORDER_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_BORDER_COLOR, old, color);
    }

    @Override
    public Color getBorderColor()
    {
        return new Color(prefs.getInt(PROP_BORDER_COLOR, Color.LIGHT_GRAY.darker().getRGB()));
    }

    @Override
    public void setFocusedBorderColor(Color color)
    {
        Color old = getFocusedBorderColor();
        prefs.putInt(PROP_FOCUSED_BORDER_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_FOCUSED_BORDER_COLOR, old, color);
    }

    @Override
    public Color getFocusedBorderColor()
    {
        return new Color(prefs.getInt(PROP_FOCUSED_BORDER_COLOR, ColorSetManager.Utilities.getDefault().getFocusedBorderColor().getRGB()));
    }

    @Override
    public TitledBorder getTitledBorder(String str)
    {
        Border lb = BorderFactory.createLineBorder(getBorderColor(), 1);
        TitledBorder bb = new TitledBorder(lb, str, TitledBorder.RIGHT, TitledBorder.TOP, getBorderFont(), getBorderColor());
        return bb;
    }

    @Override
    public TitledBorder getFocusedTitledBorder(String str)
    {
        Border lb = BorderFactory.createLineBorder(getFocusedBorderColor(), 1);
        TitledBorder bb = new TitledBorder(lb, str, TitledBorder.RIGHT, TitledBorder.TOP, getBorderFont(), getFocusedBorderColor());
        return bb;
    }

    @Override
    public void setDefaultColor(Color color)
    {
        Color old = getSelectedColor();
        prefs.putInt(PROP_BAR_DEFAULT_COLOR, color != null ? color.getRGB() : ColorSetManager.Utilities.getDefault().getWhite().getRGB());
        pcs.firePropertyChange(PROP_BAR_DEFAULT_COLOR, old, color);
    }

    @Override
    public Color getDefaultColor()
    {
        return new Color(prefs.getInt(PROP_BAR_DEFAULT_COLOR, ColorSetManager.Utilities.getDefault().getWhite().getRGB()));
    }

    @Override
    public void setPastEndColor(Color color)
    {
        Color old = getSelectedColor();
        prefs.putInt(PROP_BAR_PAST_END_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_BAR_PAST_END_COLOR, old, color);
    }

    @Override
    public Color getPastEndColor()
    {
        return new Color(prefs.getInt(PROP_BAR_PAST_END_COLOR, Color.LIGHT_GRAY.getRGB()));
    }

    @Override
    public void setDisabledColor(Color color)
    {
        Color old = getSelectedColor();
        prefs.putInt(PROP_BAR_DISABLED_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_BAR_DISABLED_COLOR, old, color);
    }

    @Override
    public Color getDisabledColor()
    {
        return new Color(prefs.getInt(PROP_BAR_DISABLED_COLOR, Color.LIGHT_GRAY.brighter().getRGB()));
    }

    @Override
    public void setDisabledPastEndColor(Color color)
    {
        Color old = getSelectedColor();
        prefs.putInt(PROP_BAR_DISABLED_PAST_END_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_BAR_DISABLED_PAST_END_COLOR, old, color);
    }

    @Override
    public Color getDisabledPastEndColor()
    {
        return new Color(prefs.getInt(PROP_BAR_DISABLED_PAST_END_COLOR, Color.LIGHT_GRAY.brighter().getRGB()));
    }

    @Override
    public Color getSelectedColor()
    {
        return new Color(prefs.getInt(PROP_BAR_SELECTED_COLOR, ColorSetManager.Utilities.getDefault().getSelectedBackgroundColor().getRGB()));
    }

    @Override
    public void setSelectedColor(Color color)
    {
        Color old = getSelectedColor();
        prefs.putInt(PROP_BAR_SELECTED_COLOR, color != null ? color.getRGB() : ColorSetManager.Utilities.getDefault().getSelectedBackgroundColor().getRGB());
        pcs.firePropertyChange(PROP_BAR_SELECTED_COLOR, old, color);
    }

    @Override
    public Color getPastEndSelectedColor()
    {
        return new Color(prefs.getInt(PROP_BAR_PAST_END_SELECTED_COLOR, Color.YELLOW.darker().getRGB()));
    }

    @Override
    public void setPastEndSelectedColor(Color color)
    {
        Color old = getPastEndSelectedColor();
        prefs.putInt(PROP_BAR_PAST_END_SELECTED_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_BAR_PAST_END_SELECTED_COLOR, old, color);
    }

    @Override
    public Color getPlaybackColor()
    {
        return new Color(prefs.getInt(PROP_BAR_PLAYBACK_COLOR, new Color(244, 219, 215).getRGB()));
    }

    @Override
    public void setPlaybackColor(Color color)
    {
        Color old = getPlaybackColor();
        prefs.putInt(PROP_BAR_PLAYBACK_COLOR, color != null ? color.getRGB() : new Color(244, 219, 215).getRGB());
        pcs.firePropertyChange(PROP_BAR_PLAYBACK_COLOR, old, color);
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener)
    {
        pcs.removePropertyChangeListener(listener);
    }
    // =====================================================================================
    // FontColorUserSettingsProvider implementation
    // =====================================================================================

    @Override
    public List<FontColorUserSettingsProvider.FCSetting> getFCSettings()
    {
        return fcSettings;
    }

}

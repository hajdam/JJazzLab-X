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
package org.jjazz.ui.rpviewer;

import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.prefs.Preferences;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.event.SwingPropertyChangeSupport;
import org.jjazz.ui.colorsetmanager.api.ColorSetManager;
import org.jjazz.ui.rpviewer.api.RpViewerSettings;
import org.jjazz.util.Utilities;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = RpViewerSettings.class)
public class RpViewerSettingsImpl extends RpViewerSettings
{

    /**
     * The Preferences of this object.
     */
    private static Preferences prefs = NbPreferences.forModule(RpViewerSettingsImpl.class);
    /**
     * The listeners for changes of this object.
     */
    private SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);

    @Override
    public Color getSelectedBackgroundColor()
    {
        return new Color(prefs.getInt(PROP_SELECTED_BACKGROUND_COLOR, ColorSetManager.Utilities.getDefault().getSelectedBackgroundColor().getRGB()));
    }

    @Override
    public void setSelectedBackgroundColor(Color color)
    {
        Color old = getSelectedBackgroundColor();
        prefs.putInt(PROP_SELECTED_BACKGROUND_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_SELECTED_BACKGROUND_COLOR, old, color);
    }

    @Override
    public Color getDefaultBackgroundColor()
    {
        return new Color(prefs.getInt(PROP_BACKGROUND_COLOR, ColorSetManager.Utilities.getDefault().getWhite().getRGB()));
    }

    @Override
    public void setDefaultBackgroundColor(Color color)
    {
        Color old = getSelectedBackgroundColor();
        prefs.putInt(PROP_BACKGROUND_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_BACKGROUND_COLOR, old, color);
    }

    @Override
    public Color getFocusedBorderColor()
    {
        return new Color(prefs.getInt(PROP_FOCUS_BORDER_COLOR, ColorSetManager.Utilities.getDefault().getFocusedBorderColor().getRGB()));
    }

    @Override
    public void setFocusedBorderColor(Color color)
    {
        Color old = getFocusedBorderColor();
        prefs.putInt(PROP_FOCUS_BORDER_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_FOCUS_BORDER_COLOR, old, color);
    }

    @Override
    public Border getFocusedBorder()
    {
        return BorderFactory.createLineBorder(getFocusedBorderColor(), 1);
    }

    @Override
    public Color getDefaultBorderColor()
    {
        return new Color(prefs.getInt(PROP_DEFAULT_BORDER_COLOR, Color.GRAY.getRGB()));
    }

    @Override
    public void setDefaultBorderColor(Color color)
    {
        Color old = getDefaultBorderColor();
        prefs.putInt(PROP_DEFAULT_BORDER_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_DEFAULT_BORDER_COLOR, old, color);
    }

    @Override
    public Border getNonFocusedBorder()
    {
        return BorderFactory.createLineBorder(getDefaultBorderColor(), 1);
    }

    @Override
    public void setFont(Font font)
    {
        Font old = getFont();
        String strFont = Utilities.fontAsString(font);
        prefs.put(PROP_FONT, strFont);
        pcs.firePropertyChange(PROP_FONT, old, font);
    }

    @Override
    public Font getFont()
    {
        String strFont = prefs.get(PROP_FONT, "Helvetica-PLAIN-9");
        return Font.decode(strFont);
    }

    @Override
    public Color getFontColor()
    {
        return new Color(prefs.getInt(PROP_FONT_COLOR, Color.DARK_GRAY.getRGB()));
    }

    @Override
    public void setFontColor(Color color)
    {
        Color old = getFontColor();
        prefs.putInt(PROP_FONT_COLOR, color.getRGB());
        pcs.firePropertyChange(PROP_FONT_COLOR, old, color);
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

}

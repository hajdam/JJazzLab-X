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
import org.openide.util.Lookup;

public abstract class StringRpViewerSettings
{

    public static String PROP_FONT = "StringRpViewerFont";
    public static String PROP_FONT_COLOR = "StringRpViewerFontColor";

    public static StringRpViewerSettings getDefault()
    {
        StringRpViewerSettings result = Lookup.getDefault().lookup(StringRpViewerSettings.class);
        if (result == null)
        {
            throw new NullPointerException("result=" + result);
        }
        return result;
    }

    abstract public void setFont(Font font);

    abstract public Font getFont();

    abstract public void setFontColor(Color color);

    abstract public Color getFontColor();

    abstract public void addPropertyChangeListener(PropertyChangeListener listener);

    abstract public void removePropertyChangeListener(PropertyChangeListener listener);
}

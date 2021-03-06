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
package org.jjazz.ui.cl_editor.api;

import org.jjazz.song.api.Song;
import org.jjazz.ui.cl_editor.CL_EditorFactoryImpl;
import org.openide.util.Lookup;

public abstract class CL_EditorFactory
{

    public static CL_EditorFactory getDefault()
    {
        CL_EditorFactory clef = Lookup.getDefault().lookup(CL_EditorFactory.class);
        if (clef == null)
        {
            clef = CL_EditorFactoryImpl.getInstance();
        }
        return clef;
    }

    abstract public CL_Editor createEditor(Song song);
}

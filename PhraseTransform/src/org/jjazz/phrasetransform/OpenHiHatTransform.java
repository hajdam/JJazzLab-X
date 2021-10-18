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
package org.jjazz.phrasetransform;

import java.util.Properties;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.jjazz.midi.api.Instrument;
import org.jjazz.phrase.api.NoteEvent;
import org.jjazz.phrase.api.SizedPhrase;
import org.jjazz.phrasetransform.api.PhraseTransformCategory;
import org.jjazz.phrasetransform.api.PhraseTransform;
import org.jjazz.phrasetransform.api.PtProperties;
import org.jjazz.songcontext.api.SongPartContext;
import org.netbeans.api.annotations.common.StaticResource;

/**
 *
 * @author Jerome
 */
public class OpenHiHatTransform implements PhraseTransform
{

    @StaticResource(relative = true)
    private static final String ICON_PATH = "resources/OpenHiHatTransformIcon.gif";
    private static final Icon ICON = new ImageIcon(OpenHiHatTransform.class.getResource(ICON_PATH));

    private PtProperties properties = new PtProperties(new Properties());

    @Override
    public SizedPhrase transform(SizedPhrase inPhrase, SongPartContext context)
    {
        SizedPhrase res = new SizedPhrase(inPhrase.getChannel(), inPhrase.getBeatRange(), inPhrase.getTimeSignature());

        for (var ne : inPhrase)
        {
            var newNe = new NoteEvent(ne, ne.getPitch() + 4, ne.getDurationInBeats() * 0.9f, (int) (ne.getVelocity() * 0.8f));
            res.addOrdered(newNe);
        }

        return res;
    }

    @Override
    public int getFitScore(SizedPhrase inPhrase, SongPartContext context)
    {
        Instrument ins = context.getMidiMix().getInstrumentMixFromChannel(inPhrase.getChannel()).getInstrument();
        return ins.getDrumKit() != null ? 90 : 0;
    }

    @Override
    public String getName()
    {
        return "hi-hat 16th";
    }

    @Override
    public int hashCode()
    {
        return PhraseTransform.hashCode(this);
    }

    @Override
    public boolean equals(Object obj)
    {
        return PhraseTransform.equals(this, obj);
    }

    @Override
    public PtProperties getProperties()
    {
        return properties;
    }

    @Override
    public OpenHiHatTransform getCopy()
    {
        OpenHiHatTransform res = new OpenHiHatTransform();
        res.properties = properties.getCopy();
        return res;
    }

    @Override
    public String getUniqueId()
    {
        return "OpenHiHatTransformID";
    }

    @Override
    public PhraseTransformCategory getCategory()
    {
        return PhraseTransformCategory.PERCUSSION;
    }

    @Override
    public String getDescription()
    {
        return "Make hi-hat 16th notes";
    }

    @Override
    public Icon getIcon()
    {
        return ICON;
    }
}

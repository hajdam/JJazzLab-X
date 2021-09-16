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
package org.jjazz.arranger;

import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Transmitter;
import org.jjazz.activesong.api.ActiveSongManager;
import org.jjazz.harmony.api.ChordSymbol;
import org.jjazz.harmony.api.ChordSymbolFinder;
import org.jjazz.harmony.api.Note;
import org.jjazz.leadsheet.chordleadsheet.api.UnsupportedEditException;
import org.jjazz.midi.api.JJazzMidiSystem;
import org.jjazz.midi.api.MidiUtilities;
import org.jjazz.midimix.api.MidiMix;
import org.jjazz.rhythm.api.MusicGenerationException;
import org.jjazz.rhythm.api.Rhythm;
import org.jjazz.rhythm.api.rhythmparameters.RP_STD_Variation;
import org.jjazz.song.api.Song;
import org.jjazz.songcontext.api.SongContext;
import org.jjazz.songstructure.api.SgsChangeListener;
import org.jjazz.songstructure.api.SongPart;
import org.jjazz.songstructure.api.event.SgsChangeEvent;
import org.jjazz.ui.keyboardcomponent.api.KeyboardComponent;
import org.jjazz.ui.keyboardcomponent.api.KeyboardRange;
import org.jjazz.uisettings.api.GeneralUISettings;
import org.jjazz.util.api.ResUtil;
import org.openide.*;
import org.openide.util.Exceptions;

/**
 * The arranger UI.
 */
public class ArrangerPanel extends javax.swing.JPanel implements PropertyChangeListener, SgsChangeListener
{

    private final Font chordSymbolFont;
    private Transmitter transmitterKbdComponent;
    private Arranger arranger;
    private Song song;
    private MidiMix midiMix;
    private SongPart songPart;
    private final ChordSymbolFinder chordSymbolFinder;
    private Transmitter transmitterChordSymbolFinder;
    private ChordReceiver chordReceiver;
    private final Future<?> chordSymbolFinderBuildFuture;
    private static final Logger LOGGER = Logger.getLogger(ArrangerPanel.class.getSimpleName());  //NOI18N  

    /**
     * Creates new form ArrangerPanel
     */
    public ArrangerPanel()
    {
        // Used by initComponents
        chordSymbolFont = GeneralUISettings.getInstance().getStdCondensedFont().deriveFont(Font.BOLD, 16f);
        initComponents();


        // Listen to active song changes
        var asm = ActiveSongManager.getInstance();
        asm.addPropertyListener(this);
        activeSongChanged(asm.getActiveSong(), asm.getActiveMidiMix());


        // Prepare the data
        chordSymbolFinderBuildFuture = Executors.newSingleThreadExecutor().submit(() -> ChordSymbolFinder.buildStaticData());   // Can take a few seconds on slow computers
        chordSymbolFinder = new ChordSymbolFinder(4);
    }

    public void closing()
    {
        if (transmitterChordSymbolFinder != null)
        {
            transmitterChordSymbolFinder.close();
        }
        if (transmitterKbdComponent != null)
        {
            transmitterKbdComponent.close();
        }
        if (chordReceiver != null)
        {
            chordReceiver.close();
        }
    }

    public void opened()
    {
        if (transmitterKbdComponent != null)
        {
            transmitterKbdComponent.close();
        }
        transmitterKbdComponent = JJazzMidiSystem.getInstance().getJJazzMidiInDevice().getTransmitter();
        transmitterKbdComponent.setReceiver(new PianoReceiver());


        if (transmitterChordSymbolFinder != null)
        {
            transmitterChordSymbolFinder.close();
        }
        chordReceiver = new ChordReceiver();
        chordReceiver.addChordListener(this::processIncomingChord);
        transmitterChordSymbolFinder = JJazzMidiSystem.getInstance().getJJazzMidiInDevice().getTransmitter();
        transmitterChordSymbolFinder.setReceiver(chordReceiver);
    }


    @Override
    public void setEnabled(boolean b)
    {
        super.setEnabled(b);
        lbl_chordSymbol.setEnabled(b);
        lbl_rhythm.setEnabled(b);
        lbl_songPart.setEnabled(b);
        kbdComponent.setEnabled(b);
        updateSptUI();
    }
    // ================================================================================    
    // PropertyChangeListener interface
    // ================================================================================   

    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getSource() == ActiveSongManager.getInstance())
        {
            if (evt.getPropertyName().equals(ActiveSongManager.PROP_ACTIVE_SONG))
            {
                activeSongChanged((Song) evt.getNewValue(), (MidiMix) evt.getOldValue());
            }
        } else if (evt.getSource() == arranger)
        {
            if (evt.getPropertyName().equals(Arranger.PROP_PLAYING))
            {
                arrangerStopped();
            }
        }
    }

    // ================================================================================    
    // SgsChangeListener interface
    // ================================================================================   
    @Override
    public void authorizeChange(SgsChangeEvent e) throws UnsupportedEditException
    {
        // Nothing
    }

    @Override
    public void songStructureChanged(SgsChangeEvent e)
    {
        updateSptUI();
    }

    // ================================================================================    
    // Private methods
    // ================================================================================    
    private void togglePlayPause()
    {
        try
        {
            // Wait in case ChordSymbolFinder.buildStaticData() is not yet complete (
            chordSymbolFinderBuildFuture.get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex)
        {
            // Should never happen
            Exceptions.printStackTrace(ex);
            return;
        }

        if (tbtn_playPause.isSelected())
        {

            // Check everything is OK
            if (song == null)
            {
                String msg = ResUtil.getString(getClass(), "ErrNoActiveSong");
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                tbtn_playPause.setSelected(false);
                return;
            }
            if (song.getSongStructure().getSongParts().isEmpty())
            {
                String msg = ResUtil.getString(getClass(), "ErrNoSongPart");
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                // Rollback UI
                tbtn_playPause.setSelected(false);
                return;
            }
            songPart = song.getSongStructure().getSongParts().get(0);

            // Prepare the arranger 
            SongContext sgContext = new SongContext(song, midiMix, songPart.getBarRange());
            if (arranger != null)
            {
                arranger.removePropertyListener(this);
                arranger.cleanup();
            }
            arranger = new Arranger(sgContext);
            arranger.addPropertyListener(this);


            // Start playback
            try
            {
                arranger.play();
            } catch (MusicGenerationException ex)
            {
                NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                // Rollback UI
                tbtn_playPause.setSelected(false);
                arranger.removePropertyListener(this);
                arranger.cleanup();
                arranger = null;
                return;
            }

            updateSptUI();
        } else
        {
            arranger.stop();
        }
    }


    private void activeSongChanged(Song sg, MidiMix mm)
    {
        if (song != sg)
        {
            if (arranger != null)
            {
                arranger.stop();
            }

            if (song != null)
            {
                song.getSongStructure().removeSgsChangeListener(this);
            }

            song = sg;
            midiMix = mm;

            if (song != null)
            {
                song.getSongStructure().addSgsChangeListener(this);
            }
        }

        updateSptUI();
    }

    private void processIncomingChord(List<Note> notes)
    {
        LOGGER.severe("processIncomingChord() -- notes=" + notes);
        if (notes.size() < 3 || notes.size() > chordSymbolFinder.getMaxNbNotes())
        {
            return;
        }
        var chordSymbols = chordSymbolFinder.find(notes);
        LOGGER.severe("                  chordSymbols=" + chordSymbols);
        if (chordSymbols != null)
        {
            var chordSymbol = chordSymbolFinder.getChordSymbol(notes, chordSymbols, cb_lowerNoteIsBass.isSelected());
            if (chordSymbol != null)
            {
                if (arranger != null)
                {
                    arranger.updateChordSymbol(chordSymbol);
                }
                lbl_chordSymbol.setText(chordSymbol.getName());
            }
        }
    }


    /**
     * Update UI depending on the first SongPart of current song and the playback mode.
     *
     * @param spt
     */
    private void updateSptUI()
    {
        LOGGER.info("updateSptUI() -- songPart=" + songPart);
        String sSpt, sRhythm;
        if (songPart == null)
        {
            sSpt = "-";
            sRhythm = "-";
        } else
        {
            sSpt = ResUtil.getString(getClass(), "SongPartRef", songPart.getName(), songPart.getStartBarIndex() + 1);

            Rhythm r = songPart.getRhythm();
            var rpVariation = RP_STD_Variation.getVariationRp(r);
            String strVariation = rpVariation == null ? "" : "/" + songPart.getRPValue(rpVariation);
            sRhythm = r.getName() + strVariation;
        }

        lbl_songPart.setText(sSpt);
        lbl_rhythm.setText(sRhythm);

        boolean b = arranger != null && arranger.isPlaying()
                && songPart != null && song != null
                && song.getSongStructure().getSongParts().contains(songPart);
        lbl_songPart.setEnabled(b);
        lbl_rhythm.setEnabled(b);
    }

    private void arrangerStopped()
    {
        LOGGER.info("arrangerStopped() --");
        arranger.cleanup();
        tbtn_playPause.setSelected(false);
        songPart = null;
        updateSptUI();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        kbdComponent = new KeyboardComponent(KeyboardRange._76_KEYS);
        lbl_chordSymbol = new javax.swing.JLabel();
        lbl_songPart = new javax.swing.JLabel();
        lbl_rhythm = new javax.swing.JLabel();
        tbtn_playPause = new org.jjazz.ui.flatcomponents.api.FlatToggleButton();
        cb_lowerNoteIsBass = new javax.swing.JCheckBox();
        flatHelpButton1 = new org.jjazz.ui.flatcomponents.api.FlatHelpButton();

        kbdComponent.setPreferredSize(new java.awt.Dimension(300, 60));

        lbl_chordSymbol.setFont(chordSymbolFont);
        lbl_chordSymbol.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(lbl_chordSymbol, "Bb7M"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbl_songPart, "Song Part \"A\" bar 23"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbl_rhythm, "SlowBossa-s232.sty - Main A"); // NOI18N

        tbtn_playPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jjazz/arranger/resources/PlayPause.png"))); // NOI18N
        tbtn_playPause.setToolTipText(org.openide.util.NbBundle.getMessage(ArrangerPanel.class, "ArrangerPanel.tbtn_playPause.toolTipText")); // NOI18N
        tbtn_playPause.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jjazz/arranger/resources/PlayPause-ON.png"))); // NOI18N
        tbtn_playPause.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                tbtn_playPauseActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cb_lowerNoteIsBass, org.openide.util.NbBundle.getMessage(ArrangerPanel.class, "ArrangerPanel.cb_lowerNoteIsBass.text")); // NOI18N
        cb_lowerNoteIsBass.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        flatHelpButton1.setHelpText(org.openide.util.NbBundle.getMessage(ArrangerPanel.class, "ArrangerPanel.flatHelpButton1.helpText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(kbdComponent, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_chordSymbol, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbl_rhythm)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cb_lowerNoteIsBass, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(flatHelpButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(lbl_songPart)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(tbtn_playPause, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_songPart)
                    .addComponent(tbtn_playPause, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_chordSymbol)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(kbdComponent, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_rhythm)
                    .addComponent(cb_lowerNoteIsBass)
                    .addComponent(flatHelpButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tbtn_playPauseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_tbtn_playPauseActionPerformed
    {//GEN-HEADEREND:event_tbtn_playPauseActionPerformed
        togglePlayPause();
    }//GEN-LAST:event_tbtn_playPauseActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox cb_lowerNoteIsBass;
    private org.jjazz.ui.flatcomponents.api.FlatHelpButton flatHelpButton1;
    private org.jjazz.ui.keyboardcomponent.api.KeyboardComponent kbdComponent;
    private javax.swing.JLabel lbl_chordSymbol;
    private javax.swing.JLabel lbl_rhythm;
    private javax.swing.JLabel lbl_songPart;
    private org.jjazz.ui.flatcomponents.api.FlatToggleButton tbtn_playPause;
    // End of variables declaration//GEN-END:variables


    // ================================================================================    
    // Private classes
    // ================================================================================ 
    private class PianoReceiver implements Receiver
    {

        @Override
        public void send(MidiMessage msg, long timeStamp)
        {
            if (!ArrangerPanel.this.isShowing())        // But might be showing AND hidden behind another TopComponent
            {
                return;
            }

            ShortMessage noteMsg = MidiUtilities.getNoteOnMidiEvent(msg);
            if (noteMsg != null)
            {
                // Note ON
                int pitch = noteMsg.getData1();
                int velocity = noteMsg.getData2();

                kbdComponent.setPressed(pitch, velocity, null);

            } else if ((noteMsg = MidiUtilities.getNoteOffMidiEvent(msg)) != null)
            {
                // Note OFF
                int pitch = noteMsg.getData1();
                kbdComponent.setReleased(pitch);
            }
        }

        @Override
        public void close()
        {
            // Nothing
        }

    }
}

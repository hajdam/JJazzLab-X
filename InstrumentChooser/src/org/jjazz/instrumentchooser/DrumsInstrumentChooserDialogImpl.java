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
package org.jjazz.instrumentchooser;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.jjazz.instrumentchooser.api.DrumsInstrumentChooserDialog;
import org.jjazz.midi.DrumKitType;
import org.jjazz.midisynth.api.MidiSynthManager;
import org.jjazz.midi.DrumsInstrument;
import org.jjazz.midi.Instrument;
import org.jjazz.midi.InstrumentBank;
import org.jjazz.midi.JJazzMidiSystem;
import org.jjazz.midi.MidiConst;
import org.jjazz.midi.MidiSynth;
import org.jjazz.midi.DrumMap;
import org.jjazz.musiccontrol.MusicController;
import org.jjazz.rhythmmusicgeneration.MusicGenerationException;
import org.jjazz.ui.utilities.HelpTextArea;
import org.jjazz.util.Filter;
import org.openide.*;
import org.openide.windows.WindowManager;

public class DrumsInstrumentChooserDialogImpl extends DrumsInstrumentChooserDialog implements ListSelectionListener
{

    private static DrumsInstrumentChooserDialogImpl INSTANCE;
    private DrumsInstrumentsTable drumsInstrumentsTable;
    private DrumsInstrument selectedInstrument;
    private int channel;
    private DrumKitType drumKitType;
    private DrumMap drumMap;
    private Filter<Instrument> insFilter;
    private Instrument initInstrument;

    private static final Logger LOGGER = Logger.getLogger(DrumsInstrumentChooserDialogImpl.class.getSimpleName());

    static public DrumsInstrumentChooserDialogImpl getInstance()
    {
        synchronized (DrumsInstrumentChooserDialogImpl.class)
        {
            if (INSTANCE == null)
            {
                INSTANCE = new DrumsInstrumentChooserDialogImpl();
                INSTANCE.setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
            }
        }
        return INSTANCE;
    }

    private DrumsInstrumentChooserDialogImpl()
    {
        setModal(true);
        drumsInstrumentsTable = new DrumsInstrumentsTable();
        initComponents();
        this.setTitle("Drums/Percussion Selection Dialog");
        // Prepare table           
        drumsInstrumentsTable.getSelectionModel().addListSelectionListener(this);
        drumsInstrumentsTable.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                handleTableMouseClicked(e);
            }
        });
    }

    @Override
    public void preset(DrumKitType kitType, DrumMap drumMap, DrumsInstrument ins, int chan, String title, Filter<Instrument> filter)
    {
        if (kitType == null || drumMap == null || MidiConst.checkMidiChannel(chan) || title == null)
        {
            throw new IllegalArgumentException("kitType=" + kitType + " drumMap=" + drumMap + " ins=" + ins + " chan=" + chan);
        }
        insFilter = filter;
        if (insFilter == null)
        {
            insFilter = new Filter<Instrument>()
            {
                @Override
                public boolean accept(Instrument ins)
                {
                    return true;
                }
            };
        }

        initInstrument = ins;
        channel = chan;

        // Update the table
        drumsInstrumentsTable.populate(getAllDrumsInstruments());

        // Update the filter : depending on insFilter and checkboxes
        updateTableFilter();

        // Update text
        String s = cb_onlyMatchingDrumKitType.getText();
        s = s.substring(0, s.indexOf(':') + 1) + " " + kitType.toString();
        cb_onlyMatchingDrumKitType.setText(s);
        s = cb_onlyMatchingDrumMap.getText();
        s = s.substring(0, s.indexOf(':') + 1) + " " + drumMap.toString();
        cb_onlyMatchingDrumMap.setText(s);

        if (initInstrument != null)
        {
            drumsInstrumentsTable.setSelected(ins);
        } else
        {
            drumsInstrumentsTable.setRowSelectionInterval(0, 0);
        }
        lbl_Title.setText(title);
    }

    @Override
    public DrumsInstrument getSelectedInstrument()
    {
        return selectedInstrument;
    }

    /**
     * Overridden to add global key bindings
     *
     * @return
     */
    @Override
    protected JRootPane createRootPane()
    {
        JRootPane contentPane = new JRootPane();
        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ENTER"), "actionOk");
        contentPane.getActionMap().put("actionOk", new AbstractAction("OK")
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                btn_OkActionPerformed(null);
            }
        });

        contentPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke("ESCAPE"), "actionCancel");
        contentPane.getActionMap().put("actionCancel", new AbstractAction("Cancel")
        {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                btn_CancelActionPerformed(null);
            }
        });
        return contentPane;
    }

    // ===================================================================================
    // ListSelectionListener interfacce
    // ===================================================================================
    @Override
    public void valueChanged(ListSelectionEvent e)
    {
        LOGGER.log(Level.FINE, "valueChanged() e={0}", e);
        if (e.getValueIsAdjusting())
        {
            return;
        }
        if (e.getSource() == drumsInstrumentsTable.getSelectionModel())
        {
            selectedInstrument = drumsInstrumentsTable.getSelectedInstrument();                 // may be null            
        }
    }

    // ============================================================================================
    // Private methods
    // ============================================================================================    
    private void handleTableMouseClicked(MouseEvent evt)
    {
        boolean ctrl = (evt.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK;
        boolean shift = (evt.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK;
        if (SwingUtilities.isLeftMouseButton(evt))
        {
            if (evt.getClickCount() == 2 && !ctrl && !shift)
            {
                btn_OkActionPerformed(null);
            }
        }
    }

    private void updateTableFilter()
    {
        // Reset filter
        TableRowSorter<? extends TableModel> sorter = (TableRowSorter<? extends TableModel>) drumsInstrumentsTable.getRowSorter();
        sorter.setRowFilter(null);

        // Recreate the filter
        RowFilter<TableModel, Integer> rowFilter = new RowFilter<TableModel, Integer>()
        {
            @Override
            public boolean include(Entry<? extends TableModel, ? extends Integer> entry)
            {
                DrumsInstrument di = (DrumsInstrument) entry.getValue(2);       // The instrument value
                boolean b;
                if (!insFilter.accept(di))
                {
                    b = false;
                } else if (!cb_onlyMatchingDrumKitType.isSelected() && !cb_onlyMatchingDrumMap.isSelected())
                {
                    b = true;
                } else if (cb_onlyMatchingDrumKitType.isSelected() && !cb_onlyMatchingDrumMap.isSelected())
                {
                    b = di.getDrumKitType().equals(drumKitType);
                } else if (!cb_onlyMatchingDrumKitType.isSelected() && cb_onlyMatchingDrumMap.isSelected())
                {
                    b = di.getDrumMap().equals(drumMap);
                } else
                {
                    b = di.getDrumKitType().equals(drumKitType) && di.getDrumMap().equals(drumMap);
                }
                return b;
            }
        };
        sorter.setRowFilter(rowFilter);
    }

    private List<DrumsInstrument> getAllDrumsInstruments()
    {
        ArrayList<DrumsInstrument> res = new ArrayList<>();
        for (MidiSynth synth : MidiSynthManager.getInstance().getSynths())
        {
            for (InstrumentBank<?> bank : synth.getBanks())
            {
                for (Instrument ins : bank.getInstruments())
                {
                    if (ins instanceof DrumsInstrument)
                    {
                        res.add((DrumsInstrument) ins);
                    }
                }
            }
        }
        return res;
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        btn_Ok = new javax.swing.JButton();
        btn_Cancel = new javax.swing.JButton();
        btn_Hear = new javax.swing.JButton();
        lbl_Title = new javax.swing.JLabel();
        jTextArea1 = new HelpTextArea();
        jScrollPane4 = new javax.swing.JScrollPane();
        tbl_instruments = drumsInstrumentsTable;
        cb_onlyMatchingDrumKitType = new javax.swing.JCheckBox();
        cb_onlyMatchingDrumMap = new javax.swing.JCheckBox();

        org.openide.awt.Mnemonics.setLocalizedText(btn_Ok, org.openide.util.NbBundle.getMessage(DrumsInstrumentChooserDialogImpl.class, "DrumsInstrumentChooserDialogImpl.btn_Ok.text")); // NOI18N
        btn_Ok.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_OkActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_Cancel, org.openide.util.NbBundle.getMessage(DrumsInstrumentChooserDialogImpl.class, "DrumsInstrumentChooserDialogImpl.btn_Cancel.text")); // NOI18N
        btn_Cancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_CancelActionPerformed(evt);
            }
        });

        btn_Hear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jjazz/instrumentchooser/resources/Speaker-20x20.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btn_Hear, org.openide.util.NbBundle.getMessage(DrumsInstrumentChooserDialogImpl.class, "DrumsInstrumentChooserDialogImpl.btn_Hear.text")); // NOI18N
        btn_Hear.setToolTipText(org.openide.util.NbBundle.getMessage(DrumsInstrumentChooserDialogImpl.class, "DrumsInstrumentChooserDialogImpl.btn_Hear.toolTipText")); // NOI18N
        btn_Hear.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_HearActionPerformed(evt);
            }
        });

        lbl_Title.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(lbl_Title, org.openide.util.NbBundle.getMessage(DrumsInstrumentChooserDialogImpl.class, "DrumsInstrumentChooserDialogImpl.lbl_Title.text")); // NOI18N

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(DrumsInstrumentChooserDialogImpl.class, "DrumsInstrumentChooserDialogImpl.jTextArea1.text")); // NOI18N
        jTextArea1.setWrapStyleWord(true);
        jTextArea1.setOpaque(false);

        tbl_instruments.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][]
            {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String []
            {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(tbl_instruments);

        cb_onlyMatchingDrumKitType.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cb_onlyMatchingDrumKitType, org.openide.util.NbBundle.getMessage(DrumsInstrumentChooserDialogImpl.class, "DrumsInstrumentChooserDialogImpl.cb_onlyMatchingDrumKitType.text")); // NOI18N
        cb_onlyMatchingDrumKitType.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cb_onlyMatchingDrumKitTypeActionPerformed(evt);
            }
        });

        cb_onlyMatchingDrumMap.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(cb_onlyMatchingDrumMap, org.openide.util.NbBundle.getMessage(DrumsInstrumentChooserDialogImpl.class, "DrumsInstrumentChooserDialogImpl.cb_onlyMatchingDrumMap.text")); // NOI18N
        cb_onlyMatchingDrumMap.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cb_onlyMatchingDrumMapActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cb_onlyMatchingDrumKitType)
                            .addComponent(cb_onlyMatchingDrumMap))
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(141, 141, 141)
                                .addComponent(btn_Cancel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btn_Ok))
                            .addGroup(layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(btn_Hear, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(lbl_Title, javax.swing.GroupLayout.PREFERRED_SIZE, 375, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(lbl_Title, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(cb_onlyMatchingDrumKitType)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cb_onlyMatchingDrumMap)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btn_Hear)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btn_Cancel)
                            .addComponent(btn_Ok)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 401, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    private void btn_CancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_CancelActionPerformed
    {//GEN-HEADEREND:event_btn_CancelActionPerformed
        if (initInstrument != null && channel >= MidiConst.CHANNEL_MIN)
        {
            // Send the Midi message to restore the original instrument
            JJazzMidiSystem.getInstance().sendMidiMessagesOnJJazzMidiOut(initInstrument.getMidiMessages(channel));
        }
        selectedInstrument = null;
        setVisible(false);
    }//GEN-LAST:event_btn_CancelActionPerformed

    private void btn_OkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_OkActionPerformed
    {//GEN-HEADEREND:event_btn_OkActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btn_OkActionPerformed

    private void btn_HearActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_HearActionPerformed
    {//GEN-HEADEREND:event_btn_HearActionPerformed
        tbl_instruments.setEnabled(false);
        btn_Hear.setEnabled(false);
        btn_Ok.setEnabled(false);
        btn_Cancel.setEnabled(false);

        Runnable endAction = new Runnable()
        {
            @Override
            public void run()
            {
                tbl_instruments.setEnabled(true);
                btn_Hear.setEnabled(true);
                btn_Ok.setEnabled(true);
                btn_Cancel.setEnabled(true);
            }
        };
        // Send MIDI messages for the selected instrument             
        MusicController mc = MusicController.getInstance();
        try
        {
            mc.playTestNotes(channel, -1, -12, endAction);
        } catch (MusicGenerationException ex)
        {
            NotifyDescriptor d = new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }

    }//GEN-LAST:event_btn_HearActionPerformed

    private void cb_onlyMatchingDrumKitTypeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cb_onlyMatchingDrumKitTypeActionPerformed
    {//GEN-HEADEREND:event_cb_onlyMatchingDrumKitTypeActionPerformed
        this.updateTableFilter();
    }//GEN-LAST:event_cb_onlyMatchingDrumKitTypeActionPerformed

    private void cb_onlyMatchingDrumMapActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cb_onlyMatchingDrumMapActionPerformed
    {//GEN-HEADEREND:event_cb_onlyMatchingDrumMapActionPerformed
        this.updateTableFilter();
    }//GEN-LAST:event_cb_onlyMatchingDrumMapActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_Cancel;
    private javax.swing.JButton btn_Hear;
    private javax.swing.JButton btn_Ok;
    private javax.swing.JCheckBox cb_onlyMatchingDrumKitType;
    private javax.swing.JCheckBox cb_onlyMatchingDrumMap;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JLabel lbl_Title;
    private javax.swing.JTable tbl_instruments;
    // End of variables declaration//GEN-END:variables

}

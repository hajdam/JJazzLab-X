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
package org.jjazz.helpers.midiwizard;

import javax.swing.JPanel;
import org.jjazz.helpers.midiwizard.MidiWizardSettings.SoundDevice;

public final class MidiWizardVisualPanel2 extends JPanel
{

    private SoundDevice selectedSoundDevice = SoundDevice.SYNTHESIZER;

    /**
     * Creates new form StartupWizardVisualPanel2
     */
    public MidiWizardVisualPanel2()
    {
        initComponents();
        updateText();
    }

    public SoundDevice getSelectedSoundDevice()
    {
        return selectedSoundDevice;
    }

    public void setSelectedSoundDevice(SoundDevice sd)
    {
        selectedSoundDevice = sd;
        switch (selectedSoundDevice)
        {
            case SYNTHESIZER:
                this.rbtn_synth.setSelected(true);
                break;
            case VIRTUAL_INSTRUMENT:
                this.rbtn_virtualsynth.setSelected(true);
                break;
            case OTHER:
                this.rbtn_other.setSelected(true);
                break;
            default:
                throw new IllegalStateException("selectedSoundDevice=" + selectedSoundDevice);
        }
        updateText();
    }

    @Override
    public String getName()
    {
        return "Sound device type";
    }

    private void updateText()
    {
        String s;
        switch (selectedSoundDevice)
        {
            case SYNTHESIZER:
                s = "This is usually a good option to have high quality sounds with an easy setup.\n\n"
                        + "Make sure there is the appropriate Midi or USB cable connection between computer and synthesizer.\n\n"
                        + "Set your synthesizer in multitimbral mode (different instruments on different Midi channels). GM-compatibility is not mandatory but is recommended.";
                break;
            case VIRTUAL_INSTRUMENT:
                s = "IMPORTANT: you need to have a 'virtual Midi device' installed on your computer.\n\n"
                        + "Virtual instrument can produce high quality sounds but need a specific set up.\n\n"
                        + "Set the 'virtual Midi device' as the Midi Out device of JJazzLab, and as the Midi In device of your virtual instrument/host software."
                        + " On Windows the 'loopMIDI' or 'LoopBe1' applications are good examples of 'virtual Midi devices'.\n\n"
                        + "Set your virtual instrument in multitimbral mode (different instruments on different Midi channels). GM-compatibility is not mandatory but is recommended.";
                break;
            case OTHER:
                s = "IMPORTANT: this may lead to POOR QUALITY sounds.\n\n"
                        + "No setup required, you use a builtin Midi synthesizer available on your computer. \n\n"
                        + "To improve sound quality try to use a device which can load SoundFont files, such as the Java Internal Synth or the VirtualMidiSynth application.";
                break;
            default:
                throw new IllegalStateException("selectedSoundDevice=" + selectedSoundDevice);
        }
        hlptext.setText(s);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        btnGroup = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        hlptext = new org.jjazz.ui.utilities.HelpTextArea();
        rbtn_synth = new javax.swing.JRadioButton();
        rbtn_virtualsynth = new javax.swing.JRadioButton();
        rbtn_other = new javax.swing.JRadioButton();
        lbl_question = new javax.swing.JLabel();

        jScrollPane1.setBorder(null);

        hlptext.setBorder(null);
        hlptext.setColumns(20);
        hlptext.setRows(5);
        hlptext.setText(org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel2.class, "MidiWizardVisualPanel2.hlptext.text")); // NOI18N
        jScrollPane1.setViewportView(hlptext);

        btnGroup.add(rbtn_synth);
        rbtn_synth.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(rbtn_synth, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel2.class, "MidiWizardVisualPanel2.rbtn_synth.text")); // NOI18N
        rbtn_synth.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                rbtn_synthActionPerformed(evt);
            }
        });

        btnGroup.add(rbtn_virtualsynth);
        org.openide.awt.Mnemonics.setLocalizedText(rbtn_virtualsynth, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel2.class, "MidiWizardVisualPanel2.rbtn_virtualsynth.text")); // NOI18N
        rbtn_virtualsynth.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                rbtn_virtualsynthActionPerformed(evt);
            }
        });

        btnGroup.add(rbtn_other);
        org.openide.awt.Mnemonics.setLocalizedText(rbtn_other, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel2.class, "MidiWizardVisualPanel2.rbtn_other.text")); // NOI18N
        rbtn_other.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                rbtn_otherActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lbl_question, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel2.class, "MidiWizardVisualPanel2.lbl_question.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rbtn_other)
                            .addComponent(rbtn_virtualsynth)
                            .addComponent(rbtn_synth)
                            .addComponent(lbl_question))
                        .addGap(0, 41, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_question)
                .addGap(18, 18, 18)
                .addComponent(rbtn_synth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbtn_virtualsynth)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rbtn_other)
                .addGap(36, 36, 36)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbtn_synthActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rbtn_synthActionPerformed
    {//GEN-HEADEREND:event_rbtn_synthActionPerformed
        selectedSoundDevice = SoundDevice.SYNTHESIZER;
        updateText();
    }//GEN-LAST:event_rbtn_synthActionPerformed

    private void rbtn_virtualsynthActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rbtn_virtualsynthActionPerformed
    {//GEN-HEADEREND:event_rbtn_virtualsynthActionPerformed
        selectedSoundDevice = SoundDevice.VIRTUAL_INSTRUMENT;
        updateText();
    }//GEN-LAST:event_rbtn_virtualsynthActionPerformed

    private void rbtn_otherActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_rbtn_otherActionPerformed
    {//GEN-HEADEREND:event_rbtn_otherActionPerformed
        selectedSoundDevice = SoundDevice.OTHER;
        updateText();
    }//GEN-LAST:event_rbtn_otherActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGroup;
    private org.jjazz.ui.utilities.HelpTextArea hlptext;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_question;
    private javax.swing.JRadioButton rbtn_other;
    private javax.swing.JRadioButton rbtn_synth;
    private javax.swing.JRadioButton rbtn_virtualsynth;
    // End of variables declaration//GEN-END:variables

}

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

import java.io.File;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jjazz.ui.utilities.api.Utilities;
import org.jjazz.util.api.ResUtil;
import org.openide.windows.WindowManager;

public final class MidiWizardVisualPanel_SfMac_1 extends JPanel
{

    /**
     * Creates new form StartupWizardVisualPanel1
     */
    public MidiWizardVisualPanel_SfMac_1()
    {
        initComponents();
    }

    @Override
    public String getName()
    {
        return ResUtil.getString(getClass(), "MidiWizardVisualPanel_SfMac_1.Name");
    }

    public void setSoundFile(File f)
    {
        tf_soundFont.setText(f == null ? "-" : f.getAbsolutePath());
    }

    /**
     *
     * @return Can be null
     */
    public File getSoundFile()
    {
        String s = tf_soundFont.getText();
        return s == null || s.equals("-") ? null : new File(s);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        buttonGroup1 = new javax.swing.ButtonGroup();
        ta_notUserOnlyToGetFont = new org.jjazz.ui.utilities.api.WizardTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        editorPane = new javax.swing.JEditorPane();
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE); // To make setFont work
        btn_load = new javax.swing.JButton();
        tf_soundFont = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        ta_notUserOnlyToGetFont.setEditable(false);
        ta_notUserOnlyToGetFont.setColumns(20);
        ta_notUserOnlyToGetFont.setTabSize(2);

        jScrollPane2.setBorder(null);

        editorPane.setEditable(false);
        editorPane.setBorder(null);
        editorPane.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));
        editorPane.setFont(ta_notUserOnlyToGetFont.getFont());
        editorPane.setText(org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel_SfMac_1.class, "MidiWizardVisualPanel_SfMac_1.editorPane.text")); // NOI18N
        editorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener()
        {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt)
            {
                editorPaneHyperlinkUpdate(evt);
            }
        });
        jScrollPane2.setViewportView(editorPane);

        org.openide.awt.Mnemonics.setLocalizedText(btn_load, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel_SfMac_1.class, "MidiWizardVisualPanel_SfMac_1.btn_load.text")); // NOI18N
        btn_load.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_loadActionPerformed(evt);
            }
        });

        tf_soundFont.setEditable(false);
        tf_soundFont.setText("jTextField1"); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(MidiWizardVisualPanel_SfMac_1.class, "MidiWizardVisualPanel_SfMac_1.jLabel1.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 532, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tf_soundFont, javax.swing.GroupLayout.PREFERRED_SIZE, 272, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGap(67, 67, 67)
                                        .addComponent(ta_notUserOnlyToGetFont, javax.swing.GroupLayout.PREFERRED_SIZE, 143, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btn_load))))
                            .addComponent(jLabel1))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(ta_notUserOnlyToGetFont, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(tf_soundFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_load))
                        .addGap(0, 9, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void editorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt)//GEN-FIRST:event_editorPaneHyperlinkUpdate
    {//GEN-HEADEREND:event_editorPaneHyperlinkUpdate
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
        {
            org.jjazz.util.api.Utilities.openInBrowser(evt.getURL(), false);
        }
    }//GEN-LAST:event_editorPaneHyperlinkUpdate

    private void btn_loadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_loadActionPerformed
    {//GEN-HEADEREND:event_btn_loadActionPerformed
        JFileChooser chooser = Utilities.getFileChooserInstance();
        chooser.resetChoosableFileFilters();
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("JJazzLab SoundFont file (.sf2)", "sf2"));
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setMultiSelectionEnabled(false);
        File f = getSoundFile();
        if (f == null)
        {
            f = new File("");
        }
        chooser.setSelectedFile(f);
        chooser.setDialogTitle("JJazzLab SoundFont file location");
        if (chooser.showOpenDialog(WindowManager.getDefault().getMainWindow()) != JFileChooser.APPROVE_OPTION)
        {
            // User cancelled
            return;
        }
        f = chooser.getSelectedFile();
        if (f != null)
        {
            setSoundFile(f);
        }
    }//GEN-LAST:event_btn_loadActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_load;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JEditorPane editorPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane2;
    private org.jjazz.ui.utilities.api.WizardTextArea ta_notUserOnlyToGetFont;
    private javax.swing.JTextField tf_soundFont;
    // End of variables declaration//GEN-END:variables
}

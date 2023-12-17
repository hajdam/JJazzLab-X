/*
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 *  Copyright @2019 Jerome Lelasseux. All rights reserved.
 *
 *  This file is part of the JJazzLab software.
 *   
 *  JJazzLab is free software: you can redistribute it and/or modify
 *  it under the terms of the Lesser GNU General Public License (LGPLv3) 
 *  as published by the Free Software Foundation, either version 3 of the License, 
 *  or (at your option) any later version.
 *
 *  JJazzLab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JJazzLab.  If not, see <https://www.gnu.org/licenses/>
 * 
 *  Contributor(s): 
 */
package org.jjazz.cl_editor.actions;

import org.jjazz.cl_editor.api.CL_Editor;
import org.jjazz.uiutilities.api.UIUtilities;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jerome
 */
public class EditorSettingsDialog extends javax.swing.JDialog
{

    private final CL_Editor editor;
    private final int saveNbCols;
    private final int saveNbAnnotationLines;

    /**
     * Creates new form EditorSettingsDialog
     */
    public EditorSettingsDialog(CL_Editor editor)
    {
        super(WindowManager.getDefault().getMainWindow(), true);

        this.editor = editor;

        initComponents();

        UIUtilities.installEnterKeyAction(this, () -> btn_okActionPerformed(null));
        UIUtilities.installEscapeKeyAction(this, () -> btn_cancelActionPerformed(null));
    
        // Init UI
        saveNbCols = editor.getNbColumns();
        saveNbAnnotationLines = editor.getBarAnnotationNbLines();

        spn_nbCols.setValue(saveNbCols);
        spn_nbAnnotationLines.setValue(saveNbAnnotationLines);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        spn_nbCols = new org.jjazz.uiutilities.api.WheelSpinner();
        jLabel1 = new javax.swing.JLabel();
        btn_cancel = new javax.swing.JButton();
        btn_ok = new javax.swing.JButton();
        spn_nbAnnotationLines = new org.jjazz.uiutilities.api.WheelSpinner();
        lbl_nbLines = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(EditorSettingsDialog.class, "EditorSettingsDialog.title")); // NOI18N

        spn_nbCols.setModel(new javax.swing.SpinnerNumberModel(4, 1, 16, 1));
        spn_nbCols.setColumns(1);
        spn_nbCols.setCtrlWheelStep(2);
        spn_nbCols.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                spn_nbColsStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EditorSettingsDialog.class, "EditorSettingsDialog.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(btn_cancel, org.openide.util.NbBundle.getMessage(EditorSettingsDialog.class, "EditorSettingsDialog.btn_cancel.text")); // NOI18N
        btn_cancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_cancelActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_ok, org.openide.util.NbBundle.getMessage(EditorSettingsDialog.class, "EditorSettingsDialog.btn_ok.text")); // NOI18N
        btn_ok.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_okActionPerformed(evt);
            }
        });

        spn_nbAnnotationLines.setModel(new javax.swing.SpinnerNumberModel(2, 1, 4, 1));
        spn_nbAnnotationLines.setColumns(1);
        spn_nbAnnotationLines.setCtrlWheelStep(2);
        spn_nbAnnotationLines.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                spn_nbAnnotationLinesStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(lbl_nbLines, org.openide.util.NbBundle.getMessage(EditorSettingsDialog.class, "EditorSettingsDialog.lbl_nbLines.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_ok)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_cancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spn_nbCols, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel1))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(spn_nbAnnotationLines, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lbl_nbLines)))
                        .addGap(0, 22, Short.MAX_VALUE)))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_cancel, btn_ok});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spn_nbCols, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spn_nbAnnotationLines, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_nbLines))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_cancel)
                    .addComponent(btn_ok))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_okActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_okActionPerformed
    {//GEN-HEADEREND:event_btn_okActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btn_okActionPerformed

    private void btn_cancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_cancelActionPerformed
    {//GEN-HEADEREND:event_btn_cancelActionPerformed
        // Reverse changes
        editor.setNbColumns(saveNbCols);
        editor.setBarAnnotationNbLines(saveNbAnnotationLines);
        setVisible(false);
        dispose();
    }//GEN-LAST:event_btn_cancelActionPerformed

    private void spn_nbColsStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_spn_nbColsStateChanged
    {//GEN-HEADEREND:event_spn_nbColsStateChanged
        editor.setNbColumns((int) spn_nbCols.getValue());
    }//GEN-LAST:event_spn_nbColsStateChanged

    private void spn_nbAnnotationLinesStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_spn_nbAnnotationLinesStateChanged
    {//GEN-HEADEREND:event_spn_nbAnnotationLinesStateChanged
        editor.setBarAnnotationNbLines((int) spn_nbAnnotationLines.getValue());
    }//GEN-LAST:event_spn_nbAnnotationLinesStateChanged



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_cancel;
    private javax.swing.JButton btn_ok;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lbl_nbLines;
    private org.jjazz.uiutilities.api.WheelSpinner spn_nbAnnotationLines;
    private org.jjazz.uiutilities.api.WheelSpinner spn_nbCols;
    // End of variables declaration//GEN-END:variables

}

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
package org.jjazz.options;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComponent;
import javax.swing.JList;
import static org.jjazz.options.Bundle.ASK_ConfirmResetAll;
import org.openide.awt.ColorComboBox;
import org.openide.util.Lookup;
import org.jjazz.ui.utilities.FontColorUserSettingsProvider;
import org.jjazz.ui.utilities.FontColorUserSettingsProvider.FCSetting;
import org.jjazz.ui.utilities.JFontChooser;
import org.jjazz.util.Utilities;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;

@Messages(
        {
            "ASK_ConfirmResetAll=This will reset all categories values. Are you sure ?"
        })

final class EditorPanel extends javax.swing.JPanel
{

    /**
     * Store the FCSettings values by id.
     */
    HashMap<String, FCvalues> mapIdValues = new HashMap<>();
    List<FCSetting> listValues = new ArrayList<>();
    private final EditorOptionsPanelController controller;
    private ColorComboBox colorComboBox;
    private static final Logger LOGGER = Logger.getLogger(EditorPanel.class.getSimpleName());

    EditorPanel(EditorOptionsPanelController controller)
    {
        this.controller = controller;

        colorComboBox = new ColorComboBox();
        initComponents();

        FCSettingCellRenderer cdr = new FCSettingCellRenderer();
        list_fcSettings.setCellRenderer(cdr);

        // TODO listen to changes in form fields and call controller.changed()
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of
     * this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        lbl_category = new javax.swing.JLabel();
        lbl_font = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list_fcSettings = new javax.swing.JList<>();
        lbl_color = new javax.swing.JLabel();
        tf_font = new javax.swing.JTextField();
        cb_color = colorComboBox;
        btn_font = new javax.swing.JButton();
        btn_resetFont = new javax.swing.JButton();
        btn_resetColor = new javax.swing.JButton();
        btn_resetAll = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(lbl_category, org.openide.util.NbBundle.getMessage(EditorPanel.class, "EditorPanel.lbl_category.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(lbl_font, org.openide.util.NbBundle.getMessage(EditorPanel.class, "EditorPanel.lbl_font.text")); // NOI18N
        lbl_font.setEnabled(false);

        list_fcSettings.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        list_fcSettings.addListSelectionListener(new javax.swing.event.ListSelectionListener()
        {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt)
            {
                list_fcSettingsValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(list_fcSettings);

        org.openide.awt.Mnemonics.setLocalizedText(lbl_color, org.openide.util.NbBundle.getMessage(EditorPanel.class, "EditorPanel.lbl_color.text")); // NOI18N
        lbl_color.setEnabled(false);

        tf_font.setEditable(false);
        tf_font.setEnabled(false);

        cb_color.setEnabled(false);
        cb_color.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                cb_colorActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_font, org.openide.util.NbBundle.getMessage(EditorPanel.class, "EditorPanel.btn_font.text")); // NOI18N
        btn_font.setEnabled(false);
        btn_font.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_fontActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_resetFont, org.openide.util.NbBundle.getMessage(EditorPanel.class, "EditorPanel.btn_resetFont.text")); // NOI18N
        btn_resetFont.setEnabled(false);
        btn_resetFont.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_resetFontActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_resetColor, org.openide.util.NbBundle.getMessage(EditorPanel.class, "EditorPanel.btn_resetColor.text")); // NOI18N
        btn_resetColor.setEnabled(false);
        btn_resetColor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_resetColorActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_resetAll, org.openide.util.NbBundle.getMessage(EditorPanel.class, "EditorPanel.btn_resetAll.text")); // NOI18N
        btn_resetAll.setEnabled(false);
        btn_resetAll.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_resetAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_font)
                            .addComponent(lbl_color))
                        .addGap(4, 4, 4)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(tf_font, javax.swing.GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
                            .addComponent(cb_color, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_font)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btn_resetFont)
                            .addComponent(btn_resetColor)
                            .addComponent(btn_resetAll)))
                    .addComponent(lbl_category))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {lbl_color, lbl_font});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_resetColor, btn_resetFont});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_category)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbl_font)
                            .addComponent(tf_font, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_font)
                            .addComponent(btn_resetFont))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbl_color)
                            .addComponent(cb_color, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btn_resetColor))
                        .addGap(18, 18, 18)
                        .addComponent(btn_resetAll))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

   private void btn_fontActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_fontActionPerformed
   {//GEN-HEADEREND:event_btn_fontActionPerformed
       FCSetting fcs = list_fcSettings.getSelectedValue();
       assert fcs != null;

       JFontChooser jfc = new JFontChooser();
       jfc.setSelectedFont(fcs.getFont());
       int res = jfc.showDialog(this);

       if (res == JFontChooser.OK_OPTION)
       {
           Font newFont = jfc.getSelectedFont();
           fcs.setFont(newFont);
           tf_font.setText(Utilities.fontAsString(newFont));
           tf_font.setFont(newFont);
       }
   }//GEN-LAST:event_btn_fontActionPerformed

   private void cb_colorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cb_colorActionPerformed
   {//GEN-HEADEREND:event_cb_colorActionPerformed
       Color c = colorComboBox.getSelectedColor();
       if (c != null)
       {
           FCSetting fcs = list_fcSettings.getSelectedValue();
           assert fcs != null;
           fcs.setColor(c);
       }
   }//GEN-LAST:event_cb_colorActionPerformed

   private void list_fcSettingsValueChanged(javax.swing.event.ListSelectionEvent evt)//GEN-FIRST:event_list_fcSettingsValueChanged
   {//GEN-HEADEREND:event_list_fcSettingsValueChanged
       if (evt != null && evt.getValueIsAdjusting())
       {
           return;
       }
       // Reset everything by default
       lbl_font.setEnabled(false);
       btn_font.setEnabled(false);
       tf_font.setEnabled(false);
       tf_font.setText("");
       btn_resetFont.setEnabled(false);
       lbl_color.setEnabled(false);
       colorComboBox.setEnabled(false);
       btn_resetColor.setEnabled(false);

       FCSetting fcs = list_fcSettings.getSelectedValue();
       if (fcs != null)
       {
           Font f = fcs.getFont();
           if (f != null)
           {
               btn_font.setEnabled(true);
               tf_font.setEnabled(true);
               lbl_font.setEnabled(true);
               btn_resetFont.setEnabled(true);
               tf_font.setText(Utilities.fontAsString(f));
               tf_font.setFont(f);
           }
           Color c = fcs.getColor();
           if (c != null)
           {
               colorComboBox.setEnabled(true);
               colorComboBox.setSelectedColor(c);
               lbl_color.setEnabled(true);
               btn_resetColor.setEnabled(true);
           }
       }
       controller.applyChanges();
       controller.changed();
   }//GEN-LAST:event_list_fcSettingsValueChanged

   private void btn_resetAllActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_resetAllActionPerformed
   {//GEN-HEADEREND:event_btn_resetAllActionPerformed

       NotifyDescriptor nd = new NotifyDescriptor.Confirmation(ASK_ConfirmResetAll(), NotifyDescriptor.OK_CANCEL_OPTION);
       Object result = DialogDisplayer.getDefault().notify(nd);
       if (result != NotifyDescriptor.OK_OPTION)
       {
           return;
       }
       for (FCSetting fcs : listValues)
       {
           fcs.setColor(null);
           fcs.setFont(null);
       }
       list_fcSettingsValueChanged(null);
   }//GEN-LAST:event_btn_resetAllActionPerformed

   private void btn_resetFontActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_resetFontActionPerformed
   {//GEN-HEADEREND:event_btn_resetFontActionPerformed
       FCSetting fcs = list_fcSettings.getSelectedValue();
       assert fcs != null;
       fcs.setFont(null);
       list_fcSettingsValueChanged(null);
   }//GEN-LAST:event_btn_resetFontActionPerformed

   private void btn_resetColorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_resetColorActionPerformed
   {//GEN-HEADEREND:event_btn_resetColorActionPerformed
       FCSetting fcs = list_fcSettings.getSelectedValue();
       assert fcs != null;
       fcs.setColor(null);
       list_fcSettingsValueChanged(null);
   }//GEN-LAST:event_btn_resetColorActionPerformed

    void load()
    {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(EditorPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(EditorPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());

        mapIdValues.clear();
        listValues.clear();
        Collection<? extends FontColorUserSettingsProvider> result = Lookup.getDefault().lookupAll(FontColorUserSettingsProvider.class);
        for (FontColorUserSettingsProvider p : result)
        {
            List<FCSetting> pFcsSettings = p.getFCSettings();

            // Prepare data for JList
            listValues.addAll(pFcsSettings);

            // Save original values
            for (FCSetting fcs : pFcsSettings)
            {
                String id = fcs.getId();
                if (mapIdValues.get(id) != null)
                {
                    LOGGER.severe("Duplicate FCSetting Id=" + id);
                }
                mapIdValues.put(id, new FCvalues(fcs));
            }
        }
        list_fcSettings.setListData(listValues.toArray(new FCSetting[0]));
        if (!listValues.isEmpty())
        {
            list_fcSettings.setSelectedIndex(0);
        }
        btn_resetAll.setEnabled(!listValues.isEmpty());
    }

    void store()
    {
        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(EditorPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        // NbPreferences.forModule(EditorPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());

        // Nothing: changes are done on the fly
    }

    public void restoreOldValues()
    {
        for (FCSetting fcs : listValues)
        {
            FCvalues fcv = mapIdValues.get(fcs.getId());
            assert fcv != null : "fcs=" + fcs + " mapIdValues=" + mapIdValues;
            if (fcv.color != null)
            {
                fcs.setColor(fcv.color);
            }
            if (fcv.font != null)
            {
                fcs.setFont(fcv.font);
            }
        }
    }

    boolean valid()
    {
        // TODO check whether form is consistent and complete
        return true;
    }

    private class FCSettingCellRenderer extends DefaultListCellRenderer
    {

        @Override
        @SuppressWarnings("rawtypes")
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            JComponent jc = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            FCSetting fcs = (FCSetting) value;
            setText(fcs.getDisplayName());
            return jc;
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_font;
    private javax.swing.JButton btn_resetAll;
    private javax.swing.JButton btn_resetColor;
    private javax.swing.JButton btn_resetFont;
    private javax.swing.JComboBox cb_color;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lbl_category;
    private javax.swing.JLabel lbl_color;
    private javax.swing.JLabel lbl_font;
    private javax.swing.JList<FCSetting> list_fcSettings;
    private javax.swing.JTextField tf_font;
    // End of variables declaration//GEN-END:variables

    /**
     * Store a FCSetting value pair.
     */
    private class FCvalues
    {

        public Font font;
        public Color color;

        public FCvalues(FCSetting fcs)
        {
            font = fcs.getFont();
            color = fcs.getColor();
        }

        @Override
        public String toString()
        {
            return font.getName() + "-" + color;
        }
    }
}

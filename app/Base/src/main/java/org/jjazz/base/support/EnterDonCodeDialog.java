package org.jjazz.base.support;

import java.awt.event.ActionEvent;
import java.text.ParseException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import org.jjazz.uiutilities.api.UIUtilities;
import org.jjazz.utilities.api.ResUtil;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;


public class EnterDonCodeDialog extends javax.swing.JDialog
{

    public EnterDonCodeDialog()
    {
        super(WindowManager.getDefault().getMainWindow(), true);
        initComponents();
        setLocationRelativeTo(WindowManager.getDefault().getMainWindow());


        // Add a paste popup menu for each field
        String pasteText = ResUtil.getCommonString("Paste");
        var popupMenuCode = new JPopupMenu();
        popupMenuCode.add(new PopupPasteAction(pasteText, tf_code));
        tf_code.setComponentPopupMenu(popupMenuCode);
        var popupMenuEmail = new JPopupMenu();
        popupMenuEmail.add(new PopupPasteAction(pasteText, tf_email));
        tf_email.setComponentPopupMenu(popupMenuEmail);


        UIUtilities.installSelectAllWhenFocused(tf_code);
        UIUtilities.installSelectAllWhenFocused(tf_email);
        UIUtilities.installEnterKeyAction(this, () -> btn_okActionPerformed(null));
        UIUtilities.installEscapeKeyAction(this, () -> btn_cancelActionPerformed(null));

        String date = DonManager.getDefault().getRegisteredCodeExpirationDateAsString();
        if (date == null)
        {
            lbl_currentCode.setText(" ");
        } else
        {
            String txt = ResUtil.getString(getClass(), "CurrentCodeExpiration", date);
            lbl_currentCode.setText(txt);
        }
    }

    // =========================================================================================
    // Private methods
    // =========================================================================================

    private String getDonationCode()
    {
        return tf_code.getText().trim();
    }

    private String getEmail()
    {
        return tf_email.getText().trim();
    }


    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        tf_code = new javax.swing.JTextField();
        tf_email = new javax.swing.JTextField();
        btn_ok = new javax.swing.JButton();
        btn_cancel = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        lbl_currentCode = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(EnterDonCodeDialog.class, "EnterDonCodeDialog.title")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(EnterDonCodeDialog.class, "EnterDonCodeDialog.jLabel1.text")); // NOI18N

        tf_code.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                tf_codeActionPerformed(evt);
            }
        });

        tf_email.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                tf_emailActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_ok, org.openide.util.NbBundle.getMessage(EnterDonCodeDialog.class, "EnterDonCodeDialog.btn_ok.text")); // NOI18N
        btn_ok.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_okActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(btn_cancel, org.openide.util.NbBundle.getMessage(EnterDonCodeDialog.class, "EnterDonCodeDialog.btn_cancel.text")); // NOI18N
        btn_cancel.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_cancelActionPerformed(evt);
            }
        });

        jLabel4.setFont(jLabel4.getFont().deriveFont((jLabel4.getFont().getStyle() | java.awt.Font.ITALIC)));
        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(EnterDonCodeDialog.class, "EnterDonCodeDialog.jLabel4.text")); // NOI18N

        lbl_currentCode.setFont(lbl_currentCode.getFont().deriveFont((lbl_currentCode.getFont().getStyle() | java.awt.Font.ITALIC)));
        org.openide.awt.Mnemonics.setLocalizedText(lbl_currentCode, "You have registered a donation code which will expire on 12 march 2022."); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(EnterDonCodeDialog.class, "EnterDonCodeDialog.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(EnterDonCodeDialog.class, "EnterDonCodeDialog.jLabel5.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tf_code))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel5, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_currentCode, javax.swing.GroupLayout.Alignment.LEADING))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_ok)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btn_cancel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
                            .addComponent(tf_email))))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {btn_cancel, btn_ok});

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {jLabel3, jLabel5});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_currentCode)
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addGap(30, 30, 30)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tf_code, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addGap(28, 28, 28)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tf_email, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 52, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_ok)
                    .addComponent(btn_cancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_okActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_okActionPerformed
    {//GEN-HEADEREND:event_btn_okActionPerformed

        DonManager am = DonManager.getDefault();

        try
        {
            am.registerCode(getDonationCode(), getEmail());
        } catch (ParseException ex)
        {
            String msg = ex.getMessage();
            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }

        String msg = ResUtil.getString(getClass(), "DonationCodeInputSuccess", am.getRegisteredCodeExpirationDateAsString());
        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);

        setVisible(false);

    }//GEN-LAST:event_btn_okActionPerformed

    private void btn_cancelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_cancelActionPerformed
    {//GEN-HEADEREND:event_btn_cancelActionPerformed
        setVisible(false);
    }//GEN-LAST:event_btn_cancelActionPerformed

    private void tf_codeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_tf_codeActionPerformed
    {//GEN-HEADEREND:event_tf_codeActionPerformed
        tf_email.requestFocusInWindow();
    }//GEN-LAST:event_tf_codeActionPerformed

    private void tf_emailActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_tf_emailActionPerformed
    {//GEN-HEADEREND:event_tf_emailActionPerformed
        btn_okActionPerformed(null);
    }//GEN-LAST:event_tf_emailActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_cancel;
    private javax.swing.JButton btn_ok;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel lbl_currentCode;
    private javax.swing.JTextField tf_code;
    private javax.swing.JTextField tf_email;
    // End of variables declaration//GEN-END:variables


    private static class PopupPasteAction extends AbstractAction
    {

        private final JTextField tf;

        public PopupPasteAction(String name, JTextField tf)
        {
            super(name);
            this.tf = tf;
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            e.setSource(tf);
            TransferHandler.getPasteAction().actionPerformed(e);
        }

    }

}

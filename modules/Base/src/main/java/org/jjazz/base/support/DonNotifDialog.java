package org.jjazz.base.support;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.prefs.Preferences;
import javax.swing.ImageIcon;
import javax.swing.Timer;
import org.jjazz.uiutilities.api.UIUtilities;
import org.openide.awt.Actions;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;


public class DonNotifDialog extends javax.swing.JDialog
{

    public static final String PREF_IMAGE_INDEX = "ViewTree";
    private static final Preferences prefs = NbPreferences.forModule(DonNotifDialog.class);
    private int waitSeconds;
    private Timer timer;
    private final ImageIcon image;
    private final String closeText;

    /**
     * Creates new form DonateNotifDialog
     *
     * @param waitSeconds
     * @param modal
     */
    public DonNotifDialog(int waitSeconds, boolean modal)
    {
        super(WindowManager.getDefault().getMainWindow(), modal);
        checkArgument(waitSeconds >= 0);
        this.waitSeconds = waitSeconds;

        initComponents();
        image = getNextImage();
        lbl_image.setIcon(image);
        lbl_image.revalidate();
        pack();

        closeText = btn_close.getText();

        UIUtilities.installEnterKeyAction(this, () -> btn_donateLinkActionPerformed(null));
        UIUtilities.installEscapeKeyAction(this, () -> btn_closeActionPerformed(null));

        setLocationRelativeTo(WindowManager.getDefault().getMainWindow());
    }

    // ===========================================================================
    // Private methods
    // ===========================================================================

    private ImageIcon getNextImage()
    {
        int i = prefs.getInt(PREF_IMAGE_INDEX, -1);
        if (i >= 0 && i <= 9)
        {
            i = (i + 1) % 10;
            prefs.putInt(PREF_IMAGE_INDEX, i);
        }

        if (i < 0)
        {
            i = (int) Math.round(Math.random() * 9d);
            prefs.putInt(PREF_IMAGE_INDEX, i);
        }

        String filename = "Pic" + (i + 1) + ".jpg";
        return new ImageIcon(getClass().getResource("resources/" + filename));
    }


    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this
     * method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanel2 = new javax.swing.JPanel();
        btn_donateLink = new javax.swing.JButton();
        btn_close = new javax.swing.JButton();
        sp_longText = new javax.swing.JScrollPane();
        ta_longText = new javax.swing.JTextArea();
        pnl_uponDonationText = new javax.swing.JPanel();
        lbl_uponDonation = new javax.swing.JLabel();
        lbl_image = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getBundle(DonNotifDialog.class).getString("DonNotifDialog.title")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter()
        {
            public void windowClosing(java.awt.event.WindowEvent evt)
            {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt)
            {
                formWindowOpened(evt);
            }
        });

        btn_donateLink.setFont(btn_donateLink.getFont().deriveFont(btn_donateLink.getFont().getStyle() | java.awt.Font.BOLD, btn_donateLink.getFont().getSize()+1));
        org.openide.awt.Mnemonics.setLocalizedText(btn_donateLink, "<HTML><u>www.jjazzlab.com/en/donate</u></HTML>"); // NOI18N
        btn_donateLink.setToolTipText(org.openide.util.NbBundle.getMessage(DonNotifDialog.class, "DonNotifDialog.btn_donateLink.toolTipText")); // NOI18N
        btn_donateLink.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_donateLinkActionPerformed(evt);
            }
        });
        jPanel2.add(btn_donateLink);

        org.openide.awt.Mnemonics.setLocalizedText(btn_close, org.openide.util.NbBundle.getMessage(DonNotifDialog.class, "DonNotifDialog.btn_close.text")); // NOI18N
        btn_close.setEnabled(false);
        btn_close.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                btn_closeActionPerformed(evt);
            }
        });

        ta_longText.setEditable(false);
        ta_longText.setLineWrap(true);
        ta_longText.setRows(5);
        ta_longText.setText(org.openide.util.NbBundle.getMessage(DonNotifDialog.class, "DonNotifDialog.ta_longText.text")); // NOI18N
        ta_longText.setWrapStyleWord(true);
        sp_longText.setViewportView(ta_longText);

        lbl_uponDonation.setFont(lbl_uponDonation.getFont().deriveFont(lbl_uponDonation.getFont().getStyle() | java.awt.Font.BOLD, lbl_uponDonation.getFont().getSize()+1));
        lbl_uponDonation.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(lbl_uponDonation, org.openide.util.NbBundle.getMessage(DonNotifDialog.class, "DonNotifDialog.lbl_uponDonation.text")); // NOI18N
        pnl_uponDonationText.add(lbl_uponDonation);

        lbl_image.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lbl_image.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/jjazz/base/support/resources/Pic1.jpg"))); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnl_uponDonationText, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sp_longText)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(lbl_image, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(btn_close, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_image)
                .addGap(12, 12, 12)
                .addComponent(sp_longText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(24, 24, 24)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnl_uponDonationText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, Short.MAX_VALUE)
                .addComponent(btn_close)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowOpened
    {//GEN-HEADEREND:event_formWindowOpened
        if (waitSeconds > 0)
        {
            timer = new Timer(1000, e -> timerElapsed());
            timer.start();
            btn_close.setText(closeText + "(" + String.valueOf(waitSeconds) + ")");
        } else
        {
            btn_close.setEnabled(true);
        }
    }//GEN-LAST:event_formWindowOpened

    private void btn_donateLinkActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_donateLinkActionPerformed
    {//GEN-HEADEREND:event_btn_donateLinkActionPerformed
        var a = Actions.forID("Help", "org.jjazz.base.actions.Donate");
        a.actionPerformed(null);
    }//GEN-LAST:event_btn_donateLinkActionPerformed

    private void btn_closeActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btn_closeActionPerformed
    {//GEN-HEADEREND:event_btn_closeActionPerformed
        if (waitSeconds <= 0)
        {
            setVisible(false);
            dispose();
        }
    }//GEN-LAST:event_btn_closeActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt)//GEN-FIRST:event_formWindowClosing
    {//GEN-HEADEREND:event_formWindowClosing
        // User clicked on the upper right arrow 
        btn_closeActionPerformed(null);
    }//GEN-LAST:event_formWindowClosing

    private void timerElapsed()
    {
        if (waitSeconds > 0)
        {
            waitSeconds--;
            btn_close.setText(closeText + "(" + String.valueOf(waitSeconds) + ")");
            if (waitSeconds == 0)
            {
                timer.stop();
                btn_close.setEnabled(true);
                btn_close.setText(closeText);
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_close;
    private javax.swing.JButton btn_donateLink;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel lbl_image;
    private javax.swing.JLabel lbl_uponDonation;
    private javax.swing.JPanel pnl_uponDonationText;
    private javax.swing.JScrollPane sp_longText;
    private javax.swing.JTextArea ta_longText;
    // End of variables declaration//GEN-END:variables


}
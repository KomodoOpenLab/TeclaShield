/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.akdroid.interfaces;

import com.akdroid.tecladesk.BluetoothClient;
import com.akdroid.tecladesk.PreferencesHandler;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * This is the main window of the TeclaDesktop
 * @author Akhil
 */
public class ClientMain extends javax.swing.JFrame {

    /**
     * Creates new form ClientMain
     */
    public static final String TECLA_ICON_PATH="tekla_icon.png";
    ButtonPref b1,b2,b3,b4,b5,b6;
    BluetoothClient bcl;
    SystemTray systray;
    TrayIcon icon;
    boolean systray_available;
    public ClientMain(PreferencesHandler pref,BluetoothClient bcl_) {
        initComponents();
        //initialize buttonpref panels
        b1=new ButtonPref(pref.getShieldButton(ShieldEvent.ECU1),pref);
        b2=new ButtonPref(pref.getShieldButton(ShieldEvent.ECU2),pref);
        b3=new ButtonPref(pref.getShieldButton(ShieldEvent.ECU3),pref);
        b4=new ButtonPref(pref.getShieldButton(ShieldEvent.ECU4),pref);
        b5=new ButtonPref(pref.getShieldButton(ShieldEvent.E1),pref);
        b6=new ButtonPref(pref.getShieldButton(ShieldEvent.E2),pref);
        //Add button tabs to the panel
        preftab.addTab("ECU UP",b1);
        preftab.addTab("ECU DOWN",b2);
        preftab.addTab("ECU LEFT",b3);
        preftab.addTab("ECU RIGHT",b4);
        preftab.addTab("SWITCH 1",b5);
        preftab.addTab("SWITCH 2",b6);
        bcl=bcl_;
        //Set Application icon in the title bar
        setIconImage(Toolkit.getDefaultToolkit().createImage(TECLA_ICON_PATH));
        //Get the System Tray of the system.
        systray=SystemTray.getSystemTray();
        systray_available=systray.isSupported();
        
        if(systray_available){
            Image icon_img = Toolkit.getDefaultToolkit().createImage(TECLA_ICON_PATH);
            icon=new TrayIcon(icon_img,"TeclaDesktop");
            icon.setImageAutoSize(true);
            icon.addMouseListener(new MouseListener(){

                @Override
                public void mouseClicked(MouseEvent me) {
                    
                    
                    java.awt.EventQueue.invokeLater(new Runnable()
                    {

                        @Override
                        public void run() {
                            setVisible(true);
                            setExtendedState(JFrame.NORMAL);
                            toFront();
                            repaint();
                        }
                    }
                    );
                    systray.remove(icon);
                }

                @Override
                public void mousePressed(MouseEvent me) {
                    
                }

                @Override
                public void mouseReleased(MouseEvent me) {
                    
                }

                @Override
                public void mouseEntered(MouseEvent me) {
                    
                }

                @Override
                public void mouseExited(MouseEvent me) {
                    
                }
            });
        }
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        preftab = new javax.swing.JTabbedPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("TeclaClient");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowIconified(java.awt.event.WindowEvent evt) {
                formWindowIconified(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(preftab, javax.swing.GroupLayout.PREFERRED_SIZE, 777, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(117, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(88, 88, 88)
                .addComponent(preftab, javax.swing.GroupLayout.PREFERRED_SIZE, 414, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(158, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
                // TODO add your handling code here:
                bcl.close();
        
    }//GEN-LAST:event_formWindowClosing

    private void formWindowIconified(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowIconified
        // TODO add your handling code here:
        if(systray_available)
            try {
            systray.add(icon);
            setVisible(false);
        } catch (AWTException ex) {
            System.out.println(ex.getMessage());
        }
    }//GEN-LAST:event_formWindowIconified

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ClientMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ClientMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ClientMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ClientMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane preftab;
    // End of variables declaration//GEN-END:variables
}

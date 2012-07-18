/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.komodo.desktop.interfaces;

import com.komodo.desktop.client.GlobalVar;
import com.komodo.desktop.client.PreferencesHandler;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


/**
 *
 * Configuration selector to provide for and choose between
 * multiple configurations
 * @author ankitdaf
 */
 public class ConfigSel extends javax.swing.JPanel {

    PreferencesHandler pref;
    ClientMain clientmain;
    ActionListener al;
    
    /**
     * Creates new form ConfigSel
     */
    public ConfigSel(ClientMain clientmain_,PreferencesHandler pref_) {
        initComponents();
        pref=pref_;
        show_all_configs();
        clientmain=clientmain_;
        al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                Object obj = jComboBox1.getSelectedItem();
                File selected_config = (File) obj;
                save_current_config();
                set_selected_config(selected_config);
            }
        };
            
        jComboBox1.addActionListener(al);
        
    }
    
    public void show_all_configs()
    {
        jComboBox1.removeAllItems();
        File[] available_configs = pref.get_available_configs();
        
        for (int i=0;i<available_configs.length;i++)
        {
            jComboBox1.addItem(available_configs[i]);
        }
        jComboBox1.setSelectedItem(pref.get_current_config());
        
    }
    
    public void save_current_config()
    {
        if(pref.get_current_config() != null)
        pref.save_config(pref.get_current_config());
    }
    
    public void set_selected_config(File selected_config)
    {
        pref.set_selected_config(selected_config);
        clientmain.refresh_display(pref);
        
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jComboBox1 = new javax.swing.JComboBox();
        save_as_button = new javax.swing.JButton();
        delete_button = new javax.swing.JButton();

        save_as_button.setText("Save As..");
        save_as_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_as_buttonActionPerformed(evt);
            }
        });

        delete_button.setText("Delete");
        delete_button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                delete_buttonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 285, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(save_as_button)
                        .addGap(62, 62, 62)
                        .addComponent(delete_button)))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(save_as_button)
                    .addComponent(delete_button))
                .addContainerGap(32, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

     private void save_as_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_save_as_buttonActionPerformed
         // TODO add your handling code here:
         NewFileDialog nfdialog=new NewFileDialog(GlobalVar.client_window_global,true,pref,this);
         nfdialog.setVisible(true);
     }//GEN-LAST:event_save_as_buttonActionPerformed

     private void delete_buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_delete_buttonActionPerformed
         // TODO add your handling code here:
         jComboBox1.removeActionListener(al);
         File to_delete = pref.get_current_config();
         to_delete.delete();
         jComboBox1.removeAllItems();
         File [] available_configs = pref.get_available_configs();
         set_selected_config(available_configs[0]);
        
        for (int i=0;i<available_configs.length;i++)
        {
            jComboBox1.addItem(available_configs[i]);
        }
        jComboBox1.setSelectedItem(pref.get_current_config());
        jComboBox1.addActionListener(al);
     }//GEN-LAST:event_delete_buttonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton delete_button;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JButton save_as_button;
    // End of variables declaration//GEN-END:variables
    
    
    public void RefreshList(){
        /*
         * Refreshes the list to include any newly added file.
         */
        jComboBox1.removeActionListener(al);
        jComboBox1.removeAllItems();
        File[] list =pref.get_available_configs();
        for(int x=0;x<list.length;x++){
            jComboBox1.addItem(list[x]);
        }
        jComboBox1.addActionListener(al);
    }
}

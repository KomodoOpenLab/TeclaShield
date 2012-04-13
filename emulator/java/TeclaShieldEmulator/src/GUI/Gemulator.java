/*
 * Gemulator.java
 *
 * Created on Apr 12, 2012, 2:16:57 PM
 */
package GUI;

import GUI.Util.Toast;
import GUI.Help.HelpFrame;
import Emulator.Emulator;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.ImageIcon;

/**
 *
 * @author Rishabh
 */
public class Gemulator extends javax.swing.JFrame implements WindowListener{

    private ImageIcon red_icon,green_icon;
    private boolean connection_status_bool = false;
    private Emulator emulator;
    private UpdateStatus updateStatus;
    private String title = "Tecla Shield Emulator";
    private static final String no_device_connected = "You are currently not connected to any device";
    private static final String device_got_disconnected = "You got disconnected from the device";
    private static final String device_got_connected = "A device was successfully connected";
    private static final String failed_connecting = "An attempt to connect to a device failed";
    private AttemptConnection attemptConnection;
    
    /** Creates new form Gemulator */
    public Gemulator() {
        red_icon = new ImageIcon(getClass().getResource("/Resources/red_orb.png"));
        green_icon = new ImageIcon(getClass().getResource("/Resources/green_orb.png"));
        initComponents();
        setTitle(title);
        emulator = new Emulator();
        addWindowListener(this);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

            private KeyEvent prevKeyEvent = null;
            
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if(!isFocused() || 
                        (e.getID() == KeyEvent.KEY_TYPED) || 
                        (prevKeyEvent != null && e.getID() == prevKeyEvent.getID() && e.getKeyCode() == prevKeyEvent.getKeyCode())){
                    prevKeyEvent = e;
                    return false;
                }
                prevKeyEvent = e;
                if(e.getID()== KeyEvent.KEY_PRESSED ){
                    int code = e.getKeyCode();
                    switch(code){
                        case KeyEvent.VK_UP:
                            ecu1MousePressed(null);
                            break;
                        case KeyEvent.VK_DOWN:
                            ecu2MousePressed(null);
                            break;
                        case KeyEvent.VK_LEFT:
                            ecu3MousePressed(null);
                            break;
                        case KeyEvent.VK_RIGHT:
                            ecu4MousePressed(null);
                            break;
                        case KeyEvent.VK_1:
                            extra1MousePressed(null);
                            break;
                        case KeyEvent.VK_NUMPAD1:
                            extra1MousePressed(null);
                            break;
                        case KeyEvent.VK_2:
                            extra2MousePressed(null);
                            break;
                        case KeyEvent.VK_NUMPAD2:
                            extra2MousePressed(null);
                            break;
                    }
                }
                else if(e.getID() == KeyEvent.KEY_RELEASED){
                    int code = e.getKeyCode();
                    switch(code){
                        case KeyEvent.VK_UP:
                            ecu1MouseReleased(null);
                            break;
                        case KeyEvent.VK_DOWN:
                            ecu2MouseReleased(null);
                            break;
                        case KeyEvent.VK_LEFT:
                            ecu3MouseReleased(null);
                            break;
                        case KeyEvent.VK_RIGHT:
                            ecu4MouseReleased(null);
                            break;
                        case KeyEvent.VK_1:
                            extra1MouseReleased(null);
                            break;
                        case KeyEvent.VK_2:
                            extra2MouseReleased(null);
                            break;
                        case KeyEvent.VK_NUMPAD1:
                            extra1MouseReleased(null);
                            break;
                        case KeyEvent.VK_NUMPAD2:
                            extra2MouseReleased(null);
                            break;
                        case KeyEvent.VK_H:
                            new HelpFrame().setVisible(true);
                            break;
                        case KeyEvent.VK_ESCAPE:
                        case KeyEvent.VK_Q:
                            emulator.disconnect();
                            System.exit(0);
                    }
                }
                //e.consume();
                return false;
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        statusbar = new javax.swing.JPanel();
        status_action = new javax.swing.JLabel();
        connection_status_panel = new javax.swing.JPanel();
        connection_status = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        cancel_connection_panel = new javax.swing.JPanel();
        cancel_connection = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        top_panel = new javax.swing.JPanel();
        extra_switch_panel = new javax.swing.JPanel();
        /*
        extra1 = new javax.swing.JButton();
        */extra1 = new CustomButton();
        /*
        extra2 = new javax.swing.JButton();
        */extra2 = new CustomButton();
        ecuPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        /*
        ecu1 = new javax.swing.JButton();
        */ecu1 = new CustomButton();
        jPanel2 = new javax.swing.JPanel();
        /*
        ecu3 = new javax.swing.JButton();
        */ecu3 = new CustomButton();
        jPanel3 = new javax.swing.JPanel();
        /*
        ecu4 = new javax.swing.JButton();
        */ecu4 = new CustomButton();
        jPanel4 = new javax.swing.JPanel();
        /*
        ecu2 = new javax.swing.JButton();
        */ecu2 = new CustomButton();
        jPanel5 = new javax.swing.JPanel();
        connectpanel = new javax.swing.JPanel();
        toggle_connect = new javax.swing.JToggleButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        statusbar.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        status_action.setText("Current Action (Press h for help)");

        connection_status.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        connection_status.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Resources/red_orb.png"))); // NOI18N
        connection_status.setText("Connection Status");

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout connection_status_panelLayout = new javax.swing.GroupLayout(connection_status_panel);
        connection_status_panel.setLayout(connection_status_panelLayout);
        connection_status_panelLayout.setHorizontalGroup(
            connection_status_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connection_status_panelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 2, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connection_status)
                .addContainerGap())
        );
        connection_status_panelLayout.setVerticalGroup(
            connection_status_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(connection_status, javax.swing.GroupLayout.DEFAULT_SIZE, 32, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connection_status_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 10, Short.MAX_VALUE)
                .addContainerGap())
        );

        cancel_connection.setText("Cancel Connection");
        cancel_connection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel_connectionActionPerformed(evt);
            }
        });

        jProgressBar1.setIndeterminate(true);

        javax.swing.GroupLayout cancel_connection_panelLayout = new javax.swing.GroupLayout(cancel_connection_panel);
        cancel_connection_panel.setLayout(cancel_connection_panelLayout);
        cancel_connection_panelLayout.setHorizontalGroup(
            cancel_connection_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, cancel_connection_panelLayout.createSequentialGroup()
                .addComponent(jProgressBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 166, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancel_connection))
        );
        cancel_connection_panelLayout.setVerticalGroup(
            cancel_connection_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cancel_connection, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
            .addGroup(cancel_connection_panelLayout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jProgressBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout statusbarLayout = new javax.swing.GroupLayout(statusbar);
        statusbar.setLayout(statusbarLayout);
        statusbarLayout.setHorizontalGroup(
            statusbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusbarLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(status_action)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addComponent(cancel_connection_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connection_status_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        statusbarLayout.setVerticalGroup(
            statusbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusbarLayout.createSequentialGroup()
                .addGroup(statusbarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cancel_connection_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(status_action, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 36, Short.MAX_VALUE)
                    .addComponent(connection_status_panel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );

        cancel_connection_panel.setVisible(false);

        extra1.setText("Extra Switch 1");
        extra1.setToolTipText("numeric or numpad 1 key");
        extra1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                extra1MouseReleased(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                extra1MousePressed(evt);
            }
        });
        extra1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                extra1ActionPerformed(evt);
            }
        });

        extra2.setText("Extra Switch 2");
        extra2.setToolTipText("numeric or numpad 1");
        extra2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                extra2MouseReleased(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                extra2MousePressed(evt);
            }
        });

        javax.swing.GroupLayout extra_switch_panelLayout = new javax.swing.GroupLayout(extra_switch_panel);
        extra_switch_panel.setLayout(extra_switch_panelLayout);
        extra_switch_panelLayout.setHorizontalGroup(
            extra_switch_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(extra_switch_panelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(extra_switch_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(extra1, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .addComponent(extra2, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                .addGap(0, 0, 0))
        );
        extra_switch_panelLayout.setVerticalGroup(
            extra_switch_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, extra_switch_panelLayout.createSequentialGroup()
                .addComponent(extra1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(extra2, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        extra_switch_panelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {extra1, extra2});

        ecuPanel.setLayout(new java.awt.GridLayout(3, 3, 10, 10));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 155, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 94, Short.MAX_VALUE)
        );

        ecuPanel.add(jPanel1);

        ecu1.setText("Up");
        ecu1.setToolTipText("up arrow key");
        ecu1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ecu1MouseReleased(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                ecu1MousePressed(evt);
            }
        });
        ecuPanel.add(ecu1);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 155, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 94, Short.MAX_VALUE)
        );

        ecuPanel.add(jPanel2);

        ecu3.setText("Left");
        ecu3.setToolTipText("left arrow key");
        ecu3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ecu3MouseReleased(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                ecu3MousePressed(evt);
            }
        });
        ecuPanel.add(ecu3);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 155, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 94, Short.MAX_VALUE)
        );

        ecuPanel.add(jPanel3);

        ecu4.setText("right arrow key");
        ecu4.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ecu4MouseReleased(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                ecu4MousePressed(evt);
            }
        });
        ecuPanel.add(ecu4);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 155, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 94, Short.MAX_VALUE)
        );

        ecuPanel.add(jPanel4);

        ecu2.setText("Down");
        ecu2.setToolTipText("down arrow key");
        ecu2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                ecu2MouseReleased(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                ecu2MousePressed(evt);
            }
        });
        ecu2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ecu2ActionPerformed(evt);
            }
        });
        ecuPanel.add(ecu2);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 155, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 94, Short.MAX_VALUE)
        );

        ecuPanel.add(jPanel5);

        toggle_connect.setText("Connect");
        toggle_connect.setToolTipText("no shortcut key");
        toggle_connect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                toggle_connectMouseClicked(evt);
            }
        });
        toggle_connect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggle_connectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout connectpanelLayout = new javax.swing.GroupLayout(connectpanel);
        connectpanel.setLayout(connectpanelLayout);
        connectpanelLayout.setHorizontalGroup(
            connectpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toggle_connect, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
        );
        connectpanelLayout.setVerticalGroup(
            connectpanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toggle_connect, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout top_panelLayout = new javax.swing.GroupLayout(top_panel);
        top_panel.setLayout(top_panelLayout);
        top_panelLayout.setHorizontalGroup(
            top_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(top_panelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ecuPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 485, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(top_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(extra_switch_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(connectpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        top_panelLayout.setVerticalGroup(
            top_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(top_panelLayout.createSequentialGroup()
                .addGroup(top_panelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(top_panelLayout.createSequentialGroup()
                        .addGap(0, 0, 0)
                        .addComponent(extra_switch_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectpanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(ecuPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusbar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(top_panel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(top_panel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void ecu2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ecu2ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_ecu2ActionPerformed

private void ecu1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ecu1MousePressed
        if(connection_status_bool){
            setStatus("Sending ECU1 pressed switch event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.UP);
            if(!sendKeyEvent){
                notOnline(true);
            }
        }
        else{
            new Toast(no_device_connected).setVisible(true);
        }
}//GEN-LAST:event_ecu1MousePressed

private void extra1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extra1ActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_extra1ActionPerformed

private void ecu1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ecu1MouseReleased
        if(connection_status_bool){
            setStatus("Sending switch released event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.RELEASE);
            if(!sendKeyEvent)
                notOnline(true);
        }
}//GEN-LAST:event_ecu1MouseReleased

private void ecu2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ecu2MousePressed
        if(connection_status_bool){
            setStatus("Sending ECU2 pressed switch event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.DOWN);
            if(!sendKeyEvent)
                notOnline(true);
        }
        else{
            new Toast(no_device_connected).setVisible(true);
        }
}//GEN-LAST:event_ecu2MousePressed

private void ecu2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ecu2MouseReleased
        if(connection_status_bool){
            setStatus("Sending switch released event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.RELEASE);
            if(!sendKeyEvent)
                notOnline(true);
        }
}//GEN-LAST:event_ecu2MouseReleased

private void ecu3MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ecu3MousePressed
        if(connection_status_bool){
            setStatus("Sending ECU3 pressed switch event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.LEFT);
            if(!sendKeyEvent)
                notOnline(true);
        }
        else{
            new Toast(no_device_connected).setVisible(true);
        }
}//GEN-LAST:event_ecu3MousePressed

private void ecu3MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ecu3MouseReleased
        if(connection_status_bool){
            setStatus("Sending switch released event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.RELEASE);
            if(!sendKeyEvent)
                notOnline(true);
        }
}//GEN-LAST:event_ecu3MouseReleased

private void ecu4MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ecu4MousePressed
        if(connection_status_bool){
            setStatus("Sending ECU4 pressed switch event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.RIGHT);
            if(!sendKeyEvent)
                notOnline(true);
        }
        else{
            new Toast(no_device_connected).setVisible(true);
        }
}//GEN-LAST:event_ecu4MousePressed

private void ecu4MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_ecu4MouseReleased
        if(connection_status_bool){
            setStatus("Sending switch released event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.RELEASE);
            if(!sendKeyEvent)
                notOnline(true);
        }
}//GEN-LAST:event_ecu4MouseReleased

private void extra1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extra1MousePressed
        if(connection_status_bool){
            setStatus("Sending extra switch 1 pressed switch event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.EXTRA_1);
            if(!sendKeyEvent)
                notOnline(true);
        }
        else{
            new Toast(no_device_connected).setVisible(true);
        }
}//GEN-LAST:event_extra1MousePressed

private void extra1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extra1MouseReleased
        if(connection_status_bool){
            setStatus("Sending switch released event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.RELEASE);
            if(!sendKeyEvent)
                notOnline(true);
        }
}//GEN-LAST:event_extra1MouseReleased

private void extra2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extra2MousePressed
        if(connection_status_bool){
            setStatus("Sending extra switch 2 pressed switch event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.EXTRA_2);
            if(!sendKeyEvent)
                notOnline(true);
        }
        else{
            new Toast(no_device_connected).setVisible(true);
        }
}//GEN-LAST:event_extra2MousePressed

private void extra2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_extra2MouseReleased
        if(connection_status_bool){
            setStatus("Sending switch released event");
            boolean sendKeyEvent = emulator.sendKeyEvent(Emulator.RELEASE);
            if(!sendKeyEvent)
                notOnline(true);
        }
}//GEN-LAST:event_extra2MouseReleased

private void setStatus(String status){
    setStatus(status, false);
}

private void setStatus(String status, boolean permanent){
    if(updateStatus != null && updateStatus.isAlive()){
        try{
            updateStatus.stop();
        }catch(Exception e){}
    }
    status_action.setText(status);
    if(!permanent){
        try{
            updateStatus = new UpdateStatus();
            updateStatus.start();
        }catch(Exception exp){}
    }
}

class UpdateStatus extends Thread{
  @Override
  public void run(){
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {}
      status_action.setText("");
  }
}

class AttemptConnection extends Thread{
    @Override
    public void run(){
        boolean connect = emulator.connect();
        if(connect){
            nowOnline(true);
        }
        else{
            new Toast(failed_connecting).setVisible(true);
            notOnline(false);
        }
    }
}

private void toggle_connectMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_toggle_connectMouseClicked
    if(toggle_connect.isSelected()){
        setStatus("Waiting for a device to connect...", true);
        toggle_connect.setText("Disconnect");
        top_panel.setVisible(false);
        connection_status_panel.setVisible(false);
        cancel_connection_panel.setVisible(true);
        if(attemptConnection != null && attemptConnection.isAlive())
            attemptConnection.stop();
        attemptConnection = new AttemptConnection();
        attemptConnection.start();
    }
    else{
        toggle_connect.setText("Connect");
        emulator.disconnect();
        notOnline(true);
    }
}//GEN-LAST:event_toggle_connectMouseClicked

private void toggle_connectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggle_connectActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_toggle_connectActionPerformed

private void cancel_connectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel_connectionActionPerformed
    if(attemptConnection != null && attemptConnection.isAlive()){
        try{
            attemptConnection.stop();
            emulator.disconnect();
        }catch(Exception e){}
    }
    System.out.println("Cancelled the connection");
    notOnline(false);
}//GEN-LAST:event_cancel_connectionActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Gemulator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Gemulator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Gemulator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Gemulator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        if(args.length==0){
            new Emulator().startEmulator();
        }
        else if(args[0] != null && args[0].equals("-gui")){
            java.awt.EventQueue.invokeLater(new Runnable() {

                public void run() {
                    new Gemulator().setVisible(true);
                }
            });
        }
        else{
            System.out.println("Incorrect arguments");
            System.out.println("Either pass no arguments or pass -gui if you wish to access the gui version of the emulator");
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancel_connection;
    private javax.swing.JPanel cancel_connection_panel;
    private javax.swing.JLabel connection_status;
    private javax.swing.JPanel connection_status_panel;
    private javax.swing.JPanel connectpanel;
    /*
    private javax.swing.JButton ecu1;
    */CustomButton ecu1;
    /*
    private javax.swing.JButton ecu2;
    */CustomButton ecu2;
    /*
    private javax.swing.JButton ecu3;
    */CustomButton ecu3;
    /*
    private javax.swing.JButton ecu4;
    */CustomButton ecu4;
    private javax.swing.JPanel ecuPanel;
    /*
    private javax.swing.JButton extra1;
    */CustomButton extra1;
    /*
    private javax.swing.JButton extra2;
    */CustomButton extra2;
    private javax.swing.JPanel extra_switch_panel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel status_action;
    private javax.swing.JPanel statusbar;
    private javax.swing.JToggleButton toggle_connect;
    private javax.swing.JPanel top_panel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
        emulator.disconnect();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    private void notOnline(boolean show_toast) {
        setStatus("Disconnected...");
        top_panel.setVisible(true);
        cancel_connection_panel.setVisible(false);
        connection_status_panel.setVisible(true);
        toggle_connect.setSelected(false);
        toggle_connect.setText("Connect");
        connection_status_bool = false;
        connection_status.setText("Disconnected");
        connection_status.setIcon(red_icon);
        if(show_toast)
            new Toast(device_got_disconnected).setVisible(true);
    }
    
    private void nowOnline(boolean show_toast) {
        setStatus("Connected...");
        top_panel.setVisible(true);
        cancel_connection_panel.setVisible(false);
        connection_status_panel.setVisible(true);
        toggle_connect.setSelected(true);
        toggle_connect.setText("Disconnect");
        connection_status_bool = true;
        connection_status.setText("Connected");
        connection_status.setIcon(green_icon);
        if(show_toast)
            new Toast(device_got_connected).setVisible(true);
    }
}

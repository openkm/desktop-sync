/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.view;

import java.awt.Component;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author abujosa
 */
public class Communication {
    
    public static final int CONFIRM_DIALOG_OK       = JOptionPane.OK_OPTION;
    public static final int CONFIRM_DIALOG_CANCEL   = JOptionPane.CANCEL_OPTION;
    
    // Time Out dialog
    private static JOptionPane option = new JOptionPane("", JOptionPane.INFORMATION_MESSAGE); 
    private static JDialog dialogo = null;
    
    // Show Message error
    public static void showError(Component cmpnt, String errMessage) {
        JOptionPane.showMessageDialog(cmpnt, errMessage, "OpenKM Synchronize error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Show Message error
    public static void showMessage(Component cmpnt, String smg) {
        JOptionPane.showMessageDialog(cmpnt, smg, "OpenKM Synchronize info", JOptionPane.INFORMATION_MESSAGE);
    }    
    
    // Show Confirm dialog
    public static int showConfirm(Component cmpnt, String text) {
        return JOptionPane.showConfirmDialog(cmpnt, text, "OpenKM Synchronize", JOptionPane.OK_CANCEL_OPTION);
    }
    
    // Show Message timeout dialog
    public static void showTimeOutMessage(Component cmpnt, String message, String title, final long timeout) {
        option.setMessage(message);
        option.setOptionType(CONFIRM_DIALOG_OK);
        if (null == dialogo) {
            dialogo = option.createDialog(cmpnt, title);
        } else {
            dialogo.setTitle(title);
        }

        Thread hilo = new Thread() {
            public void run() {
                try {
                    Thread.sleep(timeout);
                    if (dialogo.isVisible()) {
                        dialogo.setVisible(false);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        hilo.start();
        dialogo.setVisible(true);
    }
    
}

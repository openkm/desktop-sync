package com.openkm.okmsynchronize;

import com.openkm.okmsynchronize.controller.SynchronizeTrayIconController;
import com.openkm.okmsynchronize.model.SynchronizeDesktopModel;
import com.openkm.okmsynchronize.view.SynchronizeTrayIcon;
import com.openkm.okmsynchronize.view.utils.ImageList;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.OkmSynchronize
 */
public class OkmSynchronize {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SynchronizeDesktopModel model = new SynchronizeDesktopModel();
        SynchronizeTrayIcon view = new SynchronizeTrayIcon(ImageList.getImage("com.openkm.odesktop.Ologo"));
        SynchronizeTrayIconController controller = new SynchronizeTrayIconController(view, model);
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {               
//                if ("Metal".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
        } catch (ClassNotFoundException ex) {
            
        } catch (InstantiationException ex) {
            
        } catch (IllegalAccessException ex) {
            
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            
        }
    }
}


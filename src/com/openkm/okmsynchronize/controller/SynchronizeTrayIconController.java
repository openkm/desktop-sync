/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.controller;

import com.openkm.okmsynchronize.model.SynchronizeDesktopModel;
import com.openkm.okmsynchronize.view.SynchronizeAboutView;
import com.openkm.okmsynchronize.view.SynchronizeDesktopView;
import com.openkm.okmsynchronize.view.SynchronizeTrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;

/**
 *
 * @author abujosa
 */
public class SynchronizeTrayIconController {
    
    private SynchronizeTrayIcon tray;
    private SynchronizeDesktopModel model;

    public SynchronizeTrayIconController(SynchronizeTrayIcon tray, SynchronizeDesktopModel model) {
        this.tray = tray;
        this.model = model;
        
        tray.addtrayMenuItemsListener(new ExitTrayIcon());
        tray.addTrayIconMouseListener(new ClickTrayIcon());
    }
    
    
    // Cancel configure Application options
    class ExitTrayIcon implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) { 
            JMenuItem action = (JMenuItem) e.getSource();
            
            if ("Exit".equals(action.getText())) {
                try {
                    model.stopingApplication();
                    System.exit(0);
                } catch (IOException ex) {
                    System.exit(0);
                }
            } else if ("Show desktop".equals(action.getText())) {   
                if (!model.isWindowVisible(SynchronizeDesktopModel.DESKOPT_WINDOW)) {
                    model.setWindowVisible(SynchronizeDesktopModel.DESKOPT_WINDOW, Boolean.TRUE);
                    SynchronizeDesktopView view = new SynchronizeDesktopView(model);        
                    SynchronizeDesktopController controller = new SynchronizeDesktopController(model, view);                     
                    view.setVisible(true);                    
                }
             } else if ("About".equals(action.getText())) {                
                SynchronizeAboutView view = new SynchronizeAboutView(null, false);
                SynchronizeAboutController controller = new SynchronizeAboutController(model.getConfiguration(), view);
                view.setLocationRelativeTo(null);
                view.setVisible(true);
            }
        }        
    }
    
    class ClickTrayIcon implements MouseListener {
        
        @Override
        public void mousePressed(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            tray.showTrayIconMenu(e.getX(), e.getY());
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseExited(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
}

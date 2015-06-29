/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.controller;

import com.openkm.okmsynchronize.model.ConfigurationModel;
import com.openkm.okmsynchronize.view.SynchronizeAboutView;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author abujosa
 */
public class SynchronizeAboutController {
    
    private SynchronizeAboutView view;
    private ConfigurationModel model;
    
    public SynchronizeAboutController(ConfigurationModel model, SynchronizeAboutView view) {
        
        this.model = model;
        this.view = view;
        
        view.addButtonCloseListener(new CloseAbout());
        view.addButtonHelpListener(new HelpApout());
        view.addButtonChangelogListener(new ChangeLog());
    }
    
    /**
     * Button events controller
     */
    
    // Cancel configure Application options
    class CloseAbout implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {            
            view.setVisible(false);
        }        
    }
    
    // View change log application
    class ChangeLog implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {            
            try {
                //view.viewChangeLog();
                File f = new File( "/home/abujosa/Escritorio/RestVsWebServices.pdf");
                Desktop.getDesktop().open(f);
            } catch (IOException ex) {
                Logger.getLogger(SynchronizeAboutController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }        
    }
    
    // Accept configure Application options
    class HelpApout implements MouseListener {

        @Override
        public void mouseClicked(MouseEvent me) {
           
        }

        @Override
        public void mousePressed(MouseEvent me) {
            try {
                String navigator = model.getKeyValue(ConfigurationModel.KEY_NAVIGATOR);
                String host = "http://opekm.com";
                Process p = Runtime.getRuntime().exec(navigator + " " + host);
            } catch (IOException ex) {

            }
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
    }

}

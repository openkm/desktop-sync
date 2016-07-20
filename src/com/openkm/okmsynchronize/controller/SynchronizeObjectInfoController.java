/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.controller;

import com.openkm.okmsynchronize.model.ConfigurationModel;
import com.openkm.okmsynchronize.model.SynchronizedObject;
import com.openkm.okmsynchronize.view.SynchronizeAboutView;
import com.openkm.okmsynchronize.view.SynchronizedObjectInfoView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;


/**
 *
 * @author abujosa
 */
public class SynchronizeObjectInfoController {
    
    private SynchronizedObjectInfoView view;
    private SynchronizedObject model;
    
    public SynchronizeObjectInfoController(SynchronizedObject model, SynchronizedObjectInfoView view) {
        
        this.model = model;
        this.view = view;
        
        view.addButtonCloseListener(new CloseInfo());
    }
    
    /**
     * Button events controller
     */
    
    // Cancel configure Application options
    class CloseInfo implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {            
            view.setVisible(false);
        }        
    }       

}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.controller;

import com.openkm.okmsynchronize.Constants;
import com.openkm.okmsynchronize.model.ConfigurationModel;
import com.openkm.okmsynchronize.model.SynchronizeError;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.view.SynchronizeLogView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author abujosa
 */
public class SynchronizeLogController {
    
    private SynchronizeLogView view;
    private ConfigurationModel configuration;

    public SynchronizeLogController(ConfigurationModel configuration, SynchronizeLogView view) {
        this.view = view;
        this.configuration = configuration;
        view.addButtonCloseActionListener(new ButtonClose());
        view.addButtonRefreshActionListener(new ButtonRefresh());
    }
    
    class ButtonClose implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
           view.setVisible(false);
           view.dispose();
        }        
    }
    
    class ButtonRefresh implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ae) {
            try {
                String logPath = Utils.buildLocalFilePath(configuration.getKeyValue(ConfigurationModel.KEY_WORK_DIRECTORY),Constants.WORK_DIRECTORY_NAME, Constants.LOG_FOLDER_NAME, Constants.LOG_FILE_NAME);
                view.refreshViewLog(logPath);
            } catch (SynchronizeError ex) {
                
            }
        }
        
    }
    
    
    
}

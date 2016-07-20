package com.openkm.okmsynchronize.controller;

import com.openkm.okmsynchronize.model.ConfigurationModel;
import com.openkm.okmsynchronize.model.ServerCredentials;
import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.view.Communication;
import com.openkm.okmsynchronize.view.ConfigurationView;
import com.openkm.okmsynchronize.ws.OpenKMWS;
import com.openkm.okmsynchronize.ws.OpenKMWSFactory;
import com.openkm.okmsynchronize.ws.OpenKMWSVersions;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.controller.ConfigurationController
 */
public class ConfigurationController {
    
    private ConfigurationView view;
    private ConfigurationModel model;
    
    private ServerCredentials serverCredentials;
    
    private boolean firstCongiguration;
    
    public ConfigurationController(ConfigurationModel model, ConfigurationView view, boolean first) {
        
        this.model = model;
        this.view = view;
        this.firstCongiguration = first;
        
        // Save actual server credentials
        serverCredentials = new ServerCredentials(model.getKeyValue(ConfigurationModel.KEY_USER)
                                                , model.getKeyValue(ConfigurationModel.KEY_PASSWORD)
                                                , model.getKeyValue(ConfigurationModel.KEY_HOST)
                                                , OpenKMWSVersions.valueOf(model.getKeyValue(ConfigurationModel.KEY_SDK_VERSION)));
                                
        view.addButtonAcceptListener(new AcceptConfigureOptions());
        view.addButtonCancelListener(new CancelConfigureOptions());
        view.addButtonSearchWorkDirectoryListener(new SearchWorking());
        view.addButtonTestListener(new TestServerConnection());
    }
    
    /**
     * Button events controller
     */
    
    // Cancel configure Application options
    class CancelConfigureOptions implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            view.setVisible(false);
            view.dispose();            
            if(firstCongiguration) {
                 Communication.showError(view, "No s'ha establert cap configuració. el sistema no pot continuar.");
                 System.exit(1);
            }
        }        
    }
    
    // Accept configure Application options
    class AcceptConfigureOptions implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            String error = validateView(view.getSynchronizeIntervalValue(), view.getWorkingDirectoriValue());
            if(Utils.isEmpty(error)) {                        
                model.setKeyValue(ConfigurationModel.KEY_WORK_DIRECTORY, view.getWorkingDirectoriValue());
                model.setKeyValue(ConfigurationModel.KEY_HOST, view.getHostValue());
                model.setKeyValue(ConfigurationModel.KEY_USER, view.getUserValue());
                model.setKeyValue(ConfigurationModel.KEY_PASSWORD, view.getPasswordValue());
                model.setKeyValue(ConfigurationModel.KEY_SDK_VERSION, view.getSDKVersionValue());
                model.setKeyValue(ConfigurationModel.KEY_RESTRICT_EXTENSIONS, view.getRestrictedExtensionsValue());
                model.setKeyValue(ConfigurationModel.KEY_INVALID_CHARACTERS, view.getInvalidCharactersValue());
                model.setKeyValue(ConfigurationModel.KEY_SYNCHRONIZE_INTERVAL, view.getSynchronizeIntervalValue());
                model.setKeyValue(ConfigurationModel.kEY_EXPLORER, view.getExplorerValue());
                model.setKeyValue(ConfigurationModel.KEY_NAVIGATOR, view.getNavigatorValue());

                model.saveConfiguration();               
                
                // Comprovam si s'ha modificat les credencials per avisa, més endavant farem altra cosa
                if(serverCredentials.isValid()) {
                    ServerCredentials sc = new ServerCredentials(model.getKeyValue(ConfigurationModel.KEY_USER)
                                                               , model.getKeyValue(ConfigurationModel.KEY_PASSWORD)
                                                               , model.getKeyValue(ConfigurationModel.KEY_HOST)
                                                               , OpenKMWSVersions.valueOf(model.getKeyValue(ConfigurationModel.KEY_SDK_VERSION)));
                    if(!serverCredentials.equals(sc)) {
                        Communication.showMessage(view, "S'han modificat les dades del servidor openkm......");
                    }
                }

                view.setVisible(false);
                view.dispose();
                Communication.showMessage(view, "New options saved");
            } else {
                Communication.showMessage(view, error);
            }
        }        
    }
    
    // Search Working director dialog
    class SearchWorking implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // Selected work Directory
            JFileChooser fc = new JFileChooser("Select Working directory");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setFileHidingEnabled(true);

            int code = fc.showOpenDialog(null);
            if (code == JFileChooser.APPROVE_OPTION) {
                view.setWorkingDirectoriValue((fc.getSelectedFile()).getAbsolutePath());
            }
        }
    }
    
    // Testing connection server
    class TestServerConnection implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            ServerCredentials credentials = new ServerCredentials(view.getUserValue(), view.getPasswordValue(), view.getHostValue(), OpenKMWSVersions.valueOf(view.getSDKVersionValue()));                                      
                    
            if(!credentials.isValid()) {
                Communication.showError(view, "Invalid credentials. Conneciont Fail!");
            } else {
                OpenKMWS ws = null;
                try {
                    ws = OpenKMWSFactory.instance(credentials);
                } catch (SynchronizeException ex) {
                    //TODO controlar error
                }
        
                if(ws == null || !ws.isConnectionSuccessful()) {
                    Communication.showMessage(view, "Conneciont Fail!");
                } else {
                    Communication.showMessage(view, "Conneciont Successful!");
                }
            }            
        }        
    }
    
    private String validateView(String interval, String workPath) {
        StringBuilder errors = new StringBuilder();
        
        if(Utils.isEmpty(workPath) || !(new File(workPath)).exists() || !(new File(workPath)).isDirectory()) {            
            errors.append("El formulati té el següents errors:").append("\n");
            errors.append(" - Work path is invalid.").append("\n");
        }
        if(Utils.isEmpty(interval) || !Utils.isNumber(interval) || Integer.parseInt(interval) <= 0) {
            if(errors.length() == 0) { errors.append("El formulati té el següents errors:").append("\n"); }
            errors.append(" - Refresh (seconds) is invalid.").append("\n");
        }        
        
        return errors.toString();
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.service.SynchronizeService;
import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.ws.OpenKMWS;
import com.openkm.okmsynchronize.ws.OpenKMWSFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

/**
 *
 * @author abujosa
 */
public class SynchronizeTask extends TimerTask {   
    
    private final static String KEY_BUNDLE = SynchronizeTask.class.getName();
    
    private SynchronizedRepository repository;
    private ServerCredentials credentials;
    private SynchronizeLog log;
    private AlertManagerModel alertManager;    

    public SynchronizeTask(SynchronizedRepository repository, ServerCredentials credentials, SynchronizeLog log, AlertManagerModel alertManager) {
        this.repository = repository;
        this.credentials = credentials;
        this.log = log;
        this.alertManager = alertManager;
    }
    
    @Override
    public void run() {
        
        log.info(KEY_BUNDLE + " Running synchronize task.");
        
        OpenKMWS ws = null;
        try {
            ws = OpenKMWSFactory.instance(credentials);
        } catch(SynchronizeException e) {
            log.error(KEY_BUNDLE, e);
        }
        
        if(ws == null || !ws.isConnectionSuccessful()) {
            SynchronizeAlert alert = new SynchronizeAlert("Synchronization error", "Could not connect to server", SynchronizeAlert.SynchronizeAlertType.ERROR);
            alertManager.addAlert(alert);
        } else {            
            SynchronizeService service = SynchronizeService.getInstance(ws, log, alertManager);
            
            // Sincroitzam les carpetes
            List<String> deleteSF = new ArrayList<String>();
            for(SynchronizedFolder sf : repository.getSynchronizedFolders()) {
            	
            	// Check exist synchronized folder on server
				try {
					if (ws.exists(sf.getFolderServerPath(), "folder")) {

						sf.refreshSynchronizeFolder();
						if (!sf.isDelete()) {
							service.synchronizeFolder(sf);
							sf.purgeSynchronizeObjects();
							sf.persistSynchronizedObjects();
						}
					} else { 
						deleteSF.add(sf.getFolderUUID());						
					}
				} catch (SynchronizeException se) {
					log.error(KEY_BUNDLE, se);
				}
			}
            
            // Delete Sf
            for(String deleteUUID : deleteSF) {
            	repository.removeSynchronizedFolder(deleteUUID);
            }
            
            // Sincronitzam els documents
            repository.refreshSynchronizedDocuments();
            service. synchronizeDocuments(repository.getSyncronizedDocuments());
                                
            // Eliminam els objectes del repositori que ja no existeixen
            repository.purgeRemovedSynchronizedObejcts();            
        }
        
        log.info(KEY_BUNDLE + " Stopped synchronize task.");
        
    }
    
    
}

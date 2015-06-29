/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.ws.OpenKMWSFactory;
import java.util.Timer;

/**
 *
 * @author abujosa
 */
public class SynchronizeTimer {
    
    private final static String KEY_BUNDLE = SynchronizeTimer.class.getName();
    
    private int pollingInterval;
    private SynchronizedRepository repository;
    private ServerCredentials credentials;
    private Boolean running;
    private SynchronizeLog log;
    private AlertManagerModel alertManager;
    private SynchronizeTask task;
    private Timer timer;
    

    public SynchronizeTimer(int pollingInterval, SynchronizedRepository repository, ServerCredentials credentials, AlertManagerModel alertManager, SynchronizeLog log) {
        this.timer = new Timer(Boolean.TRUE);
        this.pollingInterval = pollingInterval;
        this.repository = repository;
        this.credentials = credentials;
        this.running = Boolean.FALSE;
        this.alertManager = alertManager;
        this.log = log;
    }

    public void initialize(Integer delay) {
        
        try {
            if(credentials.isValid() && OpenKMWSFactory.instance(credentials).isConnectionSuccessful()) {      
                timer = new Timer(Boolean.TRUE);
                task = new SynchronizeTask(repository, credentials, log, alertManager);
                timer.schedule(task, delay != null ? delay : 0, pollingInterval * 1000);
                running = Boolean.TRUE;
                log.info(KEY_BUNDLE + " Synchronize service running.");                
            } else {
                SynchronizeAlert alert = new SynchronizeAlert("Synchronize Service can't startting", credentials.getInfo(), SynchronizeAlert.SynchronizeAlertType.INFO);
                alertManager.addAlert(alert);
                log.info(KEY_BUNDLE + " Synchronize service can't startting: " + credentials.getInfoConnection());
            }
        } catch (SynchronizeException ex) {
            log.error(KEY_BUNDLE + "Error initialize synchronize service", ex);
        }
    } 
    
    public void stop() {
        running = Boolean.FALSE;
        
        if (task != null) { task.cancel(); }
        timer.cancel();
        
        log.info(KEY_BUNDLE + " Synchronize service stopped.");
    }
    
    public void restard() {
        if(!running) {
            initialize(null);
        }
    }
    
    public void reset(int pollingInterval, SynchronizedRepository repository, ServerCredentials credentials) {
        
        running = Boolean.FALSE;
        
        
        if (task != null) { task.cancel(); }
        timer.cancel();
        
        this.pollingInterval = pollingInterval;
        this.repository = repository;
        this.credentials = credentials;
        
        log.info(KEY_BUNDLE + " Reset synchronize service, new values saved.");    
        
        initialize(null);        
    }

    public Boolean isRunning() { return running; }
    
}

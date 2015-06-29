/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.Constants;
import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.ws.OpenKMWS;
import com.openkm.okmsynchronize.ws.OpenKMWSFactory;
import com.openkm.okmsynchronize.ws.OpenKMWSVersions;
import com.openkm.sdk4j.bean.Document;
import com.openkm.sdk4j.bean.Folder;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author abujosa
 */
public class SynchronizeDesktopModel {        
    
    private final static String KEY_BUNDLE = SynchronizeDesktopModel.class.getName();
    
    public final static String  DESKOPT_WINDOW          = "com.openkm.okmsynchronize.view.SynchronizeDesktop";    
    
    private static Boolean newError = Boolean.FALSE;
    
    
    private String workDirectoryPath;
    
    private SynchronizeLog log;
    
    private SystemStates state;
    private ConfigurationModel configuration;   
    private ServerCredentials credentials;
    private SynchronizedRepository repository;
    private SynchronizeTimer timer;
    private ContextOpenKMServer serverContext;
    private AlertManagerModel alertManager;
    private RepositoryWatcher rw;
    
    private Map<String, Boolean>  windowManager;
    
    // Constructor
    public SynchronizeDesktopModel() {
        // Set state stopped
        state = SystemStates.STOPPED;
        
        // Starting up the system
        startingUpSystem();                
    }
    
    // Method
    public SystemStates getState() { return state; }
    
    public void changeState(SystemStates newState) { state = newState; }
    
    public ConfigurationModel getConfiguration() { return configuration; } 

    public SynchronizedRepository getRepository() { return repository; }
    public void changeRepository(SynchronizedRepository repository) { this.repository = repository; }

    public ServerCredentials getCredentials() { return credentials; }       

    public AlertManagerModel getAlertManager() { return alertManager; }
    
    public SynchronizeTimer getSynchronizeService() { return timer; }        
    
    public SynchronizeLog getSynchronizeLog() { return log; } 
    public void setSynchronizeLog(SynchronizeLog log) { this.log = log; }    

    public ContextOpenKMServer getServerContext() { return serverContext; }
    public void setServerContext(ContextOpenKMServer serverContext) {this.serverContext = serverContext;}

    public RepositoryWatcher getRepositoryWatcher() { return rw; }   

    /* 
     * Stopping the system
     */
    public void stopingApplication() throws IOException {
    
        log.info(KEY_BUNDLE + " Stoping openkm desktop synchonize application.");    
        
        // Stopping Repository Watcher
        rw.stop();
        
        // Stopping timer tasck
        if(timer.isRunning()) { timer.stop(); }
        
        // change state to stopped
        changeState(SystemStates.STOPPED);
        
        log.info(KEY_BUNDLE + " Openkm desktop synchonize application."); 
    }
    
    public boolean isConnectedToServer() {
        try {
            return credentials.isValid() && OpenKMWSFactory.instance(credentials).isConnectionSuccessful();
        } catch (SynchronizeException ex) {
            log.error(KEY_BUNDLE, ex);
            return false;
        }
    }        
    
    public String getNameContextRootServerNode() {
        try {
            OpenKMWS ws = OpenKMWSFactory.instance(credentials);      
            String contx = ws.getRootNode(serverContext.getNodeName());
            if(!Utils.isEmpty(contx)) { return contx; }
            else { return "An error has occurred on the server";}
        } catch (SynchronizeException e) {
            log.error(KEY_BUNDLE, e);
            return "Error: Could not connect to server!!";
        }
    }
    
    public List<SynchronizedObject> getChildrensServerNode(String path) {
        List<SynchronizedObject> list = new ArrayList<SynchronizedObject>();

        if (!Utils.isEmpty(path)) {
            try {
                OpenKMWS ws = OpenKMWSFactory.instance(credentials);
                
                // Adding folders
                for (Folder f : ws.listFolders(path)) {
                    SynchronizedObject sobj = new SynchronizedObject(Utils.getName(f.getPath()), f.getUuid(), f.getPath(), Boolean.TRUE);
                    list.add(sobj);
                }
                
                // Adding Documents
                for (Document d : ws.listDocuments(path)) {
                    SynchronizedObject sobj = new SynchronizedObject(Utils.getName(d.getPath()), d.getUuid(), d.getPath(), Boolean.FALSE);
                    list.add(sobj);
                }
            } catch (SynchronizeException e) {
                log.error(KEY_BUNDLE, e);
            }
        }
        return list;
    }

    public boolean isWindowVisible(String window) { return windowManager.containsKey(window) && windowManager.get(window); }
    public void setWindowVisible(String window, boolean visible) { windowManager.put(window, visible); }    
    
    /**
     * Private Method
     */
    
    /**
     * Starting up the system    
     */
    private void startingUpSystem() {
        
        Utils.writeMessage(KEY_BUNDLE + " Starting up openkm desktop synchonize application.");     
        
        // Initialize window manager
        windowManager = new HashMap<String, Boolean>() {
            {
                put("DESKOPT_WINDOW", Boolean.FALSE);
            }
        };
        
        // initialize alerts manager model
        alertManager = AlertManagerModel.getAlertManagerModel();
        
        writeBootstrapEnvironment();
        
        // Create configuration
        configuration = new ConfigurationModel();
        Utils.writeMessage(configuration.getConfigurationInfo());     
        
        // Set show alerts to alerts manager
        alertManager.setShowAlerts("true".equals(configuration.getKeyValue(configuration.kEY_NOTIFY_ERRORS)));
                
        // Set the working user directory
        setWorkingDirectory(configuration.getKeyValue(ConfigurationModel.KEY_WORK_DIRECTORY));
        
        // Create log
        boolean debug = "DEBUG".equals(configuration.getKeyValue(ConfigurationModel.KEY_DEBUG_LEVEL));
        log = new SynchronizeLog(getWorkingDirectory(), debug);
        
        // Check the credentials to the server
        credentials = new ServerCredentials(configuration.getKeyValue(ConfigurationModel.KEY_USER) 
                                                            , configuration.getKeyValue(ConfigurationModel.KEY_PASSWORD)
                                                            , configuration.getKeyValue(ConfigurationModel.KEY_HOST)
                                                            , !Utils.isEmpty(configuration.getKeyValue(ConfigurationModel.KEY_SDK_VERSION))? OpenKMWSVersions.valueOf(configuration.getKeyValue(ConfigurationModel.KEY_SDK_VERSION)) : null);
        OpenKMWS ws = null;
        try {
            ws = OpenKMWSFactory.instance(credentials);
        } catch (SynchronizeException e) {
            log.error(KEY_BUNDLE, e);
        }
        
        if(ws == null || !ws.isConnectionSuccessful()) {
        
            changeState(SystemStates.BAD_CREDENTIALS); 
            
            log.info(KEY_BUNDLE + " Openkm desktop synchonize application bad credentials.");  
            log.info(KEY_BUNDLE + " Could not connect to server.");              
        }
        
        // Create monitor watcher
        rw = new RepositoryWatcher(configuration, log);
        
        // initialize repository
        repository = new SynchronizedRepository(getWorkingDirectory()
                                              , configuration.getKeyValue(ConfigurationModel.KEY_RESTRICT_EXTENSIONS)
                                              , configuration.getKeyValue(ConfigurationModel.KEY_INVALID_CHARACTERS)
                                              , credentials
                                              , getSynchronizeLog()
                                              , rw);
        
        rw.setRepository(repository);
        
        // Get Timer synchronize task
        timer = new SynchronizeTimer(Integer.parseInt(configuration.getKeyValue(ConfigurationModel.KEY_SYNCHRONIZE_INTERVAL)), repository, credentials, alertManager, log);          
        timer.initialize((10*1000));        
        
        // Start monitor watcher
        try {          
            rw.start();
        } catch (IOException io) {
            log.error(KEY_BUNDLE, io);
        }        
        
        // Set server default context
        serverContext = ContextOpenKMServer.TAXONOMY;
        
        // Change state to Running
        changeState(SystemStates.RUNNING);        
        
        log.info(KEY_BUNDLE + " Openkm desktop synchonize application is running."); 
    }
    
    /*
     * Write Bootstrap Environment
     */
    private void writeBootstrapEnvironment() {
        Utils.writeMessage("OpenKM Knowledge Management");
        Utils.writeMessage("  Copyright (c) 2006, 2014, Desktop Sync. All rights reserved.\n");

        Utils.writeMessage("================================================================\n");
        Utils.writeMessage("   OpenKM desktop synchronize Bootstrap Environment\n");
        Utils.writeMessage("   OS-System: " + System.getProperty("os.name").toLowerCase() + "\n");
        Utils.writeMessage("   VERSION: " + Constants.OPENKM_SYNCHRONIZE_VERSION + "\n");
        Utils.writeMessage("   BUILD: " + Constants.OPENKM_SYNCHRONIZE_BUILD + "\n");
        Utils.writeMessage("================================================================");
    }
    
    /*
     * set the working directory
     */
    public void setWorkingDirectory(String path) {
        
        this.workDirectoryPath = Utils.buildLocalFilePath(path, Constants.WORK_DIRECTORY_NAME);
        
        // Create working directory
        File workDirectory = new File(workDirectoryPath);
        if(!workDirectory.exists() || !workDirectory.isDirectory()) {
            workDirectory.mkdir();
        }
    }
    
    public String getWorkingDirectory() { return this.workDirectoryPath; }    
    
    public static boolean haveNewError() { return newError; }
    public static void setNewError(boolean nerror) { newError = nerror; }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.utils;

import com.openkm.okmsynchronize.Constants;
import com.openkm.okmsynchronize.model.SynchronizeDesktopModel;
import java.io.IOException;
import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.utils.Log
 */
public class SynchronizeLog {
    
    private static final String KEY_BUNDLE = SynchronizeLog.class.getName();
          
    private static boolean initialized;
    private static String loggerPath;
    private static boolean debug;
    
    public SynchronizeLog(String path, boolean debug) {
        initialized = false;
        this.debug = debug;
        loggerPath = Utils.buildLocalFilePath(path, Constants.LOG_FOLDER_NAME, Constants.LOG_FILE_NAME);
    }            
    
    private static void initialize() {
        Appender appender = null;        
        
        if (!Utils.isEmpty(loggerPath)) {
            try {
                appender = new DailyRollingFileAppender(new PatternLayout("%d{dd/MM/yyyy HH:mm:ss,SSS} %-5p - %m%n"), loggerPath, ".yyyy-MM-dd");
                //FileAppender(new PatternLayout("%d{dd/MM/yyyy HH:mm:ss,SSS} %-5p %c %x - %m%n"), pahtLog);
                appender.setName(loggerPath);
                Logger.getLogger(loggerPath).removeAllAppenders();
                Logger.getLogger(loggerPath).addAppender(appender);
                
                initialized = true;
                
                Utils.writeMessage(KEY_BUNDLE + " System log initialized.");

            } catch (IOException ex) {
                 Utils.writeMessage(KEY_BUNDLE + " Unable to initialize the system log \n " + ex.getMessage());
            }

            if (appender == null) {
                BasicConfigurator.configure();
                Logger.getLogger(loggerPath).setLevel(Level.DEBUG);
            }            
        } else {
            // TODO: generar error per no poder inicialitzar el log
        }
    
    }
    
    public  void debug(String msg) {        
        if (!initialized) initialize();
        if(debug) {
            Logger logger = Logger.getLogger(loggerPath);
            logger.debug(msg);
        }
    }

    public static void warn(String msg) {
        if (!initialized) initialize();
        Logger logger = Logger.getLogger(loggerPath);
        logger.warn(msg);
    }

    public void info(String msg) {
        if (!initialized) initialize();
        Logger logger = Logger.getLogger(loggerPath);
        logger.info(msg);
    }
    
    public void error(String msg, Throwable t) {
        // Notify Error
        SynchronizeDesktopModel.setNewError(Boolean.TRUE);
        
        if (!initialized) initialize();
        Logger logger = Logger.getLogger(loggerPath);
        if (t != null) {
            logger.error(msg, t);
        } else {
            logger.error(msg);
        }
    }
    
}

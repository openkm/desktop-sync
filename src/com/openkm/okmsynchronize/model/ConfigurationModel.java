
package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.service.ConfigurationService;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.ws.OpenKMWSVersions;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.model.ConfigurationModel
 */
public class ConfigurationModel {
    
    private static final String KEY_BUNDLE = ConfigurationModel.class.getName();
    
    public final static String KEY_WORK_DIRECTORY       = "com.openkm.odesktop.work_directory";
    public final static String KEY_SDK_VERSION          = "com.openkm.odesktop.sdk_version";
    public final static String KEY_HOST                 = "com.openkm.odesktop.host";
    public final static String KEY_USER                 = "com.openkm.odesktop.user";
    public final static String KEY_PASSWORD             = "com.openkm.odesktop.password";
    public final static String KEY_RESTRICT_EXTENSIONS  = "com.openkm.odesktop.restrict_extensions";
    public final static String KEY_INVALID_CHARACTERS   = "com.openkm.odesktop.invalid_characters";
    public final static String KEY_DEBUG_LEVEL          = "com.openkm.odesktop.log.level";
    public final static String KEY_SYNCHRONIZE_INTERVAL = "com.openkm.odesktop.synchronize.interval";
    public final static String KEY_NAVIGATOR            = "com.openkm.odesktop.defauld_navigator";
    public final static String kEY_EXPLORER             = "com.openkm.odesktop.defauld_explorer";
    public final static String kEY_NOTIFY_ERRORS        = "com.openkm.odesktop.notify_errors";
    public final static String kEY_LOCALE               = "com.openkm.odesktop.locale";
    public final static String kEY_MONITOR_COREPOOLSIZE               = "com.openkm.odesktop.monitor.corepoolsize";
    public final static String kEY_MONITOR_MAXIMUMPOOLSIZE            = "com.openkm.odesktop.monitor.maximumpoolsize";
    public final static String kEY_MONITOR_KEEPALIVETIME              = "com.openkm.odesktop.monitor.keepalivetime";

    private final static String USER_HOME               = System.getProperty("user.home");
    private final static String CONFIGURATION_FILE_NAME = ".okmDesktopConfiguration";    
    
    public static final Map<String, String> DEFAULT_MAP_CONFIGURATION = new HashMap<String, String>() {
        {
            put(KEY_HOST, "");
            put(KEY_USER, "");
            put(KEY_PASSWORD, "");
            put(KEY_SDK_VERSION, OpenKMWSVersions.v1_0.getId());
            put(kEY_LOCALE, "EN");
            put(KEY_DEBUG_LEVEL, "INFO");
            put(KEY_WORK_DIRECTORY, "");
            put(KEY_RESTRICT_EXTENSIONS, ".back,.~,.odesktop");
            put(KEY_INVALID_CHARACTERS, ":");
            put(KEY_SYNCHRONIZE_INTERVAL, "60");
            put(KEY_NAVIGATOR, "google-chrome");
            put(kEY_EXPLORER, "nautilus");
            put(kEY_NOTIFY_ERRORS, "true"); 
            put(kEY_MONITOR_COREPOOLSIZE, "2"); 
            put(kEY_MONITOR_MAXIMUMPOOLSIZE, "4"); 
            put(kEY_MONITOR_KEEPALIVETIME, "10"); 
        }
    };
    
    private Map<String, String> mapConfiguration;
    private Map<String, String> oldConfiguration;

    /*
     * Constructor
     */
    public ConfigurationModel() {
        
        ConfigurationService service = null;
        
        oldConfiguration = null;
        
        service = ConfigurationService.getConfigurationService();
        
        Utils.writeMessage(KEY_BUNDLE + " Reading configuration system.");          
        String configurationPath = Utils.buildLocalFilePath(USER_HOME, CONFIGURATION_FILE_NAME);
        mapConfiguration = service.readConfiguration(new File(configurationPath));
        if(mapConfiguration == null) {
            mapConfiguration = DEFAULT_MAP_CONFIGURATION;
            service.establirPrimeraConfiguracio(this);
        } else {
            Utils.writeMessage(KEY_BUNDLE + " Configuration loaded.");
        }
        
    }
    
    /*
     * Get the value of the configuration key
     */
    public String getKeyValue(String key) {
        if(mapConfiguration != null && mapConfiguration.containsKey(key)) {
            return mapConfiguration.get(key);
        } else {
            return null;
        }
    }
    
    /*
     * Set the value of the configuration key
     */
    public void setKeyValue(String key, String value) {
        if(mapConfiguration != null) {
            mapConfiguration.put(key, value);
        }
    }
    
    /*
     * Save the configuration settings
     */
    public void saveConfiguration() {
        ConfigurationService service = null;
        
        service = ConfigurationService.getConfigurationService();
        
        String configurationPath = Utils.buildLocalFilePath(USER_HOME, CONFIGURATION_FILE_NAME);
        service.writeMapConfiguration(new File(configurationPath), mapConfiguration);
    }
    
    /*
     * Get configuration values info to string
     */
    public String getConfigurationInfo() {
        StringBuilder str = new StringBuilder();

        str.append("System configuration \n");
        str.append("================================================================\n");
        str.append("   WORKING_DIRECTORY: ").append(getKeyValue(KEY_WORK_DIRECTORY)).append("\n");
        str.append("   SDK_VERSION: ").append(getKeyValue(KEY_SDK_VERSION)).append("\n");
        str.append("   HOST: ").append(getKeyValue(KEY_HOST)).append("\n");
        str.append("   USERNAME: ").append(getKeyValue(KEY_USER)).append("\n");
        str.append("   RESTRICT_EXTENSIONS: ").append(getKeyValue(KEY_RESTRICT_EXTENSIONS)).append("\n");
        str.append("   INVALID_CHARACTERS: ").append(getKeyValue(KEY_INVALID_CHARACTERS)).append("\n");
        str.append("   DEBUG_MODE: ").append(("DEBUG".equals(getKeyValue(KEY_DEBUG_LEVEL)))).append("\n");
        str.append("================================================================\n");

        return str.toString();
    }

    public boolean isChangedKey(String key) { 
    
            if(Utils.isEmpty(key)) { return false; }
            else if(oldConfiguration == null) { return false; }
            else if(!mapConfiguration.containsKey(key) || !oldConfiguration.containsKey(key)) { return false; }
            else { return !mapConfiguration.get(key).equals(oldConfiguration.get(key)); }
    }        
    
    public boolean isChanged() {
        return isChangedKey(KEY_WORK_DIRECTORY) || isChangedKey(KEY_SDK_VERSION) || isChangedKey(KEY_HOST) ||
               isChangedKey(KEY_USER) || isChangedKey(KEY_PASSWORD) || isChangedKey(KEY_RESTRICT_EXTENSIONS) ||
               isChangedKey(KEY_INVALID_CHARACTERS) || isChangedKey(KEY_SYNCHRONIZE_INTERVAL) || isChangedKey(KEY_NAVIGATOR) ||
               isChangedKey(kEY_EXPLORER);
    }
    
    public void initChangeControl() {
        oldConfiguration = new HashMap<String, String>();
        
        for(String key : mapConfiguration.keySet()) {
            oldConfiguration.put(key, mapConfiguration.get(key));            
        }
    }
    
    public void stopChangeControl() {
        oldConfiguration = null;
    }
        
}

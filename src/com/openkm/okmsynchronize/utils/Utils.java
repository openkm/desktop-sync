package com.openkm.okmsynchronize.utils;

import com.openkm.okmsynchronize.Constants;
import com.openkm.okmsynchronize.model.SynchronizedRepository;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *  
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.utils.Utils
 */

public class Utils {
    
    /*
     * 
     */
    public static boolean isEmpty(String str) {
        return (str == null || str.length() == 0);
    }
    
    /*
     * Determines whether the OS is Windows
     */
    public static boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
    }

    /*
     * Determines whether the OS is Mac
     */
    public static boolean isMac() {
        return (System.getProperty("os.name").toLowerCase().indexOf("mac") >= 0);
    }

    /*
     * Determines whether the OS is Linux
     */
    public static boolean isUnix() {
        return (System.getProperty("os.name").toLowerCase().indexOf("nix") >= 0 || System.getProperty("os.name").toLowerCase().indexOf("nux") >= 0 || System.getProperty("os.name").toLowerCase().indexOf("aix") > 0);
    }
    
    /*
     * Build the local file path
     */
    public static String buildLocalFilePath(String... parts) {
        StringBuilder path = new StringBuilder();

        if (parts != null) {
            if (parts.length >= 2) {
                for (int i = 0; i < parts.length - 1; i++) {
                    path.append(parts[i]).append(Constants.FILE_SEPARATOR_OS);
                }

                path.append(parts[parts.length - 1]);
            } else {
                return parts[0];
            }
        }
        return path.toString();
    }
    
    /**
     * get relative path
     */
    public static String getRelativePath(String path) {              
        return replaceAllString(path, SynchronizedRepository.REPOSITORY_SYNCHRONIZED_FOLDERS_PATH, "");
    }
    
    /*
     * Build the OpenKm server document path
     */
    public static String buildOpenkmDocumentPath(String... parts) {
        StringBuilder path = new StringBuilder();

        if (parts != null) {
            if (parts.length >= 2) {
                for (int i = 0; i < parts.length - 1; i++) {
                    path.append(parts[i]).append(Constants.FILE_SEPARATOR_OKM);
                }

                path.append(parts[parts.length - 1]);
            } else {
                return parts[0];
            }
        }
        return path.toString();
    }
    
    /*
     * Show a hidden local file
     */
    public static void displayLocalFile(String path) {
        try {
            if (new File(path).exists()) {
                Runtime rt = Runtime.getRuntime();
                //put your directory path instead of your_directory_path
                Process proc = rt.exec("attrib -h \"" + path + "\"");
                int exitVal = proc.waitFor();
            } else {
                
            }
        } catch (Throwable t) {
            
        }
    }

    /*
     * Hide de local file in win SO
     */
    public static void hideLocalFile(String path) {
        try {
            if (new File(path).exists()) {
                Runtime rt = Runtime.getRuntime();
                //put your directory path instead of your_directory_path
                Process proc = rt.exec("attrib +h \"" + path + "\"");
                int exitVal = proc.waitFor();
            } else {
                
            }
        } catch (Throwable t) {
            
        }
    }
    
    /*
     * write message to console
     */
    public static void writeMessage(String msg) {
        StringBuilder str = new StringBuilder();
        
        str.append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss,SSS").format(new Date())).append(" ");
        str.append(msg);
        
        System.out.println(str.toString());                        
    }
    
    /*
     * Get name from path
     */
    public static String getName(String path) {
        if (path == null) {
            return null;
        } else if (path.contains(Constants.FILE_SEPARATOR_OKM)) {
            return path.substring(path.lastIndexOf(Constants.FILE_SEPARATOR_OKM) + 1, path.length());
        } else if (path.contains(Constants.FILE_SEPARATOR_OS)) {
            return path.substring(path.lastIndexOf(Constants.FILE_SEPARATOR_OS) + 1, path.length());
        } else {
            return path;
        }
    }
    
    /*
     * Test n is a number
     */
    public static boolean isNumber(String n) {
        try {
            Integer.parseInt(n);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    
    /**
     * Return sufix for the folder name in directory
     */
    private static String getSufixFolderName(String directoryPath, String name) {
        
        File rd = new File(directoryPath);
        
        int count = 0;
        for(File f : rd.listFiles()) {
            String vname = buildVirtualName(f.getName());
            if(f.isDirectory() && name.equals(vname)) {
                count ++;
            }
        }

        if(count > 0) { return Integer.toString(count); }
        else { return ""; }
    }
    
    public static String buildVirtualName(String name) {
        if(name.indexOf("(") == -1) { return name; }
        return name.substring(0, name.lastIndexOf("("));
    }
    
    public static String getRealName(String name, String path) {        
        String sufix = !Utils.isEmpty(Utils.getSufixFolderName(path, name))? "(" + Utils.getSufixFolderName(path, name) + ")" : "";
        return name + sufix;
    }     
    
    public static String replaceAllString(String strOrig, String strFind, String strReplace) {
        if (strOrig == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(strOrig);
        String toReplace = "";

        if (strReplace == null) {
            toReplace = "";
        } else {
            toReplace = strReplace;
        }

        int pos = strOrig.length();

        while (pos > -1) {
            pos = strOrig.lastIndexOf(strFind, pos);
            if (pos > -1) {
                sb.replace(pos, pos + strFind.length(), toReplace);
            }
            pos = pos - strFind.length();
        }

        return sb.toString();
    }
}

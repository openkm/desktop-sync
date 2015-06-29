package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.utils.Utils;
import java.io.File;
import java.io.FileFilter;

/**
 * Classe que implementa el filtre de fitxers que es poden pujar al servidor
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.odesktop.SynchronizeFileFilter
 */
public class SynchronizeObjectFileFilter implements FileFilter {
    private static final String INVALID_CHARACTER_1 = "~";
    
    private String extensions;
    private String invalidCharacters;
    private String controlFileName;

    public SynchronizeObjectFileFilter(String ext, String chars, String cfn) {
        extensions = ext;
        invalidCharacters = Utils.isEmpty(chars) ? "" : chars;
        controlFileName = cfn;

        // Adding invalid characters
        invalidCharacters += INVALID_CHARACTER_1;
    }

    @Override
    public boolean accept(File f) {
        if (f.isHidden()) {
            return false;  
        } else if (f.isDirectory()) {
            return !nameContainsInvalidCharacters(f.getName());
        } else if (controlFileName.equals(f.getName())) {
            return false;
        } else if (f.getName().startsWith(".")) {
            return false;
        } else if(f.getName().endsWith("~")) {
            return false;
        } else if (Utils.isEmpty(extensions)) {
            return true;
        } else {
            return extensions.indexOf(getFileExtension(f.getName())) == -1 && !nameContainsInvalidCharacters(f.getName());
        }
    }

    private String getFileExtension(String name) {
        if(name.indexOf(".") == -1) {return name;}
        return name.substring(name.lastIndexOf(".") + 1);
    }

    private boolean nameContainsInvalidCharacters(String name) {
        
        if(Utils.isEmpty(invalidCharacters)) {return false;}
        for (int i = 0; i < invalidCharacters.length(); i++) {
            if (name.contains(String.valueOf(invalidCharacters.charAt(i)))) {
                return true;
            }
        }
        return false;
    }
}

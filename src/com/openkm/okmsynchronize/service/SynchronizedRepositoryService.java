package com.openkm.okmsynchronize.service;

import com.openkm.okmsynchronize.model.SynchronizedObject;
import com.openkm.okmsynchronize.model.SynchronizedFolder;
import com.openkm.okmsynchronize.model.SynchronizedRepository;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.service.SynchronizedRepositoryService
 */
public class SynchronizedRepositoryService {
    
    private static SynchronizedRepositoryService singleton = new SynchronizedRepositoryService();

    /**
     * Construeix un objecte de la classe. Aquest mètode és privat per forçar el
     * patrò singleton.
     */
    private SynchronizedRepositoryService() {
        super();
    }

    /**
     * Recupera l'objecte singleton de la classe.
     */
    public static SynchronizedRepositoryService getSynchronizedRepositoryService() {
        return singleton;
    }
    
    public void openSynchronizedRepository(SynchronizedRepository rep) {        
        
        // Create synchronized folder directory
        File rfd = new File(rep.getRepositoryFoldersPath());
        if(!rfd.exists() || !rfd.isDirectory()) {
            rfd.mkdir();
        }
        
        rep.setSynchronizedFolders(getSynchronizedFolders(rfd, rep.getRestrictedExtensions(), rep.getInvalidCharacters()));
        
        // Create synchronized document directory
        File rdd = new File(rep.getRepositoryDocumentsPath());
        if(!rdd.exists() || !rdd.isDirectory()) {
            rdd.mkdir();
        }    
        
        rep.setSyncronizedDocuments(getSynchronizedDocuments(rdd));
    }
    
    private List<SynchronizedFolder> getSynchronizedFolders(File dir, String restrictedExtensions, String invalidCharacters) {
        
        List<SynchronizedFolder> lsf = new ArrayList<SynchronizedFolder>();
        for(File f : dir.listFiles()) {
            if(SynchronizedFolder.isSynchronizedFolder(f)) {
                SynchronizedFolder fs = SynchronizedFolder.loadSynchronizeFolder(f.getName(), f.getPath(), restrictedExtensions, invalidCharacters);
                fs.refreshSynchronizeFolder();
                lsf.add(fs);
            }
        }        
        return lsf;        
    }
    
    private List<SynchronizedObject> getSynchronizedDocuments(File dir) {
               
        SynchronizedFolder fs = SynchronizedFolder.loadSynchronizeFolder(dir.getName(), dir.getPath(), "", "");
        return fs.getSynchronizeObjects();        
    }    
}

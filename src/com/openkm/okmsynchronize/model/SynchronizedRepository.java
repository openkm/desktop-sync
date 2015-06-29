package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.Constants;
import com.openkm.okmsynchronize.service.SynchronizedRepositoryService;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.view.Communication;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.model.SynchronizedRepository
 */
public class SynchronizedRepository {
    
    protected static final String KEY_BUNDLE = SynchronizedRepository.class.getName();     
    
    public static String REPOSITORY_PATH                         = null;
    public static String REPOSITORY_SYNCHRONIZED_FOLDERS_PATH    = null;
    public static String REPOSITORY_SYNCHRONIZED_DOCUMENTS_PATH  = null;
    
    private List<SynchronizedFolder> synchronizedFolders;
    private List<SynchronizedObject> syncronizedDocuments;    
    
    private String invalidCharacters;
    private String restrictedExtensions;        
    
    private SynchronizeLog log;
    
    private ServerCredentials credentials;
    
    private HashMap<String, SynchronizeWorker> foldersWorkers;
    private HashMap<String, SynchronizeWorker> documentsWorkers;
    private DoWorkFolders fdoWork;
    private DoWorkDocuments ddoWork;
    private static final Object foldersLock = new Object();
    private static final Object documentsLock = new Object();
    private static final Object fworkersLock = new Object();
    private static final Object dworkersLock = new Object();
    
    private RepositoryWatcher rw;
    
    public SynchronizedRepository(String repositoryPath, String restrictedExtensions, String invalidCharacters, ServerCredentials credentials, SynchronizeLog log, RepositoryWatcher rw) {
        SynchronizedRepositoryService service = null;
        
        service = SynchronizedRepositoryService.getSynchronizedRepositoryService();
        
        REPOSITORY_PATH = repositoryPath;
        REPOSITORY_SYNCHRONIZED_FOLDERS_PATH = Utils.buildLocalFilePath(REPOSITORY_PATH, Constants.REPOSITORY_SYNCHRONIZED_FOLDERS_NAME);
        REPOSITORY_SYNCHRONIZED_DOCUMENTS_PATH = Utils.buildLocalFilePath(REPOSITORY_PATH, Constants.REPOSITORY_SYNCHRONIZED_DOCUMENTS_NAME);
        
        this.restrictedExtensions = restrictedExtensions;
        this.invalidCharacters = invalidCharacters;
        this.log = log;
        this.credentials = credentials;
        this.foldersWorkers = new HashMap<String, SynchronizeWorker>();
        this.documentsWorkers = new HashMap<String, SynchronizeWorker>(); 
        this.rw = rw;
        
        service.openSynchronizedRepository(this);   
        
        // Adding synchronized folders to monitor watcher
        try {
        for (SynchronizedFolder sf : getSynchronizedFolders()) {
                rw.addDirectory(sf.getFolderLocalPath());
            }

            // Adding synchronized documents folder to monitor watcher
            rw.addDirectory(getRepositoryDocumentsPath());
        } catch (IOException ex) {
            log.error(KEY_BUNDLE, ex);
        }

    }
    
    public SynchronizedObjectConflict[] getRenamedDocuments(String foldeUUID, String path, String name) {
        log.debug(KEY_BUNDLE + " method:SynchronizedObjectConflict [foldeUUID=" + foldeUUID + ",path=" + path + ",name=" + name + "]");
        
        path = path.replace(Constants.FILE_SEPARATOR_OS + name, "");
        
        if(Utils.isEmpty(foldeUUID) || Utils.isEmpty(path) || Utils.isEmpty(name)) {
            return new SynchronizedObjectConflict[0];
        }
        
        List<SynchronizedObjectConflict> list = new ArrayList<SynchronizedObjectConflict>();
        for(SynchronizedObject sobj : getSynchronizeObject(foldeUUID, path, "document")) {
            if(StateSynchronizeObject.CONFLICT.equals(sobj.getState()) && !sobj.isPossibleToRename() && !name.equals(sobj.getName())) {
                list.add(new SynchronizedObjectConflict(sobj.getName(), sobj.getVersion(), sobj.getUuid()));
            }         
        }
       
        return list.toArray(new SynchronizedObjectConflict[list.size()]);
    }
    
    public boolean containsSynchronizedFolder(String uuid) {
        log.debug(KEY_BUNDLE + " method:containsSynchronizedFolder [uuid=" + uuid + "]");
        
        if(Utils.isEmpty(uuid)) { return false; }
        for(SynchronizedFolder sf : getSynchronizedFolders()) {
            if(uuid.equals(sf.getFolderUUID())) { return true;}            
        }
        return false;
    }  
      
    public void removeSynchronizedFolder(String uuid) {
        log.debug(KEY_BUNDLE + " method:removeSynchronizedFolder [uuid=" + uuid + "]");
        
        synchronized (foldersLock) {
            SynchronizedFolder sf = getSynchronizeFolder(uuid);
            if (sf != null) {
                sf.deleteSynchronizeFolder();
                synchronizedFolders.remove(sf);
            }
        }
    }
    
    public void removeSynchronizedDocument(String uuid) {
        log.debug(KEY_BUNDLE + " method:removeSynchronizedDocument [uuid=" + uuid + "]");

        if (!Utils.isEmpty(uuid)) {

            synchronized (documentsLock) {
                SynchronizedObject rm = null;
                for (SynchronizedObject sobj : syncronizedDocuments) {
                    if (uuid.equals(sobj.getUuid())) {
                        File f = new File(sobj.getLocalPath());
                        if (f.exists()) {
                            f.delete();
                        }
                        rm = sobj;
                    }
                }
                if (rm != null) {
                    syncronizedDocuments.remove(rm);
                }
                
                SynchronizedFolder sf = SynchronizedFolder.getDocumentsSynchronizedFolder(Constants.REPOSITORY_SYNCHRONIZED_DOCUMENTS_NAME, getPath(), syncronizedDocuments);
                sf.persistSynchronizedObjects();
            }
        }
    }
    
    public void purgeSynchronizedObject(String uuid, String name) {
        log.debug(KEY_BUNDLE + " method:purgeSynchronizedObject [uuid=" + uuid + ", name=" + name + "]"); 
                
        for(SynchronizedFolder sf : getSynchronizedFolders()) {
            boolean find = false;
            for(SynchronizedObject o : sf.getSynchronizeObjects()) {
                if(uuid.equals(o.getUuid()) && name.equals(o.getName())) {
                    o.setState(StateSynchronizeObject.DELETE);
                    find = true;
                }
            }
            if(find) {
                sf.persistSynchronizedObjects();
                sf.purgeSynchronizeObjects();
            }
        }
    }
    
    public void refreshSynchronizedDocuments() {
        log.debug(KEY_BUNDLE + " method:refreshSynchronizedDocuments"); 
        
         synchronized (documentsLock) {
            for (SynchronizedObject sobj : syncronizedDocuments) {
                File f = new File(sobj.getLocalPath());
                if(f.exists()) {
                    sobj.update(f.lastModified());
                } else {
                    sobj.setState(StateSynchronizeObject.DELETE);
                }
            }
        }        
    }
    
    /**
     * Add new folder to synchronized repository
     */
    public void addFolder(String uuid, String name, String folderServerPath) {
        log.debug(KEY_BUNDLE + " method:addFolder [uuid=" + uuid + " ,name:" + name + " ,folderServerPath:" + folderServerPath + "]");        
        
        synchronized (fworkersLock) {
            // Comprovam que no estigui dins el worker
            if (!foldersWorkers.containsKey(uuid)) {
                
                String realName = Utils.getRealName(name, getRepositoryFoldersPath());

                // cream el worker per a la nova tasca
                SynchronizeWorker worker = new SynchronizeFolderWorker(uuid, getRepositoryFoldersPath(), name, folderServerPath, credentials, log, restrictedExtensions, invalidCharacters);

                foldersWorkers.put(realName, worker);
                ((SynchronizeWorker) foldersWorkers.get(realName)).start();

                if (fdoWork == null || fdoWork.getState() == Thread.State.TERMINATED) {
                    fdoWork = new DoWorkFolders();
                    fdoWork.start();
                    
                    // active alert
                    AlertManagerModel am = AlertManagerModel.getAlertManagerModel();
                    am.setCheckAlert(AlertManagerModel.KeyChecks.KEY_FODER_SYNCHRONIZE_RUNNING, Boolean.TRUE);
                }
            }
        }
    }
    
    public void addDocument(String uuid) {
        log.debug(KEY_BUNDLE + " method:addDocument [uuid=" + uuid + "]");
        
        synchronized (dworkersLock) {
            // Comprovam que no estigui dins el worker
            if (!documentsWorkers.containsKey(uuid)) {

                // cream el worker per a la nova tasca
                SynchronizeWorker worker = new SynchronizeDocumentWorker(uuid, getPath(), Constants.REPOSITORY_SYNCHRONIZED_DOCUMENTS_NAME, credentials, log);

                documentsWorkers.put(uuid, worker);
                ((SynchronizeWorker) documentsWorkers.get(uuid)).start();

                if (ddoWork == null || ddoWork.getState() == Thread.State.TERMINATED) {
                    ddoWork = new DoWorkDocuments();
                    ddoWork.start();
                    
                    // active alert
                    AlertManagerModel am = AlertManagerModel.getAlertManagerModel();
                    am.setCheckAlert(AlertManagerModel.KeyChecks.KEY_DOCUMENT_SYNCHRONIZE_RUNNING, Boolean.TRUE);
                }
            }
        }        
    }
    
    /**
     * 
     * @param type folder | document | all
     */
    public List<SynchronizedObject> getSynchronizeObject(String uuid, String path, String type) {
        log.debug(KEY_BUNDLE + " method:getSynchronizeObject [uuid=" + uuid + ", path=" + path + ", type=" + type + "]");
        
        List<SynchronizedObject> list = new ArrayList<SynchronizedObject>();
        
        SynchronizedFolder sf = getSynchronizeFolder(uuid);
        if(sf != null) {
            String relativePath = path.replace(getRepositoryFoldersPath(), "");
            list = sf.getSynchronizeObjectsByRelativePath(relativePath, type);
        }        
        return list;
    }
    
    public List<SynchronizedObject> getAllSynchronizeObjects() {
        log.debug(KEY_BUNDLE + " method:getAllSynchronizeObjects []");
        
        List<SynchronizedObject> list = new ArrayList<SynchronizedObject>();
        for(SynchronizedFolder sf : getSynchronizedFolders()) {
            list.addAll(sf.getSynchronizeObjects());
        }        
        return list;
    }
    
    public List<SynchronizedObject> getSynchronizeDocumentsByFolder(String uuid) {
        log.debug(KEY_BUNDLE + " method:getAllSynchronizeObjects [uuid= " + uuid + "]");
        
        SynchronizedFolder sf = getSynchronizeFolder(uuid);
        if(sf != null) {
            return sf.getSynchronizeObjectsByType("document");
        } else {
            return new ArrayList<SynchronizedObject>();  
        }                      
    }

    public String getPath() { return REPOSITORY_PATH; }
    public void setRepositoryPath(String repositoryPath) { 
        REPOSITORY_PATH = repositoryPath; 
        REPOSITORY_SYNCHRONIZED_FOLDERS_PATH = Utils.buildLocalFilePath(REPOSITORY_PATH, Constants.REPOSITORY_SYNCHRONIZED_FOLDERS_NAME);
        REPOSITORY_SYNCHRONIZED_DOCUMENTS_PATH = Utils.buildLocalFilePath(REPOSITORY_PATH, Constants.REPOSITORY_SYNCHRONIZED_DOCUMENTS_NAME);
    }
    
    public String getRepositoryFoldersPath() { return REPOSITORY_SYNCHRONIZED_FOLDERS_PATH; }
    public String getRepositoryDocumentsPath() { return REPOSITORY_SYNCHRONIZED_DOCUMENTS_PATH; }

    public List<SynchronizedObject> getSyncronizedDocuments() { 
        log.debug(KEY_BUNDLE + " method:getSyncronizedDocuments []");
        
        synchronized (documentsLock) {
            return syncronizedDocuments;
        }
    }
    public void setSyncronizedDocuments(List<SynchronizedObject> syncronizedDocuments) { this.syncronizedDocuments = syncronizedDocuments; }

    public List<SynchronizedFolder> getSynchronizedFolders() {
        log.debug(KEY_BUNDLE + " method:getSynchronizedFolders []");
        
        synchronized (foldersLock) {
            return synchronizedFolders;
        }
    }
    public void setSynchronizedFolders(List<SynchronizedFolder> synchronizedFolders) { 
        log.debug(KEY_BUNDLE + " method:setSynchronizedFolders [list]");
        
        synchronized (foldersLock) {
            this.synchronizedFolders = synchronizedFolders; 
        }        
    }
    
    public void purgeRemovedSynchronizedObejcts() {
        log.debug(KEY_BUNDLE + " method:removeSynchronizedFolder[]");
        
        synchronized (foldersLock) {
            List<SynchronizedFolder> rmf = new ArrayList<SynchronizedFolder>();
            for(SynchronizedFolder f : synchronizedFolders) {
                if(f.isDelete()) { rmf.add(f); }
            }            
            
            synchronizedFolders.removeAll(rmf);
        }
        
        synchronized (documentsLock) {
            List<SynchronizedObject> rmo = new ArrayList<SynchronizedObject>();
            for (SynchronizedObject sobj : syncronizedDocuments) {
               if(sobj.getState().equals(StateSynchronizeObject.DELETE)) {
                   rmo.add(sobj);
               }
            }
            syncronizedDocuments.removeAll(rmo);
        }        
        
    }

    public String getInvalidCharacters() { return invalidCharacters; }
    public void setInvalidCharacters(String invalidCharacters) { this.invalidCharacters = invalidCharacters; }

    public String getRestrictedExtensions() { return restrictedExtensions; }
    public void setRestrictedExtensions(String restrictedExtensions) { this.restrictedExtensions = restrictedExtensions; }

    public void setCredentials(ServerCredentials credentials) { this.credentials = credentials; }
    
    private SynchronizedFolder getSynchronizeFolder(String uuid) {
        log.debug(KEY_BUNDLE + " method:getSynchronizeFolder [uuid=" + uuid + "]");
        
        if(Utils.isEmpty(uuid)) { return null; }
        for(SynchronizedFolder sf : getSynchronizedFolders()) {
            if(uuid.equals(sf.getFolderUUID())) { return sf; }
        }
        return null;
    }
    
    /**
     * private class
     */
    class DoWorkFolders extends Thread {

        private boolean stop;

        public DoWorkFolders() {
            super("DoWorkFolders-Repository");
            stop = false;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            log.info(KEY_BUNDLE + " Starting synchronized repository doWork.");                        
                        
            while (!stop) {
                Set<String> ids = new HashSet<String>();
                
                synchronized (fworkersLock) { for(String id : foldersWorkers.keySet()) { ids.add(id); } }
                try {                    

                    if (ids.isEmpty()) {
                        stop = true;
                    } else {
                        for (String id : ids) {
                            if (((SynchronizeWorker) foldersWorkers.get(id)).getState() == Thread.State.TERMINATED) {

                                SynchronizedFolder fs = new SynchronizedFolder(id, getRepositoryFoldersPath());
                                getSynchronizedFolders().add(fs);
                                synchronized (fworkersLock) { foldersWorkers.remove(id); }
                                
                                // Show message information
                                Communication.showMessage(null, fs.getVirtualName() + " add to synchronized folders.");
                                
                                try {
                                    // adding folder to monitor watcher
                                    rw.addDirectory(fs.getFolderLocalPath());
                                } catch (IOException ex) {
                                    log.error(KEY_BUNDLE, ex);
                                }
                            }
                        }                                               
                    }
                    
                // Sleep 1 second    
                Thread.sleep((1 * 1000));    

                } catch (InterruptedException ex) {
                    log.error(KEY_BUNDLE, ex);
                }
            }
            
            // desactive alert
            AlertManagerModel am = AlertManagerModel.getAlertManagerModel();
            am.setCheckAlert(AlertManagerModel.KeyChecks.KEY_FODER_SYNCHRONIZE_RUNNING, Boolean.FALSE);

            log.info(KEY_BUNDLE + " Stopping synchronized repository doWork.");
        }
    }
    
    
    class DoWorkDocuments extends Thread {

        private boolean stop;

        public DoWorkDocuments() {
            super("DoWorkDocuments-Repository");
            stop = false;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        @Override
        public void run() {
            log.info(KEY_BUNDLE + " Starting synchronized repository doWork.");                        
                        
            while (!stop) {
                Set<String> ids = new HashSet<String>();
                
                synchronized (dworkersLock) { for(String id : documentsWorkers.keySet()) { ids.add(id); } }
                try {                    

                    if (ids.isEmpty()) {
                        stop = true;
                    } else {
                        for (String id : ids) {
                            if (((SynchronizeWorker) documentsWorkers.get(id)).getState() == Thread.State.TERMINATED) {
                           
                                SynchronizedFolder fs = SynchronizedFolder.getDocumentsSynchronizedFolder(Constants.REPOSITORY_SYNCHRONIZED_DOCUMENTS_NAME, getPath(), null);
                                SynchronizedObject sobj = fs.getSynchronizeObject(id);
                                getSyncronizedDocuments().add(sobj);
                                
                                synchronized (dworkersLock) { documentsWorkers.remove(id); }
                                
                                // Show message information
                                Communication.showMessage(null, sobj.getName() + " add to synchronized documents.");
                            }
                        }                                               
                    }
                    
                // Sleep 1 second    
                Thread.sleep((1 * 1000));    

                } catch (InterruptedException ex) {
                    log.error(KEY_BUNDLE, ex);
                }
            }
            
            // desactive alert
            AlertManagerModel am = AlertManagerModel.getAlertManagerModel();
            am.setCheckAlert(AlertManagerModel.KeyChecks.KEY_DOCUMENT_SYNCHRONIZE_RUNNING, Boolean.FALSE);

            log.info(KEY_BUNDLE + " Stopping synchronized repository doWork.");
        }
    }        
    
}

package com.openkm.okmsynchronize.model;

import com.google.gson.Gson;
import com.openkm.okmsynchronize.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.model.SynchronizedFolder
 */
public class SynchronizedFolder {
    
    public final static String SYNCHRONIZE_CONTROL_FILE_NAME =  "sync.odesktop";
    
    private String folderName;
    private String virtualName;
    private String folderLocalPath;    
    private String folderUUID;
    private String folderServerPath;
    private List<SynchronizedObject> synchronizeObjects;
    private boolean conflicts;
    private boolean delete;
    
    private String restrictedExtensions;
    private String invalidCharacters;    

    private SynchronizedFolder(String folderName, String localPath, String restrictedExtensions ,String invalidCharacters) {
        this.folderName = folderName;
        this.virtualName = Utils.buildVirtualName(folderName);
        this.folderLocalPath = localPath;
        this.restrictedExtensions = restrictedExtensions;
        this.invalidCharacters = invalidCharacters;     
        this.conflicts = false;        
        this.synchronizeObjects = new ArrayList<SynchronizedObject>();
        
        readSynchronizeControlFile();       
        scanFolder(new File(localPath), true);
    }
    
    /**
     * Constructor usat (*)
     */
    public SynchronizedFolder(String folderName, String virtualName, String localPath, String uuid, String serverPath, List<SynchronizedObject> synchronizeObjects, String restrictedExtensions ,String invalidCharacters) {
        this.folderName = folderName;
        this.virtualName = virtualName;
        this.folderLocalPath = localPath;
        this.folderUUID = uuid;
        this.folderServerPath = serverPath;
        this.restrictedExtensions = restrictedExtensions;
        this.invalidCharacters = invalidCharacters;     
        this.conflicts = false;        
        this.synchronizeObjects = synchronizeObjects;        
    }
    
    
    /**
     * Constructor usat per creat noves carpetes despres de baixar-les (*)
     */
    public SynchronizedFolder(String name, String localBasePath) {                
        
        this.folderName = name;        
        this.folderLocalPath = Utils.buildLocalFilePath(localBasePath, this.folderName);        
        this.conflicts = false;
        readSynchronizeControlFile();        
        this.virtualName = Utils.getName(this.folderServerPath);
    }
    
    public static SynchronizedFolder getDocumentsSynchronizedFolder(String name, String localBasePath, List<SynchronizedObject> documents) {
        SynchronizedFolder sf = new SynchronizedFolder(name, localBasePath);
        if(documents != null) { sf.synchronizeObjects = documents; } 
        return sf;
    }
    
    
    public static SynchronizedFolder loadSynchronizeFolder(String name, String localPath, String restrictedExtensions, String invalidCharacters) {
        SynchronizedFolder nfld = new SynchronizedFolder(name, localPath, restrictedExtensions, invalidCharacters);
        
        return nfld;
    }
    
    public SynchronizedObject getSynchronizeObject(String search) {
        if (synchronizeObjects != null & !synchronizeObjects.isEmpty()) {
            for (SynchronizedObject sobj : synchronizeObjects) {
                if(sobj.contains(search)) {
                    return sobj;
                }
            }
            return null;
        } else {
            return null;
        }
    }        
    
    public static boolean isSynchronizedFolder(File f) {
        if(!f.isDirectory()) { return false; }
        else {
            String fileControlPath = Utils.buildLocalFilePath(f.getPath(), SYNCHRONIZE_CONTROL_FILE_NAME);
            File fc = new File(fileControlPath);
            if(!fc.exists() || !fc.isFile()) { return false; }
            else { return true; }
        }
    }
    
    public void persistSynchronizedObjects() { writeSynchronizeControlFile(); }

    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }

    public String getFolderLocalPath() { return folderLocalPath; }
    public void setFolderLocalPath(String folderLocalPath) { this.folderLocalPath = folderLocalPath; }

    public String getVirtualName() { return virtualName; }
        
    public String getFolderServerPath() { return folderServerPath; }

	public String getFolderUUID() { return folderUUID; }    
    
    public void setDelete(boolean delete) { this.delete = delete; }
    public boolean isDelete() { return delete; }
    
    public List<SynchronizedObject> getSynchronizeObjectsByState(StateSynchronizeObject state) {        
        List<SynchronizedObject> list = new ArrayList<SynchronizedObject>();
        
        for(SynchronizedObject sobj : synchronizeObjects) {
            if(state.equals(sobj.getState())) {
                list.add(sobj);
            }
        }
        
        return list;
    }
        
    public List<SynchronizedObject> getSynchronizeObjectsByRelativePath(String rpath, String type) {
        List<SynchronizedObject> list = new ArrayList<SynchronizedObject>();
        for(SynchronizedObject sobj : synchronizeObjects ){
            if(rpath.equals(sobj.getRelativePath()) && (("folder".equals(type) && sobj.isFolder()) ||
                                                        ("document".equals(type) && !sobj.isFolder()) ||
                                                        ("all".equals(type)))) {
                list.add(sobj);
            }
        }
               
        return list;
    }
    
    public List<SynchronizedObject> getSynchronizeObjects() { return synchronizeObjects; }
    
    public List<SynchronizedObject> getSynchronizeObjectsByType(String type) { 
        List<SynchronizedObject> list = new ArrayList<SynchronizedObject>();
        for(SynchronizedObject sobj : synchronizeObjects) {
            if(sobj.isFolder() && "folder".equals(type)) {
                list.add(sobj);
            } else if(!sobj.isFolder() && "document".equals(type)) {
                list.add(sobj);
            }
        }
        return list; 
    }
    
    public void refreshSynchronizeFolder() {
        File root = new File(this.folderLocalPath);
        if(root.exists() && root.isDirectory()) {
            scanFolder(root, false);         
        } else {
            delete = true;
        }
        for(SynchronizedObject sobj : synchronizeObjects) {
            File f = new File(sobj.getLocalPath());
            if(!StateSynchronizeObject.CONFLICT.equals(sobj.getState()) && !f.exists()) {
                sobj.setState(StateSynchronizeObject.DELETE);
            }
        }
    }
    
    public void deleteSynchronizeFileControl() {
        File scf = new File(Utils.buildLocalFilePath(folderLocalPath, SYNCHRONIZE_CONTROL_FILE_NAME));
        scf.delete();
    }
    
    public void deleteSynchronizeFolder() {
        deleteDirectory(new File(folderLocalPath));
    }
    
    private void deleteDirectory(File d) {
        for (File f : d.listFiles()) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        d.delete();
    }
    
    private void scanFolder(File root, boolean initial) {
        SynchronizeLock lock = SynchronizeLock.getSynchronizeLock();

        // comprovam si està bloquejat per rename
        if (!lock.isLock(root.toPath())) { //Si no està bloqueado 

            SynchronizeObjectFileFilter ff = new SynchronizeObjectFileFilter(restrictedExtensions, invalidCharacters, SYNCHRONIZE_CONTROL_FILE_NAME);
            for (File f : root.listFiles(ff)) {
                SynchronizedObject sobj = getSynchronizeObject(f.getPath());
                if (sobj != null) {
                    sobj.update(f.lastModified());
                } else {
                    if (initial) {
                        sobj = new SynchronizedObject(f.getName(), f.getPath(), Utils.getRelativePath(f.getPath()), f.lastModified(), f.isDirectory());
                        sobj.setState(StateSynchronizeObject.CONFLICT);
                        sobj.setConflictType(ConflictType.RENAME_STOP);
                    } else {
                        sobj = new SynchronizedObject(f.getName(), f.getPath(), Utils.getRelativePath(f.getPath()), f.lastModified(), f.isDirectory());
                    }
                    synchronizeObjects.add(sobj);
                }
                if (f.isDirectory()) {
                    scanFolder(f, initial);
                }
            }
        } else {
            System.out.println("[abb].................................................. lock:" + root.getPath());
        }
    }
    
    public void purgeSynchronizeObjects() {
        List<SynchronizedObject> deleteSynchronizeObjects = new ArrayList<SynchronizedObject>();
        for (SynchronizedObject sobj : synchronizeObjects) {
            if (StateSynchronizeObject.DELETE.equals(sobj.getState())) {
                deleteSynchronizeObjects.add(sobj);
            }
        }

        if (!deleteSynchronizeObjects.isEmpty()) {
            synchronizeObjects.removeAll(deleteSynchronizeObjects);
        }
    }
    
    private void readSynchronizeControlFile() {
        synchronizeObjects = new ArrayList<SynchronizedObject>();
        conflicts = false;

        File scf = new File(Utils.buildLocalFilePath(folderLocalPath, SYNCHRONIZE_CONTROL_FILE_NAME));
        Gson gson = new Gson();

        FileReader fr = null;

        try {
            fr = new FileReader(scf);
            BufferedReader br = new BufferedReader(fr);
            String linia;
            while ((linia = br.readLine()) != null) {
                SynchronizedObject obj = gson.fromJson(linia, SynchronizedObject.class);
                if(obj.isRootFolder()) {
                    folderUUID = obj.getUuid();
                    folderServerPath = obj.getServerPath();
                } else {
                    synchronizeObjects.add(obj);                   
                    if(!obj.toFile().exists()) {
                        obj.setState(StateSynchronizeObject.CONFLICT);
                        obj.setConflictType(ConflictType.DELETE_STOP);                    
                    }
                    conflicts = conflicts || StateSynchronizeObject.CONFLICT.equals(obj.getState());
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    
    private void writeSynchronizeControlFile() {
    FileWriter fw = null;
    File scf = new File(Utils.buildLocalFilePath(folderLocalPath, SYNCHRONIZE_CONTROL_FILE_NAME));    
    Gson gson = new Gson();
        try {
            if(scf.exists() && scf.isFile()) { scf.delete(); } 
            
            fw = new FileWriter(scf);
            PrintWriter pw = new PrintWriter(fw);
            for (SynchronizedObject sobj : synchronizeObjects) { 
                if(!StateSynchronizeObject.DELETE.equals(sobj.getState())) {
                    pw.println(gson.toJson(sobj));              
                }
            }                 
            
            // Adding rootFolder control object
            SynchronizedObject rootFolder = new SynchronizedObject(folderName, folderLocalPath, folderUUID, folderServerPath);
            pw.println(gson.toJson(rootFolder));   
            
            pw.flush();
                       
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException ex) {

                }
            }
        }
    }                       
    
}

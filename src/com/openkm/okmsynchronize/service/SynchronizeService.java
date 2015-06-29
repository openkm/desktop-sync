/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.service;

import com.openkm.okmsynchronize.model.AlertManagerModel;
import com.openkm.okmsynchronize.model.ConflictType;
import com.openkm.okmsynchronize.model.StateSynchronizeObject;
import com.openkm.okmsynchronize.model.SynchronizeAlert;
import com.openkm.okmsynchronize.model.SynchronizeLock;
import com.openkm.okmsynchronize.model.SynchronizedFolder;
import com.openkm.okmsynchronize.model.SynchronizedObject;
import com.openkm.okmsynchronize.model.SynchronizedObjectConflict;
import com.openkm.okmsynchronize.model.SynchronizedRepository;
import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.view.Communication;
import com.openkm.okmsynchronize.ws.OpenKMWS;
import com.openkm.sdk4j.bean.Document;
import com.openkm.sdk4j.bean.Folder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 *
 * @author abujosa
 */
public class SynchronizeService {
    
    private final static String KEY_BUNDLE = SynchronizeService.class.getName();   
    
    private enum ResultType {

        MODIFY_SERVER("Modified documents on the server: ")
      , MODIFY_REPOSITORY("Modified documents on the repository: ")
      , ADD_SERVER("Added documents on the server: ")
      , ADD_REPOSITORY("Added documents on the repository: ")
      , DELETE_SERVER("Removed documents on the server: ")
      , DELETE_REPOSITORY("Removed documents on the repository: ")
      , CONFLICT_SERVER("Conflic documents on the server: ")
      , CONFLICT_REPOSITORY("Conflic documents on the repository: ");
        
        private String informationText;
        
        private ResultType(String informationText) {
            this.informationText = informationText;
        }
        
        public String getId() {
            return name();
        }        

        public String getInformationText() {
            return informationText;
        }
        
        public static List<ResultType> getResultTypes() {
            List<ResultType> list = new ArrayList<ResultType>();
            
            list.add(MODIFY_SERVER);
            list.add(MODIFY_REPOSITORY);
            list.add(ADD_SERVER);
            list.add(ADD_REPOSITORY);
            list.add(DELETE_SERVER);            
            list.add(DELETE_REPOSITORY);
            list.add(CONFLICT_SERVER);
            list.add(CONFLICT_REPOSITORY);
            
            return list;
        }
    }
    
    private SynchronizeLog log;
    private OpenKMWS ws;
    private List<String> synchronizeds;
    private AlertManagerModel alertManager;
    
    private Map<ResultType, Integer> resultAction;        
    
    private SynchronizeService(OpenKMWS ws, SynchronizeLog log, AlertManagerModel alertManager) {
        this.ws = ws;
        this.log = log;
        this.alertManager = alertManager;
    }
    
    public static SynchronizeService getInstance(OpenKMWS ws, SynchronizeLog log) {
        SynchronizeService service = new SynchronizeService(ws, log, null);
        return service;     
    }
    
    public static SynchronizeService getInstance(OpenKMWS ws, SynchronizeLog log, AlertManagerModel alertManager) {        
        SynchronizeService service = new SynchronizeService(ws, log, alertManager);
        return service;        
    }    
    
    public void synchronizeDocuments(List<SynchronizedObject> listDocuments) {
        log.debug(KEY_BUNDLE + "method:synchronizeDocuments [listDocuments]");

        try {
            
            // Initialize actionResult
            initializeResultAction();

            // Get start date action
            Date startDate = new Date();
            
            for (SynchronizedObject sobj : listDocuments) {

                File f = new File(sobj.getLocalPath());
                if(!ws.exists(sobj.getServerPath(), "document") && f.exists()) {
                    f.delete();
                    sobj.setState(StateSynchronizeObject.DELETE);
                } else if(ws.exists(sobj.getServerPath(), "document") && !f.exists()) { 
                    Document doc = ws.getDocument(sobj.getUuid());
                    createDocument(f, ws.getContentDocument(doc.getUuid()));
                    sobj.setLocalTime(f.lastModified());
                    sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                    sobj.setVersion(doc.getActualVersion().getName());                    
                } else if (f.exists() && ws.exists(sobj.getServerPath(), "document")) {
                    Document doc = ws.getDocument(sobj.getUuid());
                    synchronizeObject(sobj, doc);                   
                }
            }
            
            // Get end date action
            Date endDate = new Date();

            // Summary information
            generateSummaryInformatio("Synchronize documents", startDate, endDate);
            
        } catch (SynchronizeException se) {
            log.error(KEY_BUNDLE, se);
        } catch (FileNotFoundException ex) {
            log.error(KEY_BUNDLE, ex);
        }
    }

    public void synchronizeFolder(SynchronizedFolder folder) {
        log.debug(KEY_BUNDLE + "method:synchronizeFolder [SynchronizeFolder=" + folder.getFolderUUID() + "]");
        
        synchronizeds = new ArrayList<String>();
        try {

            // Initialize actionResult
            initializeResultAction();

            // Get start date action
            Date startDate = new Date();
            
            Folder root = ws.getFodler(folder.getFolderUUID());
            
            synchronizeServerFolder(folder, folder.getFolderUUID(), folder.getFolderLocalPath());
            synchronizeLocalFolder(folder, folder.getFolderLocalPath(), root.getPath());            

            // Get end date action
            Date endDate = new Date();

            // Summary information
            generateSummaryInformatio("Synchronize folder", startDate, endDate);
            
        } catch (FileNotFoundException ex) {
            log.error(KEY_BUNDLE, ex);
        } catch (SynchronizeException e) {
            log.error(KEY_BUNDLE, e);
        }
    }
    
    public int updateFromRepository(SynchronizedObject sobj) {
        log.debug(KEY_BUNDLE + " method:updateFromRepository [sobj=" + sobj.toString() + "]");
        
        if (ws == null || !ws.isConnectionSuccessful()) {
            Communication.showMessage(null, "Could not connect to server. Conflict can not be resolve.");
            return 4;
        } else {
            try {
                Document doc = ws.getDocument(sobj.getUuid());
                File f = sobj.toFile();
                if (doc.isCheckedOut() || doc.isLocked()) {
                    Communication.showMessage(null, "Server document is locked or edit. Conflict can not be resolve.");
                    return 4;
                } else if(!canWriteDocument(doc.getPermissions())) {
                    Communication.showMessage(null, "User dont have permission for write document on the server. Conflict can not be resolve.");
                    return 4;
                } else {
                    ws.setContentDocument(sobj.getUuid(), new FileInputStream(f), "Modified by Synchronize OpenKM Desktop");
                    sobj.setLocalTime(f.lastModified());
                    sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                    sobj.setVersion(doc.getActualVersion().getName());
                    sobj.setState(StateSynchronizeObject.UPDATE);
                    sobj.setConflictMessage("");
                    return 0;
                }
            } catch (SynchronizeException ex) {
                log.error(KEY_BUNDLE, ex);
                return 1;
            } catch (FileNotFoundException ex) {
                log.error(KEY_BUNDLE, ex);
                return 1;
            }
        }
    }
    
    public int updateFromServer(SynchronizedObject sobj) {
        log.debug(KEY_BUNDLE + " method:updateFromServer [sobj=" + sobj.toString() + "]");

        if (ws == null || !ws.isConnectionSuccessful()) {
            Communication.showMessage(null, "Could not connect to server. Conflict can not be resolve.");
            return 4;
        } else {
            try {
                Document doc = ws.getDocument(sobj.getUuid());
                File f = sobj.toFile();
                createDocument(f, ws.getContentDocument(sobj.getUuid()));
                sobj.setLocalTime(f.lastModified());
                sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                sobj.setVersion(doc.getActualVersion().getName());
                sobj.setState(StateSynchronizeObject.UPDATE);
                return 0;
            } catch (SynchronizeException ex) {
                log.error(KEY_BUNDLE, ex);
                return 1;
            }
        }
    }
    
	public void renameSynchronizedObject(SynchronizedObject o, Path destination, SynchronizedRepository repository) {
		log.debug(KEY_BUNDLE + " method:renameSynchronizedObject [o=" + o.toString() + ",destination:" + destination.toString() + "]");

		String newName = destination.getFileName().toString();
		if (!o.isFolder()) {
			// Rename document
			renameDocument(o, newName);
		} else {
			// rename fodler
			String oPath = o.getLocalPath();
			renameFolder(o, newName);
			for (SynchronizedObject c : repository.getAllSynchronizeObjects()) {
				Path cPath = FileSystems.getDefault().getPath(c.getLocalPath());
				if (cPath.startsWith(oPath)) {
					c.setLocalPath(Utils.replaceAllString(c.getLocalPath(),
							oPath, destination.toString()));
					c.setRelativePath(Utils.getRelativePath(c.getLocalPath()));
					c.setState(o.getState());
				}
			}
		}
	}
    
    private void renameFolder(SynchronizedObject o, String newName) {
        log.debug(KEY_BUNDLE + " method:renameFolder [o=" + o.toString() + ",newName:" + newName + "]");
        
        try {
            if (ws == null || !ws.isConnectionSuccessful()) {
                o.setState(StateSynchronizeObject.CONFLICT);
                o.setConflictType(ConflictType.NO_CONNECTION);
                o.rename(newName);
            } else {
                if(o.getUuid() == null) {
                    o.setState(StateSynchronizeObject.NEW);                    
                    o.rename(newName);
                } else if (ws.isFolder(o.getUuid())) {
                    Folder fol = ws.getFodler(o.getUuid());
                    if (!canWriteDocument(fol.getPermissions())) {
                        o.setState(StateSynchronizeObject.CONFLICT);
                        o.setConflictType(ConflictType.RENAME_PERMISSIONS);
                        o.rename(newName);
                    } else {
                        ws.renameFolder(o.getUuid(), newName);
                        fol = ws.getFodler(o.getUuid());
                        o.setServerPath(fol.getPath());
                        o.rename(newName);
                        o.setState(StateSynchronizeObject.RENAMED);
                    }
                } else {
                    o.setState(StateSynchronizeObject.CONFLICT);
                    o.setConflictType(ConflictType.RENAME_NOT_EXISTS);
                    o.rename(newName);
                }
            }
            
        } catch (SynchronizeException ex) {
            log.error(KEY_BUNDLE, ex);
        } catch (IOException ioe) {
            log.error(KEY_BUNDLE, ioe);
        }       
    }
    
    private void renameDocument(SynchronizedObject o, String newName) {
        log.debug(KEY_BUNDLE + " method:renameDocument [o=" + o.toString() + ",newName:" + newName + "]");

        try {
            if (ws == null || !ws.isConnectionSuccessful()) {
                o.setState(StateSynchronizeObject.CONFLICT);
                o.setConflictType(ConflictType.NO_CONNECTION);
                o.rename(newName);
            } else {
                if(o.getUuid() == null) {
                    o.setState(StateSynchronizeObject.NEW);                    
                    o.rename(newName);
                } else if(ws.isDocument(o.getUuid())) {
                    Document doc = ws.getDocument(o.getUuid());
                    if (!canWriteDocument(doc.getPermissions())) {
                        o.setState(StateSynchronizeObject.CONFLICT);
                        o.setConflictType(ConflictType.RENAME_PERMISSIONS);
                        o.rename(newName);
                    } else {
                        ws.renameDocument(o.getUuid(), newName);
                        doc = ws.getDocument(o.getUuid());
                        o.setServerPath(doc.getPath());
                        o.setState(StateSynchronizeObject.RENAMED);
                        o.rename(newName);
                    }
                } else {
                    o.setState(StateSynchronizeObject.CONFLICT);
                    o.setConflictType(ConflictType.RENAME_NOT_EXISTS);
                    o.rename(newName);
                }
            }
        } catch (SynchronizeException ex) {
            log.error(KEY_BUNDLE, ex);
        } catch (IOException ioe) {
            log.error(KEY_BUNDLE, ioe);
        }
    }  
        
    public int renameDocument(SynchronizedObject sobj, SynchronizedObjectConflict renamedDocument) {
        log.debug(KEY_BUNDLE + " method:renameDocument [sobj=" + sobj.toString() + ",renamedocument:" + renamedDocument + "]");
        
        if (ws == null || !ws.isConnectionSuccessful()) {
            Communication.showMessage(null, "Could not connect to server. Conflict can not be resolve.");
            return 4;
        } else {
            try {
                if (ws.isDocument(renamedDocument.getUuid())) {
                    Document doc = ws.getDocument(renamedDocument.getUuid());
                    if (doc.isCheckedOut() || doc.isLocked()) {
                        Communication.showMessage(null, "Document " + renamedDocument.toString() + " is locked or checkout on the server. Conflict can not be resolve.");
                        return 4;
                    } else if(!canWriteDocument(doc.getPermissions())) {
                        Communication.showMessage(null, "User dont have permission for write document on the server. Conflict can not be resolve.");
                        return 4;
                    } else {
                        ws.renameDocument(renamedDocument.getUuid(), sobj.getName());
                        File f = sobj.toFile();
                        sobj.setLocalTime(f.lastModified());
                        doc = ws.getDocument(renamedDocument.getUuid());
                        sobj.setUuid(doc.getUuid());
                        sobj.setServerPath(doc.getPath());
                        sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                        sobj.setVersion(doc.getActualVersion().getName());
                        sobj.setState(StateSynchronizeObject.UPDATE);
                    }
                } else {
                    Communication.showMessage(null, "Document " + renamedDocument.toString() + " not exists. Conflict can not be resolve.");
                    return 4;
                }
            } catch (SynchronizeException ex) {
                log.error(KEY_BUNDLE, ex);
                return 1;
            }
        }

        return 0;
    }
    
    public int renameDocumentAndUpdate(SynchronizedObject sobj, SynchronizedObjectConflict renamedDocument) {
        log.debug(KEY_BUNDLE + " method:renameDocumentAndUpdate [sobj=" + sobj.toString() + ",renamedocument:" + renamedDocument + "]");

        if (ws == null || !ws.isConnectionSuccessful()) {
            Communication.showMessage(null, "Could not connect to server. Conflict can not be resolve.");
            return 4;
        } else {
            try {
                if (ws.isDocument(renamedDocument.getUuid())) {

                    Document doc = ws.getDocument(renamedDocument.getUuid());
                    if (doc.isCheckedOut() || doc.isLocked()) {
                        Communication.showMessage(null, "Document " + renamedDocument.toString() + " is locked or checkout on the server. Conflict can not be resolve.");
                        return 4;
                    } else if (!renamedDocument.getVersion().equals(doc.getActualVersion().getName())) {
                        Communication.showMessage(null, "Document " + renamedDocument.toString() + " is modifyed on the server. Conflict can not be resolve.");
                        return 4;
                    } else if(!canWriteDocument(doc.getPermissions())) {
                        Communication.showMessage(null, "User dont have permission for write document on the server. Conflict can not be resolve.");
                        return 4;
                    } else {
                        File f = sobj.toFile();
                        ws.renameDocument(renamedDocument.getUuid(), sobj.getName());                        
                        ws.setContentDocument(renamedDocument.getUuid(), new FileInputStream(f), "Modified by Synchronize OpenKM Desktop");
                        doc = ws.getDocument(renamedDocument.getUuid());
                        sobj.setLocalTime(f.lastModified());
                        sobj.setUuid(doc.getUuid());
                        sobj.setServerPath(doc.getPath());
                        sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                        sobj.setVersion(doc.getActualVersion().getName());
                        sobj.setState(StateSynchronizeObject.UPDATE);
                    }

                } else {
                    Communication.showMessage(null, "Document " + renamedDocument.toString() + " not exists. Conflict can not be resolve.");
                    return 4;
                }
            } catch (SynchronizeException ex) {
                log.error(KEY_BUNDLE, ex);
                return 1;
            } catch (FileNotFoundException ex) {
                log.error(KEY_BUNDLE, ex);
                return 1;
            }
        }

        return 0;
    }

    /**
     * Private Method
     */
    private void synchronizeServerFolder(SynchronizedFolder folder, String uuid, String localFolderPath) throws SynchronizeException, FileNotFoundException {
        log.debug(KEY_BUNDLE + " method:synchronizeServerFolder [SynchronizeFolder=" + folder.getFolderUUID() + "uuid=" + uuid + ",localFolderPath=" + localFolderPath + "]");
        
        SynchronizeLock lock = SynchronizeLock.getSynchronizeLock();

        //synchronize documents
        for (Document doc : ws.listDocuments(uuid)) {
            String path = Utils.buildLocalFilePath(localFolderPath, Utils.getName(doc.getPath()));
            log.debug(KEY_BUNDLE + " Searching object:" + path);
            SynchronizedObject sobj = folder.getSynchronizeObject(path);
            if (sobj != null) {
                log.debug(KEY_BUNDLE + " Found object:" + path);
                synchronizeObject(sobj, doc);
                synchronizeds.add(sobj.getKey());
            } else if(!lock.haveBlockedPath()) {
                // Donwload document to repository
                log.debug(KEY_BUNDLE + " Object Not Found:" + path);
                path = Utils.buildLocalFilePath(localFolderPath, Utils.getName(doc.getPath()));
                File f = new File(path);
                createDocument(f, ws.getContentDocument(doc.getUuid()));
                String relativePath = Utils.getRelativePath(localFolderPath);
                sobj = new SynchronizedObject(Utils.getName(doc.getPath()), path, relativePath, doc.getUuid(), doc.getPath(), doc.getLastModified().getTimeInMillis(), doc.getActualVersion().getName(), Boolean.FALSE);
                folder.getSynchronizeObjects().add(sobj);
                synchronizeds.add(sobj.getKey());
            }            
        }

        //synchronize folders
        for (Folder fld : ws.listFolders(uuid)) {
            String path = Utils.buildLocalFilePath(localFolderPath, Utils.getName(fld.getPath()));
            log.debug(KEY_BUNDLE + " Searching object:" + path);
            SynchronizedObject sobj = folder.getSynchronizeObject(path);
            if (sobj != null && StateSynchronizeObject.DELETE.equals(sobj.getState())) {
                log.debug(KEY_BUNDLE + " Found object:" + path);
                ws.deleteFolder(fld.getUuid());          
            } else if (((sobj != null && StateSynchronizeObject.NEW.equals(sobj.getState())) || sobj == null) && !lock.haveBlockedPath()) {    
                log.debug(KEY_BUNDLE + " Found object:" + path);
                File dir = new File(path);
                if (!dir.exists()) {
                    createFolder(path);
                }
                if(sobj != null) {
                    sobj.setState(StateSynchronizeObject.UPDATE);
                } else {
                    sobj = new SynchronizedObject(dir.getName(), dir.getPath(), Utils.getRelativePath(dir.getPath()), dir.lastModified(), true);
                    sobj.setState(StateSynchronizeObject.UPDATE);
                    folder.getSynchronizeObjects().add(sobj);
                }
                
                synchronizeServerFolder(folder, fld.getUuid(), path);
             } else if (sobj != null && StateSynchronizeObject.RENAMED.equals(sobj.getState())) {
                 log.debug(KEY_BUNDLE + " Found object:" + path);
                 sobj.setServerPath(fld.getPath());
                 sobj.setState(StateSynchronizeObject.UPDATE);
                 synchronizeServerFolder(folder, fld.getUuid(), path);
             } else if (sobj != null && StateSynchronizeObject.UPDATE.equals(sobj.getState())) { 
                 log.debug(KEY_BUNDLE + " Found object:" + path);
                 synchronizeServerFolder(folder, fld.getUuid(), path);
             }            
        }
    }
    
    private void synchronizeLocalFolder(SynchronizedFolder folder, String localPath, String serverPath) throws SynchronizeException, FileNotFoundException {
        log.debug(KEY_BUNDLE + "method:synchronizeLocalFolder [SynchronizeFolder=" + folder.getFolderUUID() + "localPath=" + localPath + ",serverPath=" + serverPath + "]");

        File directory = new File(localPath);
        if (directory.exists()) {            
            for (File f : directory.listFiles()) {
                log.debug(KEY_BUNDLE + " Searching object:" + f.getPath());
                SynchronizedObject sobj = folder.getSynchronizeObject(f.getPath());
                if (sobj != null && !synchronizeds.contains(sobj.getKey())) {
                    log.debug(KEY_BUNDLE + " Found object:" + f.getPath());
                    if (sobj.isFolder() && StateSynchronizeObject.NEW.equals(sobj.getState())) {
                        if (!ws.exists(Utils.buildOpenkmDocumentPath(serverPath, f.getName()), "folder")) {
                            ws.createFolder(Utils.buildOpenkmDocumentPath(serverPath, f.getName()));
                            Folder fld = ws.getFodler(Utils.buildOpenkmDocumentPath(serverPath, f.getName()));
                            sobj.setUuid(fld.getUuid());
                            sobj.setServerPath(fld.getPath());
                            sobj.setState(StateSynchronizeObject.UPDATE);
                        }
                        synchronizeLocalFolder(folder, f.getPath(), Utils.buildOpenkmDocumentPath(serverPath, f.getName()));
                    } else if (sobj.isFolder() && StateSynchronizeObject.UPDATE.equals(sobj.getState())) {
                        //s'ha de borrar la carpeta en local
                        if (!ws.exists(Utils.buildOpenkmDocumentPath(serverPath, f.getName()), "folder")) {
                            synchronizeLocalFolder(folder, f.getPath(), Utils.buildOpenkmDocumentPath(serverPath, f.getName()));
                            sobj.setState(StateSynchronizeObject.DELETE);
                            f.delete();
                            log.info(KEY_BUNDLE + " The folder has been removed from localy: " + sobj.getRelativePath());
                        } else {
                            synchronizeLocalFolder(folder, f.getPath(), Utils.buildOpenkmDocumentPath(serverPath, f.getName()));
                        }
                    } else if (!sobj.isFolder()) {
                        synchronizeObject(sobj, Utils.buildOpenkmDocumentPath(serverPath, f.getName()));
                        sobj.setRelativePath(Utils.getRelativePath(localPath));
                        synchronizeds.add(sobj.getKey());
                    }
                } else {
                    log.debug(KEY_BUNDLE + " Object Not Found:" + f.getPath());
                }
            }
        } else {
            log.debug(KEY_BUNDLE + " Local directory not exists: " + localPath);
        }
    }
    
    private void synchronizeObject(SynchronizedObject sobj, String docPath) throws SynchronizeException, FileNotFoundException {
        log.debug(KEY_BUNDLE + "method:synchronizeObject [sobj=" + sobj.getUuid() + "docPath=" + docPath + "]");
        
        ResultType rt = null;
        Document doc = null;
        
        log.debug(KEY_BUNDLE + " Exists node " + docPath + "? " + ws.exists(docPath, "document"));
        
        if (ws.exists(docPath, "document")) {
            doc = ws.getDocument(docPath);
        }
        
        switch (sobj.getState()) {            
            case NEW:
                if (doc == null) {
                    File f = new File(sobj.getLocalPath());
                    ws.setContentDocument(docPath, new FileInputStream(f), "Created by Synchronize OpenKM Desktop");
                    doc = ws.getDocument(docPath);
                    sobj.setServerPath(docPath);
                    sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                    sobj.setVersion(doc.getActualVersion().getName());
                    sobj.setState(StateSynchronizeObject.UPDATE);
                    sobj.setUuid(doc.getUuid());
                    
                    sobj.setRootFolder(Boolean.FALSE);
                    rt = ResultType.MODIFY_SERVER;
                    
                    log.info(KEY_BUNDLE + " The document has been updated from server: " + doc.getUuid());
                } else {
                    sobj.setState(StateSynchronizeObject.CONFLICT); 
                    sobj.setConflictType(ConflictType.NEW_EXISTS);
                    rt = ResultType.CONFLICT_REPOSITORY;                    
                    log.info(KEY_BUNDLE + " The document already exists from server: " + sobj.getRelativePath());
                }
                break;
            case UPDATE:                
                if (doc == null) {
                    File f = new File(sobj.getLocalPath());
                    f.delete();
                    sobj.setState(StateSynchronizeObject.DELETE);
                    rt = ResultType.DELETE_REPOSITORY;                    
                    log.info(KEY_BUNDLE + " The document has been removed from localy: " + sobj.getRelativePath());
                }
                break;
            case MODIFIED:                
                if (doc == null) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);                    
                    sobj.setConflictType(ConflictType.MODIFY_NOT_EXISTS);
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document does not exist from server: " + sobj.getRelativePath());
                } else if (doc.isCheckedOut()) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);  
                    sobj.setConflictType(ConflictType.MODIFY_EDIT);
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document is checkout from server: " + doc.getUuid());
                } else if (doc.isLocked()) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);  
                    sobj.setConflictType(ConflictType.MODIFY_LOCK);
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document is locked from server: " + doc.getUuid());
                } else if (!sobj.getVersion().equals(doc.getActualVersion().getName())) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);
                    sobj.setConflictType(ConflictType.MODIFY_CHANGE);
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document has been modified from server: " + doc.getUuid());                   
                }else if (!canWriteDocument(doc.getPermissions())) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);
                    sobj.setConflictType(ConflictType.MODIFY_PERMISSIONS);                    
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " TUser dont have permission for write document on the server: " + doc.getUuid());  
                } else {
                    File f = new File(sobj.getLocalPath());
                    ws.setContentDocument(doc.getUuid(), new FileInputStream(f), "Modified by Synchronize OpenKM Desktop");
                    doc = ws.getDocument(doc.getUuid());
                    sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                    sobj.setVersion(doc.getActualVersion().getName());
                    sobj.setState(StateSynchronizeObject.UPDATE);
                    rt = ResultType.MODIFY_SERVER;
                    log.info(KEY_BUNDLE + " The document has been created from server: " + doc.getUuid());
                }
                break;
            case DELETE: 
                if (doc == null) {

                } else if (doc.isCheckedOut()) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);
                    sobj.setConflictType(ConflictType.DELETE_EDIT);
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document is locked or checkout from server: " + doc.getUuid());
                } else if (doc.isLocked()) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);
                    sobj.setConflictType(ConflictType.DELETE_LOCK);
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document is locked or checkout from server: " + doc.getUuid());
                } else if (!sobj.getVersion().equals(doc.getActualVersion().getName())) {
                    sobj.setVersion(doc.getActualVersion().getName());
                    sobj.setConflictType(ConflictType.DELETE_CHANGE);
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document has been modified from server: " + doc.getUuid());
                } else if (!canDeleteDocument(doc.getPermissions())) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);
                    sobj.setConflictType(ConflictType.DELETE_PERMISSIONS);
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " TUser dont have permission for delete document on the server: " + doc.getUuid());
                } else {
                    ws.deleteDocument(sobj.getUuid());
                    rt = ResultType.DELETE_SERVER;
                    log.info(KEY_BUNDLE + " The document has been deleted from server: " + sobj.getRelativePath());
                }
                break;
            case CONFLICT:                
                break;
        }

        // increment result
        if (rt != null) {
            incrementResultAction(rt);
        }
    }
    
    private void synchronizeObject(SynchronizedObject sobj, Document doc) throws SynchronizeException, FileNotFoundException {
        log.debug(KEY_BUNDLE + "method:synchronizeObject [sobj=" + sobj.getUuid() + "doc=" + doc.getUuid() + "]");        
        
        ResultType rt = null;
        switch (sobj.getState()) {            
            case NEW:
                sobj.setState(StateSynchronizeObject.CONFLICT);                
                sobj.setConflictType(ConflictType.NEW_EXISTS);
                rt = ResultType.CONFLICT_REPOSITORY;
                log.info(KEY_BUNDLE + " The document already exists from server: " + sobj.getRelativePath());
                break;
            case UPDATE:                
                if (!sobj.getVersion().equals(doc.getActualVersion().getName())) {
                    
                    File f = new File(sobj.getLocalPath());
                    createDocument(f, ws.getContentDocument(doc.getUuid()));
                    sobj.setLocalTime(f.lastModified());
                    sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                    sobj.setVersion(doc.getActualVersion().getName());                    
                    rt = ResultType.MODIFY_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document has been updated from localy: " + sobj.getRelativePath());
                }
                break;
            case MODIFIED:                     
                if (!sobj.getVersion().equals(doc.getActualVersion().getName())) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);
                    sobj.setConflictType(ConflictType.MODIFY_CHANGE);                    
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " The document has been modified from server: " + doc.getUuid());
                } else {
                    if (doc.isCheckedOut()) {
                        sobj.setState(StateSynchronizeObject.CONFLICT);                        
                        sobj.setConflictType(ConflictType.MODIFY_EDIT);          
                        rt = ResultType.CONFLICT_REPOSITORY;
                        log.info(KEY_BUNDLE + " The document is locked or checkout from server: " + doc.getUuid());
                    } else if(doc.isLocked()) {
                        sobj.setState(StateSynchronizeObject.CONFLICT);                        
                        sobj.setConflictType(ConflictType.MODIFY_LOCK);          
                        rt = ResultType.CONFLICT_REPOSITORY;
                        log.info(KEY_BUNDLE + " The document is locked or checkout from server: " + doc.getUuid());
                    } else if(!canWriteDocument(doc.getPermissions())) { 
                        sobj.setState(StateSynchronizeObject.CONFLICT);                        
                        sobj.setConflictType(ConflictType.MODIFY_PERMISSIONS);          
                        rt = ResultType.CONFLICT_REPOSITORY;
                        log.info(KEY_BUNDLE + " TUser dont have permission for write document on the server: " + doc.getUuid());                        
                    }else {
                        File f = new File(sobj.getLocalPath());
                        ws.setContentDocument(doc.getUuid(), new FileInputStream(f), "Modified by Synchronize OpenKM Desktop");
                        doc = ws.getDocument(doc.getUuid());
                        sobj.setServerTime(doc.getLastModified().getTimeInMillis());
                        sobj.setVersion(doc.getActualVersion().getName());
                        sobj.setState(StateSynchronizeObject.UPDATE);
                        rt = ResultType.MODIFY_SERVER;
                        log.info(KEY_BUNDLE + " The document has been updated from server: " + doc.getUuid());
                    }
                }
                break;
            case DELETE:                
                if (!sobj.getVersion().equals(doc.getActualVersion().getName())) {
                    sobj.setState(StateSynchronizeObject.CONFLICT);                
                    sobj.setConflictType(ConflictType.DELETE_CHANGE);    
                    rt = ResultType.CONFLICT_REPOSITORY;
                    log.info(KEY_BUNDLE + " he document has been modified from server: " + doc.getUuid());
                } else {
                    if (doc.isCheckedOut()) {
                        sobj.setState(StateSynchronizeObject.CONFLICT);                       
                        rt = ResultType.CONFLICT_REPOSITORY;
                        sobj.setConflictType(ConflictType.DELETE_EDIT);   
                        log.info(KEY_BUNDLE + " The document is checkout from server: " + doc.getUuid());
                    } else if(doc.isLocked()) {
                        sobj.setState(StateSynchronizeObject.CONFLICT);                       
                        rt = ResultType.CONFLICT_REPOSITORY;
                        sobj.setConflictType(ConflictType.DELETE_LOCK);   
                        log.info(KEY_BUNDLE + " The document is locked from server: " + doc.getUuid());
                    } else if(!canDeleteDocument(doc.getPermissions())) { 
                        sobj.setState(StateSynchronizeObject.CONFLICT);                       
                        sobj.setConflictType(ConflictType.DELETE_PERMISSIONS);   
                        rt = ResultType.CONFLICT_REPOSITORY;
                        log.info(KEY_BUNDLE + " TUser dont have permission for delete document on the server: " + doc.getUuid());                        
                    } else {
                        ws.deleteDocument(sobj.getUuid());
                        rt = ResultType.DELETE_REPOSITORY;
                        log.info(KEY_BUNDLE + " The document has been deleted from repository: " + sobj.getRelativePath());
                    }
                }
                break;
            case RENAMED :
                sobj.setServerPath(doc.getPath());
                sobj.setState(StateSynchronizeObject.UPDATE);
                log.info(KEY_BUNDLE + " The document has been renamed on the server: " + doc.getPath());
            case CONFLICT:                
                break;
        }
        // increment result
        if (rt != null) {
            incrementResultAction(rt);
        }
        
    }
    
    private void downloadFolder(String uuidServerFolder, String localFolderPath, String repositoryPath, List<SynchronizedObject> listSynchronizeObject) throws SynchronizeException {        
        log.debug(KEY_BUNDLE + "method:downloadFolder [uuidServerFolder=" + uuidServerFolder + "localFolderPath=" + localFolderPath + ",repositoryPath=" + repositoryPath + "]");
        
        downloadDocuments(uuidServerFolder, localFolderPath, repositoryPath, listSynchronizeObject);
        
        for (Folder fld : ws.listFolders(uuidServerFolder)) {
            String path = Utils.buildLocalFilePath(localFolderPath, Utils.getName(fld.getPath()));
            createFolder(path);
            downloadFolder(fld.getUuid(), path, repositoryPath, listSynchronizeObject);
            String relativePath = Utils.getRelativePath(localFolderPath);
            SynchronizedObject sobj = new SynchronizedObject(Utils.getName(fld.getPath()), path, relativePath, fld.getUuid(), fld.getPath(), fld.getCreated().getTimeInMillis(), null, Boolean.TRUE);
            listSynchronizeObject.add(sobj);
        }        
    }
    
    private void downloadDocuments(String uuidServerFolder, String localFolderPath, String repositoryPath, List<SynchronizedObject> listSynchronizeObject) throws SynchronizeException {        
        log.debug(KEY_BUNDLE + "method:downloadDocuments [uuidServerFolder=" + uuidServerFolder + "localFolderPath=" + localFolderPath + ",repositoryPath=" + repositoryPath + "]");
        
        for (Document doc : ws.listDocuments(uuidServerFolder)) {
            String path = Utils.buildLocalFilePath(localFolderPath, Utils.getName(doc.getPath()));
            File f = new File(path);
            createDocument(f, ws.getContentDocument(doc.getUuid()));
            String relativePath = Utils.getRelativePath(localFolderPath);
            SynchronizedObject sobj = new SynchronizedObject(Utils.getName(doc.getPath()), path, relativePath, doc.getUuid(), doc.getPath(), doc.getLastModified().getTimeInMillis(), doc.getActualVersion().getName(), Boolean.FALSE);
            listSynchronizeObject.add(sobj);

            // increment result
            incrementResultAction(ResultType.ADD_REPOSITORY);
        }        
    }
    
    private void createDocument(File f, byte[] content) throws SynchronizeException {        
        log.debug(KEY_BUNDLE + "method:createDocument [file=" + f.getPath() + "]");
        
        if (f.exists() && f.isFile()) {
            f.delete();
        }
        
        OutputStream out;
        try {
            out = new FileOutputStream(f);
            out.write(content);
            out.close();
        } catch (IOException e) {
            log.error(KEY_BUNDLE, e);
        }
    }
    
    private void createFolder(String path) throws SynchronizeException {
        log.debug(KEY_BUNDLE + "method:createcreateFolderDocument [path=" + path + "]");
        
        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            throw new SynchronizeException();
        }
        
        folder.mkdir();        
    }    
    
    private void generateSummaryInformatio(String action, Date startDate, Date endDate) {
        log.debug(KEY_BUNDLE + "method:generateSummaryInformatio [action=" + action + "]");
        
        StringBuilder summary = new StringBuilder();
        StringBuilder tittle = new StringBuilder();
        
        tittle.append(action).append(" summary").append("\n");
        
        summary.append(tittle);
        summary.append("------------------------------------").append("\n");
        summary.append("Start time:").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(startDate)).append("\n");
        summary.append("Completion time:").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(endDate)).append("\n");        
        summary.append(toStringResultAction());        
        summary.append("\n").append("For more information look at the application log.");
        
        log.debug(summary.toString());
        
        if(hasResultAction()) {            
            SynchronizeAlert alert = new SynchronizeAlert(tittle.toString(), summary.toString(), SynchronizeAlert.SynchronizeAlertType.NOTIF);
            alertManager.addAlert(alert);
        }
    }
    
    private void initializeResultAction() {
        log.debug(KEY_BUNDLE + "method:initializeResultAction []");
        
        resultAction = new HashMap<ResultType, Integer>();
        for (ResultType rt : ResultType.getResultTypes()) {
            resultAction.put(rt, new Integer(0));
        }
    }
    
    private void incrementResultAction(ResultType rt) {
        log.debug(KEY_BUNDLE + "method:incrementResultAction [ResultType=" + rt.getId() + "]");
        
        resultAction.put(rt, resultAction.get(rt) + 1);
    }
    
    private String toStringResultAction() {
        log.debug(KEY_BUNDLE + "method:toStringResultAction []");
        
        StringBuilder str = new StringBuilder();
        for (ResultType rt : ResultType.getResultTypes()) {
            str.append(rt.getInformationText()).append(resultAction.get(rt)).append("\n");
        }
        return str.toString();        
    }
    private boolean hasResultAction() {
        log.debug(KEY_BUNDLE + "method:hasResultAction []");
        
        for (ResultType rt : ResultType.getResultTypes()) {
            if(resultAction.get(rt) != 0) { return true; }
        }
        return false;        
    }
    
    private boolean canWriteDocument(int permissions) {
        return permissions == 31 || permissions == 23 || permissions == 19 || permissions == 27;
    }
    
    private boolean canDeleteDocument(int permissions) {
        return permissions == 31 || permissions == 23 || permissions == 29 || permissions == 21;
    }
    
    class LengthComparator implements Comparator<String> {
        
        @Override
        public int compare(String o1, String o2) {
            if (o1.length() < o2.length()) {
                return 1;
            } else if (o1.length() > o2.length()) {
                return -1;
            }
            return o1.compareTo(o2);
        }        
    }    
    
}

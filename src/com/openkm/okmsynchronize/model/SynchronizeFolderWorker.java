/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.ws.OpenKMWS;
import com.openkm.okmsynchronize.ws.OpenKMWSFactory;
import com.openkm.sdk4j.bean.Document;
import com.openkm.sdk4j.bean.Folder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author abujosa
 */

public class SynchronizeFolderWorker extends Thread implements SynchronizeWorker {
    
    protected static final String KEY_BUNDLE = SynchronizeFolderWorker.class.getName();
    
    private String uuid;
    private String path;
    private String folderName;
    private String folderServerPath;
    private ServerCredentials credentials;
    private SynchronizeLog log;
    private OpenKMWS ws;
    
    private String restrictedExtensions;
    private String invalidCharacters;   
    
    private boolean stop = false;

    public SynchronizeFolderWorker(String uuid, String path, String folderName, String folderServerPath, ServerCredentials credentials, SynchronizeLog log, String restrictedExtensions ,String invalidCharacters) {
        super(uuid);
        this.uuid = uuid;
        this.path = path;
        this.folderName = folderName;
        this.folderServerPath = folderServerPath;
        this.credentials = credentials;
        this.log = log;
        this.restrictedExtensions = restrictedExtensions;
        this.invalidCharacters = invalidCharacters;

        // Instanciam el WS
        this.ws = null;
        try {
            this.ws = OpenKMWSFactory.instance(credentials);
        } catch (SynchronizeException e) {
            log.error(KEY_BUNDLE, e);
        }
    }
    
    @Override
    public void run() {
        try {

            List<SynchronizedObject> listSynchronizeObject = new ArrayList<SynchronizedObject>();
            String realName = Utils.getRealName(folderName, path);
            String basePath = Utils.buildLocalFilePath(path, realName);
            createFolder(basePath);
            downloadFolder(uuid, basePath, path, listSynchronizeObject);
            
            Folder fld = ws.getFodler(uuid);
            
            SynchronizedFolder sf = new SynchronizedFolder(realName, folderName, basePath, uuid, fld.getPath(), listSynchronizeObject, restrictedExtensions, invalidCharacters);
            sf.persistSynchronizedObjects();

        } catch (SynchronizeException e) {
            log.error(KEY_BUNDLE, e);
        }
    }

    @Override
    public void stopWorker() {
        interrupt();       
    }
    
    private void createFolder(String path) throws SynchronizeException {
        log.debug(KEY_BUNDLE + "method:createcreateFolderDocument [path=" + path + "]");

        File folder = new File(path);
        if (folder.exists() && folder.isDirectory()) {
            throw new SynchronizeException("Folder can not create. Folder exists");
        }

        folder.mkdir();
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
        log.debug(KEY_BUNDLE + "method:downloadDocuments [uuidServerFolder=" + uuidServerFolder + "localFolderPath=" + localFolderPath + ",repositoryPath=" + repositoryPath + "]" );
        
        for(Document doc : ws.listDocuments(uuidServerFolder)) {
            String path = Utils.buildLocalFilePath(localFolderPath, Utils.getName(doc.getPath()));
            File f = new File(path);
            createDocument(f, ws.getContentDocument(doc.getUuid()));
            String relativePath = Utils.getRelativePath(localFolderPath);
            SynchronizedObject sobj = new SynchronizedObject(Utils.getName(doc.getPath()), path, relativePath, doc.getUuid(), doc.getPath(), doc.getLastModified().getTimeInMillis(), doc.getActualVersion().getName(), Boolean.FALSE);
            listSynchronizeObject.add(sobj);
        }                
    }
    
    private void createDocument(File f, byte[] content) throws SynchronizeException {        
        log.debug(KEY_BUNDLE + "method:createDocument [file=" + f.getPath() + "]" );
        
        if(f.exists() && f.isFile()) { f.delete(); }

        OutputStream out;
        try {
            out = new FileOutputStream(f);
            out.write(content);
            out.close();
        } catch (IOException e) {
             log.error(KEY_BUNDLE, e);
        }
    }
    
    
    
}

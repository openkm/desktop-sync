/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

import static com.openkm.okmsynchronize.model.SynchronizeFolderWorker.KEY_BUNDLE;
import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.okmsynchronize.utils.SynchronizeLog;
import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.ws.OpenKMWS;
import com.openkm.okmsynchronize.ws.OpenKMWSFactory;
import com.openkm.sdk4j.bean.Document;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author abujosa
 */
public class SynchronizeDocumentWorker extends Thread implements SynchronizeWorker {

    protected static final String KEY_BUNDLE = SynchronizeDocumentWorker.class.getName();

    private String uuid;
    private String repositoryPath;
    private String repositoryDocumentsName;
    private ServerCredentials credentials;
    private SynchronizeLog log;
    private OpenKMWS ws;

    public SynchronizeDocumentWorker(String uuid, String repositoryPath, String repositoryDocumentsName, ServerCredentials credentials, SynchronizeLog log) {
        this.uuid = uuid;
        this.repositoryPath = repositoryPath;
        this.repositoryDocumentsName = repositoryDocumentsName;
        this.credentials = credentials;
        this.log = log;
        this.ws = ws;
        
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
            
            SynchronizedFolder sf = SynchronizedFolder.getDocumentsSynchronizedFolder(repositoryDocumentsName, repositoryPath, null);
            String path = Utils.buildLocalFilePath(repositoryPath, repositoryDocumentsName);
            Document doc = ws.getDocument(uuid);
            String fname = Utils.getRealName(Utils.getName(doc.getPath()), path);
            File fdoc = new File(Utils.buildLocalFilePath(repositoryPath, repositoryDocumentsName, fname));
            createDocument(fdoc, ws.getContentDocument(uuid));   
            String relativePath = null;
            SynchronizedObject sobj = new SynchronizedObject(Utils.getName(doc.getPath()), fdoc.getPath(), relativePath, doc.getUuid(), doc.getPath(), doc.getLastModified().getTimeInMillis(), doc.getActualVersion().getName(), Boolean.FALSE);
            sf.getSynchronizeObjects().add(sobj);
            sf.persistSynchronizedObjects();
            
        } catch (SynchronizeException e) {
            log.error(KEY_BUNDLE, e);
        }
    }

    @Override
    public void stopWorker() {
        interrupt();       
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

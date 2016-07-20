package com.openkm.okmsynchronize.ws;


import com.openkm.okmsynchronize.utils.SynchronizeException;
import com.openkm.sdk4j.OKMWebservices;
import com.openkm.sdk4j.bean.Document;
import com.openkm.sdk4j.bean.Folder;
import com.openkm.sdk4j.exception.AccessDeniedException;
import com.openkm.sdk4j.exception.AutomationException;
import com.openkm.sdk4j.exception.DatabaseException;
import com.openkm.sdk4j.exception.ExtensionException;
import com.openkm.sdk4j.exception.FileSizeExceededException;
import com.openkm.sdk4j.exception.ItemExistsException;
import com.openkm.sdk4j.exception.LockException;
import com.openkm.sdk4j.exception.PathNotFoundException;
import com.openkm.sdk4j.exception.RepositoryException;
import com.openkm.sdk4j.exception.UnknowException;
import com.openkm.sdk4j.exception.UnsupportedMimeTypeException;
import com.openkm.sdk4j.exception.UserQuotaExceededException;
import com.openkm.sdk4j.exception.VersionException;
import com.openkm.sdk4j.exception.VirusDetectedException;
import com.openkm.sdk4j.exception.WebserviceException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementació de la interície OopenkmWS per la versió Openkm-sdk4j
 *
 * @author Andreu Bujosa Bestard
 * @version 1.0
 * @see com.openkm.odesktop.ws.OopenkmWS_sdk
 */
public class OpenKMWS_sdk1_0 implements OpenKMWS {

    private final String KEYBUNDLE = OpenKMWS_sdk1_0.class.getName();
    public final static OpenKMWSVersions VERSION = OpenKMWSVersions.v1_0;
    
    private OKMWebservices ws = null;
    private String host = null;
    private String user = "openkmxx";
    private String password = "openkmxx";
    
    /**
     * Objecte que emmagatzema la instï¿½ncia de la classe segons el patrï¿½
     * singleton.
     */
    private static OpenKMWS_sdk1_0 singleton = null;

    /**
     * Construeix un objecte de la classe. Aquest mï¿½tode ï¿½s privat per
     * forï¿½ar el patrï¿½ singleton.
     */
    private OpenKMWS_sdk1_0(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
        
        //ws = OKMWebservicesFactory.newInstance(host, user, password);
    }

    /**
     * Recupera l'objecte singleton de la classe.
     */
    public static OpenKMWS_sdk1_0 getOopenkmWS_sdk1_0(String host, String user, String password) {
        if(singleton == null) {
            singleton = new OpenKMWS_sdk1_0(host, user, password);
            
        }
        return singleton;
    }        
    
//    public OopenkmWS_sdk1_0(String host, String user, String password) {
//        this.host = host;
//        this.user = user;
//        this.password = password;
//        
//        ws = OKMWebservicesFactory.newInstance(host, user, password);
//    }   

    @Override
    public boolean isConnectionSuccessful() {
        return true;
    }
    
    @Override
    public void renameDocument(String node, String newName) throws SynchronizeException {
        try {            
            ws.renameDocument(node, newName);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        } catch (AccessDeniedException ex) {
            throw new SynchronizeException(ex);
        } catch (ItemExistsException ex) {
            throw new SynchronizeException(ex);
        } catch (LockException ex) {
           throw new SynchronizeException(ex);
        } catch (ExtensionException ex) {
            throw new SynchronizeException(ex);
        }
    }
    
    @Override
    public void renameFolder(String node, String newName) throws SynchronizeException {
        try {            
            ws.renameFolder(node, newName);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        } catch (AccessDeniedException ex) {
            throw new SynchronizeException(ex);
        } catch (ItemExistsException ex) {
            throw new SynchronizeException(ex);
        }
    }
    
    @Override
    public boolean exists(String node, String object) {
        try {
            return ws.hasNode(node);
        } catch (RepositoryException ex) {
            return false;
        } catch (DatabaseException ex) {
            return false;
        } catch (UnknowException ex) {
            return false;
        } catch (WebserviceException ex) {
            return false;
        }
    }

    @Override
    public boolean isFolder(String node) {
        try {
            return ws.isValidFolder(node);
        } catch (PathNotFoundException ex) {
            return false;
        } catch (AccessDeniedException ex) {
            return false;
        } catch (RepositoryException ex) {
            return false;
        } catch (DatabaseException ex) {
            return false;
        } catch (UnknowException ex) {
            return false;
        } catch (WebserviceException ex) {
            return false;
        }
    }       

    @Override
    public void createFolder(String node)  throws SynchronizeException {
        try {
            ws.createFolderSimple(node);
        } catch (AccessDeniedException ex) {
            throw new SynchronizeException(ex);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (ItemExistsException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (ExtensionException ex) {
            throw new SynchronizeException(ex);
        } catch (AutomationException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        }
    }

    @Override
    public void deleteFolder(String node) throws SynchronizeException {
        try {
            ws.deleteFolder(node);
        } catch (LockException ex) {
             throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
             throw new SynchronizeException(ex);
        } catch (AccessDeniedException ex) {
             throw new SynchronizeException(ex);
        } catch (RepositoryException ex) {
             throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
             throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
             throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
             throw new SynchronizeException(ex);
        }
    }

    @Override
    public List<Folder> listFolders(String node) throws SynchronizeException {
        try {
            List<Folder> list = new ArrayList<Folder>();
            list = ws.getFolderChildren(node);
            return list;
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        }
    }

    @Override
    public Folder getFodler(String node) throws SynchronizeException {        
        try {
            return ws.getFolderProperties(node);
        } catch (AccessDeniedException ex) {
            throw new SynchronizeException(ex);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        }
    }

    @Override
    public boolean isDocument(String node) {
        try {
            return ws.isValidDocument(node);
        } catch (PathNotFoundException ex) {
            return false;
        } catch (AccessDeniedException ex) {
            return false;
        } catch (RepositoryException ex) {
            return false;
        } catch (DatabaseException ex) {
            return false;
        } catch (UnknowException ex) {
            return false;
        } catch (WebserviceException ex) {
            return false;
        }
    }

    @Override
    public Document getDocument(String node) throws SynchronizeException {
        try {
            return ws.getDocumentProperties(node);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        }
    }
    
    @Override
    public String getNodeUuid(String uuid) throws SynchronizeException {
        String node = null;
        try {
            node = ws.getNodeUuid(uuid);

            if(node != null) {
                return node;
            } else {
                throw new SynchronizeException(new Exception(uuid + " not found."));
            }
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        }
    }

    @Override
    public void deleteDocument(String node) throws SynchronizeException {
        try {
            ws.deleteDocument(node);
        } catch (AccessDeniedException ex) {
            throw new SynchronizeException(ex);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (LockException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (ExtensionException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        }
    }

    @Override
    public byte[] getContentDocument(String node) throws SynchronizeException {
        try {
            return getBytes(ws.getContent(node));
        } catch (RepositoryException ex) {
             throw new SynchronizeException(ex);
        } catch (IOException ex) {
             throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
             throw new SynchronizeException(ex);
        } catch (AccessDeniedException ex) {
             throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
             throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
             throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
             throw new SynchronizeException(ex);
        }
    }

    @Override
    public void setContentDocument(String node, InputStream is, String comment) throws SynchronizeException {
        try {
            if (isDocument(node)) {
                ws.checkout(node);
                ws.checkin(node, is, comment);
            } else {
                ws.createDocumentSimple(node, is);
            }
        } catch (AccessDeniedException ex) {
            throw new SynchronizeException(ex);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (LockException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        } catch (FileSizeExceededException ex) {
            throw new SynchronizeException(ex);
        } catch (UserQuotaExceededException ex) {
            throw new SynchronizeException(ex);
        } catch (VirusDetectedException ex) {
            throw new SynchronizeException(ex);
        } catch (VersionException ex) {
            throw new SynchronizeException(ex);
        } catch (IOException ex) {
            throw new SynchronizeException(ex);
        } catch (ExtensionException ex) {
            throw new SynchronizeException(ex);
        } catch (UnsupportedMimeTypeException ex) {
            throw new SynchronizeException(ex);
        } catch (ItemExistsException ex) {
            throw new SynchronizeException(ex);
        } catch (AutomationException ex) {
            throw new SynchronizeException(ex);
        }
    }

    @Override
    public List<Document> listDocuments(String node) throws SynchronizeException {
        try {
            return ws.getDocumentChildren(node);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        }
    }

    @Override
    public String getRootNode(String context) throws SynchronizeException {
        String node = null;
        try {
            if ("Taxonomy".equals(context)) {
                node = ws.getRootFolder().getPath();
            } else if ("Templates".equals(context)) {
                node = ws.getTemplatesFolder().getPath();
            } else if ("My documents".equals(context)) {
                node = ws.getPersonalFolder().getPath();
            } else {
                node = ws.getMailFolder().getPath();
            }
        } catch (PathNotFoundException ex) {
            throw new SynchronizeException(ex);
        } catch (RepositoryException ex) {
            throw new SynchronizeException(ex);
        } catch (DatabaseException ex) {
            throw new SynchronizeException(ex);
        } catch (UnknowException ex) {
            throw new SynchronizeException(ex);
        } catch (WebserviceException ex) {
            throw new SynchronizeException(ex);
        } finally {
            return node;
        }
    }
    
    private byte[] getBytes(InputStream is) throws IOException {
        int len;
        int size = 1024;
        byte[] buf;
        
        if(is instanceof ByteArrayInputStream) {
            size = is.available();
            buf = new byte[size];
            len = is.read(buf, 0, size);            
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            buf = new byte[size];
            while((len = is.read(buf, 0, size)) != -1) {
                bos.write(buf, 0, len);                
            }
            buf = bos.toByteArray();
        }
        return buf;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 * @author abujosa
 */
public class SynchronizedObject {
    
    private String name;
    private String oldPath;
    private String localPath;
    private String relativePath;
    private Long localTime;
    private String uuid;
    private String serverPath;
    @Deprecated
    private Long serverTime;
    private String version;
    private StateSynchronizeObject state;
    private ConflictType conflictType;
    private Boolean rootFolder;
    private Boolean folder;
    @Deprecated
    private String conflictMessage;

    public SynchronizedObject(String name, String localPath, String uuid, String serverPath) {
        this.name = name;
        this.localPath = localPath;
        this.uuid = uuid;
        this.serverPath = serverPath;
        this.rootFolder = Boolean.TRUE;
    }

    public SynchronizedObject(String name, String localPath,String relativePath, Long localTime, Boolean folder) {
        this.name = name;
        this.localPath = localPath;
        this.relativePath = relativePath;
        this.localTime = localTime;
        this.folder = folder;
        this.state = StateSynchronizeObject.NEW;
    }

    /** 
    * Constructor used in download Objects from server
    */
    public SynchronizedObject(String name, String localPath, String relativePath, String uuid, String serverPath, Long serverTime, String version, Boolean folder) {
        this.name = name;
        this.localPath = localPath;
        this.relativePath = relativePath;
        this.uuid = uuid;
        this.serverPath = serverPath;
        this.serverTime = serverTime;        
        this.version = version;
        this.folder = folder;
        this.rootFolder = Boolean.FALSE;
        this.state = StateSynchronizeObject.UPDATE;
        if(!folder) {
            this.localTime = (new File(localPath)).lastModified();
        } else {
            this.localTime = -1l;
        }
    }

    /**
     * Constructor used in server tree nodes     
     */
    public SynchronizedObject(String name, String uuid, String serverPath, Boolean folder) {
        this.name = name;
        this.uuid = uuid;
        this.serverPath = serverPath;
        this.folder = folder;
        this.rootFolder = Boolean.FALSE;
    }     

    public SynchronizedObject() {  }    
    
    public void update(Long lastModified) {
        if(StateSynchronizeObject.UPDATE.equals(state) && !localTime.equals(lastModified) && !folder) {
            state = StateSynchronizeObject.MODIFIED;
            localTime = lastModified;
        } 
    }   

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public Long getLocalTime() {
        return localTime;
    }

    public void setLocalTime(Long localTime) {
        this.localTime = localTime;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getServerPath() {
        return serverPath;
    }

    public void setServerPath(String serverPath) {
        this.serverPath = serverPath;
    }

    @Deprecated
    public Long getServerTime() {
        return serverTime;
    }
    @Deprecated
    public void setServerTime(Long serverTime) {
        this.serverTime = serverTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public StateSynchronizeObject getState() {
        return state;
    }

    public void setState(StateSynchronizeObject state) {
        this.state = state;
        if(!StateSynchronizeObject.CONFLICT.equals(state)) { conflictType = ConflictType.NO_CONFLICT; }        
    }

    public Boolean isRootFolder() {
        return rootFolder != null && rootFolder;
    }

    public void setRootFolder(Boolean rootFolder) {
        this.rootFolder = rootFolder;
    }

    public Boolean isFolder() {
        return folder;
    }

    public void setFolder(Boolean folder) {
        this.folder = folder;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public String getOldPath() {
        return oldPath;
    }

    public void setOldPath(String oldPath) {
        this.oldPath = oldPath;
    }
    
    @Deprecated
    public String getConflictMessage() {
        return conflictMessage;
    }

    @Deprecated
    public void setConflictMessage(String conflictMessage) {
        this.conflictMessage = conflictMessage;
    }

    public ConflictType getConflictType() {
        return conflictType;
    }

    public void setConflictType(ConflictType conflictType) {
        this.conflictType = conflictType;
    }
    
    public boolean contains(String search) {
        if(Utils.isEmpty(search)) { return false; }
        else {
            return (search.equals(localPath) || search.equals(uuid) || search.equals(serverPath));
        }
    }
    
    public String getKey() {
        StringBuilder str = new StringBuilder();
        
        if(!Utils.isEmpty(uuid)) { str.append(uuid); }
        str.append("@");
        if(!Utils.isEmpty(localPath)) { str.append(localPath); }
        
        return str.toString();
    }
    
    public File toFile() {
        if(!Utils.isEmpty(localPath)) {
            return new File(localPath);
        } else {
            return null;
        }
    }
    
    public boolean isPossibleToRename() {
        return (Utils.isEmpty(uuid) && StateSynchronizeObject.CONFLICT.equals(state)) 
                || (uuid != null && !Utils.getName(serverPath).equals(name));
    }
    
    public void rename(String newName) throws IOException {
        Path p = FileSystems.getDefault().getPath(localPath);            
        localPath = FileSystems.getDefault().getPath(localPath.substring(0, localPath.indexOf(p.getFileName().toString())), newName).toString(); 
        relativePath = Utils.getRelativePath(localPath);
        name = newName;
    }
    
    @Override
    public String toString() {
    	return localPath;
    }
    

}

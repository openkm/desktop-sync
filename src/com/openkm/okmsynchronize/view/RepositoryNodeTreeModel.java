/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.view;

import javax.swing.ImageIcon;

/**
 *
 * @author abujosa
 */
public class RepositoryNodeTreeModel {
    
    private String name;
    private String virtualName;
    private String path;
    private String synchronizeFolder;
    private String uuid;
    private Boolean root;
    private Boolean sync;
    private String type;
    private Boolean folder;

    public RepositoryNodeTreeModel(String name, String virtualName, String path, Boolean root, String type) {
        this.name = name;
        this.virtualName = virtualName;
        this.path = path;
        this.synchronizeFolder = null;
        this.root = root;    
        this.sync = false;
        this.uuid = null;
        this.type = type;
        this.folder = Boolean.TRUE;
    }

    public RepositoryNodeTreeModel(String name, String virtualName, String path, String synchronizeFolder, String uuid, Boolean root, Boolean sync, Boolean folder) {
        this.name = name;
        this.virtualName = virtualName;
        this.path = path;
        this.synchronizeFolder = synchronizeFolder;
        this.root = root;
        this.sync = sync;
        this.uuid = uuid;
        this.folder = folder;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }   

    public boolean isRoot() {
        return root;
    }

    public void setRoot(boolean root) {
        this.root = root;
    }
    
    public void setSynchronized(boolean sync) {
        this.sync = sync;
    }
    
    public boolean isSynchronized() {
        return sync;
    }
    
    @Override
    public String toString() {
        return virtualName;
    } 

    public String getSynchronizeFolder() {
        return synchronizeFolder;
    }

    public void setSynchronizeFolder(String synchronizeFolder) {
        this.synchronizeFolder = synchronizeFolder;
    }

    public String getUuid() {
        return uuid;
    }     

    public String getType() { return type; }

    public Boolean isFolder() { return folder; }   
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RepositoryNodeTreeModel other = (RepositoryNodeTreeModel) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }    
}

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
public class ServerNodeTreeModel {
    
    private String name;
    private String path;
    private String uuid;
    private Boolean folder;
    private Boolean root;

    public ServerNodeTreeModel(String name, String path, String uuid, Boolean folder, Boolean root) {
        this.name = name;
        this.path = path;
        this.uuid = uuid;
        this.folder = folder;
        this.root = root;
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

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Boolean isFolder() {
        return folder;
    }

    public void setFolder(Boolean folder) {
        this.folder = folder;
    }

    public void setRoot(Boolean root) {
        this.root = root;
    }
    
    
    @Override
    public String toString() {
        return name;
    }  

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.uuid != null ? this.uuid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerNodeTreeModel other = (ServerNodeTreeModel) obj;
        if ((this.uuid == null) ? (other.uuid != null) : !this.uuid.equals(other.uuid)) {
            return false;
        }
        return true;
    }
    
    
    
}

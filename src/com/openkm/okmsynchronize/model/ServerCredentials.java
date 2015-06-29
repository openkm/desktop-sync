/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.utils.Utils;
import com.openkm.okmsynchronize.ws.OpenKMWSVersions;

/**
 *
 * @author abujosa
 */
public class ServerCredentials {

    private String userName;
    private String password;
    private String host;
    private OpenKMWSVersions version;

    public ServerCredentials(String userName, String password, String host, OpenKMWSVersions version) {
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.version = version;
    }
    
    public void refresh(String userName, String password, String host, OpenKMWSVersions version) {
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.version = version;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public OpenKMWSVersions getVersion() {
        return version;
    }

    public void setVersion(OpenKMWSVersions version) {
        this.version = version;
    }
    
    public boolean isValid() {

        return !Utils.isEmpty(userName) && !Utils.isEmpty(password) && !Utils.isEmpty(host) && version != null;
    }
    
    public String getInfo() {
        StringBuilder str = new StringBuilder();
        
        str.append("---------- Server Credentials info ----------\n");        
        str.append("Host: ").append(host).append("\n");
        str.append("User: ").append(userName).append("\n");
        str.append("SDK version: ").append(version.getId()).append("\n");
        
        return str.toString();
    }
    
    public String getInfoConnection() {
        StringBuilder str = new StringBuilder();        
        str.append(!Utils.isEmpty(userName)? userName : "no userName").append("@").append(!Utils.isEmpty(host)? host: "no host");        
        return str.toString();
    }  

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ServerCredentials other = (ServerCredentials) obj;
        if ((this.userName == null) ? (other.userName != null) : !this.userName.equals(other.userName)) {
            return false;
        }
        if ((this.password == null) ? (other.password != null) : !this.password.equals(other.password)) {
            return false;
        }
        if ((this.host == null) ? (other.host != null) : !this.host.equals(other.host)) {
            return false;
        }
        return true;
    }
    
    
}

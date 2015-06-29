/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.openkm.okmsynchronize.ws;

import com.openkm.okmsynchronize.model.ContextOpenKMServer;
import com.openkm.okmsynchronize.utils.Utils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author abujosab
 */
public enum OpenKMWSVersions {
    
      v1_0 ("com.openkm.okmsynchronize.ws.OpenKMWS_sdk1_0")
    , v2_0 ("com.openkm.okmsynchronize.ws.OpenKMWS_sdk2_0");
    
    private String clazz;    
    
    OpenKMWSVersions(String key) {
        this.clazz = key;
    }  

    public String getId() {return this.name();}
    
    public String getClazz() {return clazz;}
        
    public static List<OpenKMWSVersions> getOKMWebServiceVersions() {
        List<OpenKMWSVersions> _list = new ArrayList<OpenKMWSVersions>();
        _list.add(v1_0);
        _list.add(v2_0);
        
        return _list;
    }
    
    public static String[] getOpenKMVersion() {
        String[] scl = new String[1];
        
        scl[0] = v2_0.getId();
        
        return scl;
    }
    
    public static OpenKMWSVersions getById(String id){
        
        if(Utils.isEmpty(id)) { return null; }
        else if (id.equals(v1_0.getId())) { return v1_0; }
        else if (id.equals(v2_0.getId())) { return v2_0; }        
        else { return null; }
    }
}

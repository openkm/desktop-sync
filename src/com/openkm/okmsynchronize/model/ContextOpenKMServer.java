/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.utils.Utils;

/**
 *
 * @author abujosa
 */
public enum ContextOpenKMServer {
    
      TAXONOMY      ("Taxonomy", "Taxonomy")
    , TEMPLATES     ("Templates", "Templates")
    , MY_DOCUMENTS  ("My documents", "My documents")
    , E_MAIL        ("E-mail", "E-mail");
    
    private String name;
    private String nodeName;
    
    private ContextOpenKMServer(String name, String nodeName) {
        this.name = name;
        this.nodeName = nodeName;
    }
    
    public String getName() {
        return name;
    }

    public String getContext() {
        return name();
    } 

    public String getNodeName() {
        return nodeName;
    }
    
    public static String[] getServerContextList() {
        String[] scl = new String[4];
        
        scl[0] = TAXONOMY.getName();
        scl[1] = TEMPLATES.getName();
        scl[2] = MY_DOCUMENTS.getName();
        scl[3] = E_MAIL.getName();
        
        return scl;
    }
    
    public static ContextOpenKMServer getByContext(String cntx){
        
        if(Utils.isEmpty(cntx)) { return null; }
        else if (cntx.equals(TAXONOMY.getName())) { return TAXONOMY; }
        else if (cntx.equals(TEMPLATES.getName())) { return TEMPLATES; }
        else if (cntx.equals(MY_DOCUMENTS.getName())) { return MY_DOCUMENTS; }
        else if (cntx.equals(E_MAIL.getName())) { return E_MAIL; }
        else { return null; }
    }
}

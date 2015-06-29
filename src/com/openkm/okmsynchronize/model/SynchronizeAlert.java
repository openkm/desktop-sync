package com.openkm.okmsynchronize.model;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.model.SynchronizeAlert
 */
public class SynchronizeAlert {
    
    public enum SynchronizeAlertType {
        
          INFO      ("Information")
        , ERROR     ("Error")
        , NOTIF     ("Notification");
        
        private String text;
        
        private SynchronizeAlertType(String text) {
            this.text = text;
        }
        
        public String getType() { return name(); }
        public String getText() { return text; }
    }
    
    
    private Integer uuid;
    private SynchronizeAlertType type;
    private String message;
    private Throwable error;
    private String information;
    private Boolean read;
    private Date date;

    public SynchronizeAlert(String message, String description, Throwable error) {
        this.message = message;
        this.error = error;
        this.information = description;
        this.type = SynchronizeAlertType.ERROR;
        this.read = Boolean.FALSE;
        this.date = new Date();
        this.uuid = hashCode();
    }

    public SynchronizeAlert(String message, String description, SynchronizeAlertType type) {        
        this.message = message;
        this.information = description;
        this.type = type;
        this.read = Boolean.FALSE;
        this.date = new Date();
        this.uuid = hashCode();
    }

    
    public Integer getUuid() { return uuid; }
    
    public SynchronizeAlertType getType() { return type; }

    public String getMessage() { return message; }

    public Throwable getError() { return error; }   

    public Date getDate() { return date; }

    public Boolean isRead() { return read; }
    public void setRead(Boolean read) { this.read = read; }

    public String getInformation() {
        StringBuilder info = new StringBuilder();                
        if (SynchronizeAlertType.ERROR.equals(type)) {
            info.append(error.getMessage());
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            error.printStackTrace(pw);
            info.append(sw.toString());                   
        } else {
            info.append(information);
        }        
        return info.toString();
    }        
        

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.type != null ? this.type.hashCode() : 0);
        hash = 89 * hash + (this.message != null ? this.message.hashCode() : 0);
        hash = 89 * hash + (this.error != null ? this.error.hashCode() : 0);
        hash = 89 * hash + (this.information != null ? this.information.hashCode() : 0);
        hash = 89 * hash + (this.read != null ? this.read.hashCode() : 0);
        hash = 89 * hash + (this.date != null ? this.date.hashCode() : 0);
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
        final SynchronizeAlert other = (SynchronizeAlert) obj;
        if (this.uuid != other.uuid && (this.uuid == null || !this.uuid.equals(other.uuid))) {
            return false;
        }
        return true;
    }    
    
}

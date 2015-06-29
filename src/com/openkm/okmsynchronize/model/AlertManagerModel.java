package com.openkm.okmsynchronize.model;

import com.openkm.okmsynchronize.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * Classe principal de Odesktop
 *
 * @author abujosab (Andreu Bujosa Bestard)
 * @version 1.0
 * @see com.openkm.okmsynchronize.model.AlertManagerModel
 */
public class AlertManagerModel {

    public enum KeyChecks {

        KEY_NEW_ALERT, KEY_NEW_FOLDER_ADD, KEY_FODER_SYNCHRONIZE_RUNNING, KEY_DOCUMENT_SYNCHRONIZE_RUNNING;        
    }
    
    // Alerts
    private List<SynchronizeAlert> alerts;
    
    // Checks
    private HashMap<KeyChecks, Boolean> checks;   
    
    private boolean showAlerts;
        
    private static AlertManagerModel singleton = new AlertManagerModel();

    /**
     * Construeix un objecte de la classe. Aquest mètode és privat per forçar el
     * patrò singleton.
     */
    private AlertManagerModel() {
        super();
        alerts = new ArrayList<SynchronizeAlert>();
        checks = new HashMap<KeyChecks, Boolean>();        
        for(KeyChecks key : KeyChecks.values()) { checks.put(key, Boolean.FALSE); }
        
        showAlerts = false;
    }

    /**
     * Recupera l'objecte singleton de la classe.
     */
    public static AlertManagerModel getAlertManagerModel() {
        return singleton;
    }        
    
    public void addAlert(SynchronizeAlert alert) {
        alerts.add(alert);
        checks.put(KeyChecks.KEY_NEW_ALERT, Boolean.TRUE);
    }
    
    public void clearAllAlerts() { alerts.clear();  }
    
    public List<SynchronizeAlert> getAllAlerts() {
        return alerts;
    }
    
    public SynchronizeAlert getAlertByUuid(String uuid) {
        if(Utils.isEmpty(uuid)) { return null; }
        for(SynchronizeAlert alert : alerts) {
            if((uuid).equals(alert.getUuid())) { return alert; }
        }
        return null;
    }

    public List<SynchronizeAlert> getAlerts(SynchronizeAlert.SynchronizeAlertType type) {        
        checks.put(KeyChecks.KEY_NEW_ALERT, Boolean.FALSE);
        if(type == null) { return getAllAlerts(); }
        List<SynchronizeAlert> list = new ArrayList<SynchronizeAlert>();
        for(SynchronizeAlert alert : alerts) {
            if (type.equals(alert.getType())) { list.add(alert); }        
        }
        return list;
    }
    
    public void deleteAlert(SynchronizeAlert alert) {
        if(alert != null) {
            alerts.remove(alert);
        }
    }
    
    public String[] getAlertTypes() {
        String [] array = new String[4];
        
        array[0] = "All";
        array[1] = SynchronizeAlert.SynchronizeAlertType.INFO.getText();
        array[2] = SynchronizeAlert.SynchronizeAlertType.NOTIF.getText();
        array[3] = SynchronizeAlert.SynchronizeAlertType.ERROR.getText();
                
        return array;
    }
    
    public SynchronizeAlertsTableModel createTableModel() {
        SynchronizeAlertsTableModel tmodel = new SynchronizeAlertsTableModel(getAllAlerts());
        return tmodel;
    }
    
    public boolean isCheckAlertActive(KeyChecks key) { return checks.get(key); }
    public void setCheckAlert(KeyChecks key, boolean active) { checks.put(key, active); }        

    public void setShowAlerts(boolean showAlerts) { this.showAlerts = showAlerts; }
        
    public class SynchronizeAlertsTableModel extends AbstractTableModel {

        private Object[][] data;
        private SynchronizeAlert[] alerts;
        private String[] columnNames;
        private Class[] types = new Class[]{java.lang.String.class,
            java.lang.String.class,
            java.lang.String.class};

        protected SynchronizeAlertsTableModel(List<SynchronizeAlert> list) {

            columnNames = new String[]{"Alert", "Type", "Date"};
            initModel(list);
        }

        @Override
        public Class getColumnClass(int columnIndex) {
            return types[columnIndex];
        }

        @Override
        public int getRowCount() {
            if (data != null) {
                return data.length;
            } else {
                return 0;
            }
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data[rowIndex][columnIndex];
        }

        public String getColumnName(int columnIndex) {
            return columnNames[columnIndex];
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            data[rowIndex][columnIndex] = aValue;
            fireTableCellUpdated(rowIndex, columnIndex);
        }

        public void refreshTableContent(List<SynchronizeAlert> list) {
            initModel(list);
            fireTableDataChanged();
        }

        public void initModel(List<SynchronizeAlert> list) {
            data = new Object[list.size()][columnNames.length + 1];
            alerts = new SynchronizeAlert[list.size()];

            int index = 0;
            for (SynchronizeAlert alert : list) {
                data[index][0] = alert.getMessage();
                data[index][1] = alert.getType().getText();
                data[index][2] = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(alert.getDate());
                data[index][3] = alert.getUuid();
                
                alerts[index] = alert;
                index++;
            }
        }
        
        public SynchronizeAlert getSynchronizeAlert(int row)  {
            return alerts[row];
        }
        
        public void deleteRow(int row) {
            Object[][] dataTemp = new Object[data.length - 1][columnNames.length + 1];
            SynchronizeAlert[] alertsTemp = new SynchronizeAlert[alerts.length - 1];

            int index = 0;
            for (int i = 0; i < data.length; i++) {
                if (i != row) {
                    dataTemp[index] = data[i];
                    alertsTemp[index] = alerts[i];
                    index++;
                }
            }

            data = new Object[dataTemp.length][columnNames.length + 1];
            alerts = new SynchronizeAlert[alertsTemp.length];
            for (int i = 0; i < dataTemp.length; i++) {
                data[i] = dataTemp[i];
                alerts[i] = alertsTemp[i];
            }
        }
    }

}

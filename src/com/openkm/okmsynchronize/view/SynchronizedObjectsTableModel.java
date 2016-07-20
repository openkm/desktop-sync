/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.openkm.okmsynchronize.view;

import com.openkm.okmsynchronize.model.SynchronizedObject;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author abujosa
 */
public class SynchronizedObjectsTableModel extends AbstractTableModel {
    
    private Object[][] data;
    private SynchronizedObject[]  synchronizeObjects;
    private String[] columnNames;
    private Class[] types = new Class[]{java.lang.String.class,
                                        java.lang.String.class,
                                        java.lang.String.class,
                                        java.lang.String.class,
                                        java.lang.String.class,
                                        java.lang.String.class};
    
    public SynchronizedObjectsTableModel(List<SynchronizedObject> list) {
        
        columnNames = new String [] {"Name", "Type", "Server path", "Server version", "Last updated", "State"};
        initModel(list);     
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
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
    
    public void refreshTableContent(List<SynchronizedObject> list) {
        initModel(list);        
        fireTableDataChanged();
    }
    public void changeRowValue(int row, SynchronizedObject sobj) {
        data[row][0] = sobj.getName();
        data[row][1] = sobj.isFolder() ? "folder" : "document";
        data[row][2] = sobj.getServerPath();
        data[row][3] = sobj.getVersion();
        data[row][4] = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(sobj.getLocalTime());
        data[row][5] = sobj.getState().getDescription();
        synchronizeObjects[row] = sobj;
        fireTableCellUpdated(row, 0);       
        fireTableCellUpdated(row, 1);       
        fireTableCellUpdated(row, 2);       
        fireTableCellUpdated(row, 3);       
        fireTableCellUpdated(row, 4);       
        fireTableCellUpdated(row, 5);       
    }
    
    private void initModel(List<SynchronizedObject> list) {
        data = new Object[list.size()][columnNames.length];
        synchronizeObjects = new SynchronizedObject[list.size()];
        
        int index = 0;
        for(SynchronizedObject sobj : list) {
            data[index][0] = sobj.getName();
            data[index][1] = sobj.isFolder() ? "folder" : "document";
            data[index][2] = sobj.getServerPath();
            data[index][3] = sobj.getVersion();
            data[index][4] = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(sobj.getLocalTime());
            data[index][5] = sobj.getState().getDescription();
            synchronizeObjects[index] = sobj;
            index++;
        }        
    }
    
    public SynchronizedObject getSelectedSynchronizeObject(int row) {
        return synchronizeObjects[row];
    }
}

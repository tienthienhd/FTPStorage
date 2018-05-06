/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorage.views.components;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author HP Zbook 15
 */
public class FileRemoteTableModel extends AbstractTableModel {

    private String[] files;
    private String[] columns = {
        "Icon",
        "Name",
        "Type",
        "Size",
        "Last Modified",};

    public FileRemoteTableModel() {
        this.files = new String[0];
    }

    public FileRemoteTableModel(String[] files) {
        this.files = files;
    }

    public Object getValueAt(int row, int column) {
        String file = files[row];
//        System.out.println(files.length);
        //System.out.println("row" + row + ":" + file);
        String[] parsed = file.split("\\s+");
        String name = parsed[7];
        switch (column) {
            case 0:
                if (parsed[0].startsWith("d")) {
                    return new ImageIcon("src/resources/Folder.png");
                } else {
                    return new ImageIcon("src/resources/File.png");
                }
            case 1:
                return name;
            case 2:
                if (parsed[0].startsWith("d")) {
                    return "";
                } else {
                    return name.substring(name.lastIndexOf('.') + 1);
                }
            case 3:
                return parsed[4];
            case 4:
                return parsed[5] + " " + parsed[6];
            default:
                System.err.println("Logic Error");
        }
        return "";
    }

    public int getColumnCount() {
        return columns.length;
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return ImageIcon.class;
            case 3:
                return Long.class;
            case 4:
                return String.class;
        }
        return String.class;
    }

    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return files.length;
    }

    public void setFiles(String[] files) {
        this.files = files;
        this.fireTableDataChanged();
    }
    
    public String getFileName(int row){
        return files[row].split("\\s+")[7];
    }
    
    public boolean isFile(int row){
        return !files[row].startsWith("d");
    }
    
    public String getName(int row){
        String[] parsed = files[row].split("\\s+");
        String name = parsed[7];
        return name;
    }
}

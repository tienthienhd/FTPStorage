/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorage.views;

import ftpstorage.views.components.FileLocalTableModel;
import java.io.File;
import javax.swing.JFileChooser;

/**
 *
 * @author HP Zbook 15
 */
public class PanelLocalFile extends javax.swing.JPanel {

    private FileLocalTableModel fileLocalTableModel;
    private File currentLocalFolder;
    
    /**
     * Creates new form PanelLocalFile
     */
    public PanelLocalFile() {
        this.currentLocalFolder = new File("./local/");
        this.fileLocalTableModel = new FileLocalTableModel(currentLocalFolder.listFiles());
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        txtFilePathLocal = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        pnlShowFileLocal = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblShowFileLocal = new javax.swing.JTable();

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Local File", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        setMaximumSize(new java.awt.Dimension(512, 318));
        setMinimumSize(new java.awt.Dimension(512, 318));
        setPreferredSize(new java.awt.Dimension(512, 318));

        jLabel5.setText("File path:");

        tblShowFileLocal.setAutoCreateRowSorter(true);
        tblShowFileLocal.setModel(fileLocalTableModel);
        tblShowFileLocal.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tblShowFileLocal.setShowVerticalLines(false);
        jScrollPane1.setViewportView(tblShowFileLocal);

        javax.swing.GroupLayout pnlShowFileLocalLayout = new javax.swing.GroupLayout(pnlShowFileLocal);
        pnlShowFileLocal.setLayout(pnlShowFileLocalLayout);
        pnlShowFileLocalLayout.setHorizontalGroup(
            pnlShowFileLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        pnlShowFileLocalLayout.setVerticalGroup(
            pnlShowFileLocalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 259, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(8, 8, 8)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFilePathLocal, javax.swing.GroupLayout.PREFERRED_SIZE, 419, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlShowFileLocal, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFilePathLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlShowFileLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

     private File chooseFile() {
        JFileChooser chooser = new JFileChooser(currentLocalFolder);
        chooser.setDialogTitle("Choose folder destination to download file.");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPanel pnlShowFileLocal;
    private javax.swing.JTable tblShowFileLocal;
    private javax.swing.JTextField txtFilePathLocal;
    // End of variables declaration//GEN-END:variables
}

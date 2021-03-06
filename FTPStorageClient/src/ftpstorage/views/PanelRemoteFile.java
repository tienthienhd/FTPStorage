/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorage.views;

import ftpstorage.client.IClient;
import ftpstorage.views.components.FileRemoteTableModel;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JTable;

/**
 *
 * @author HP Zbook 15
 */
public class PanelRemoteFile extends javax.swing.JPanel {
    
    private IClient client;

    private FileRemoteTableModel fileRemoteTableModel = new FileRemoteTableModel();
    /**
     * Creates new form PanelRemoteFile
     */
    public PanelRemoteFile() {
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

        popupMenuOptionFile = new javax.swing.JPopupMenu();
        menuItemDownload = new javax.swing.JMenuItem();
        jLabel6 = new javax.swing.JLabel();
        txtFilePathRemote = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblShowFileRemote = new javax.swing.JTable();

        menuItemDownload.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/Download.png"))); // NOI18N
        menuItemDownload.setText("Download");
        menuItemDownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemDownloadActionPerformed(evt);
            }
        });
        popupMenuOptionFile.add(menuItemDownload);

        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Remote File", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12))); // NOI18N
        setMaximumSize(new java.awt.Dimension(720, 318));
        setMinimumSize(new java.awt.Dimension(720, 318));
        setPreferredSize(new java.awt.Dimension(720, 318));

        jLabel6.setText("File path:");

        tblShowFileRemote.setAutoCreateRowSorter(true);
        tblShowFileRemote.setModel(fileRemoteTableModel);
        tblShowFileRemote.setShowVerticalLines(false);
        tblShowFileRemote.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblShowFileRemoteMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblShowFileRemote);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtFilePathRemote)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(txtFilePathRemote, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tblShowFileRemoteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblShowFileRemoteMouseClicked
        switch (evt.getButton()) {
            case 1:
            break;
            case 2:
            break;
            case 3:
            JTable table = (JTable) evt.getComponent();
            int row = table.rowAtPoint(evt.getPoint());
            int column = table.columnAtPoint(evt.getPoint());
            if (!table.isRowSelected(row)) {
                table.changeSelection(row, column, false, false);
            }
            this.popupMenuOptionFile.show(evt.getComponent(), evt.getX(), evt.getY());
            break;
        }
    }//GEN-LAST:event_tblShowFileRemoteMouseClicked

    private void menuItemDownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemDownloadActionPerformed
        int row = this.tblShowFileRemote.getSelectedRow();
        try {
            if (!this.fileRemoteTableModel.isFile(row)) {
                throw new IOException("This is folder. Canot download folder");
            }
            String filename = this.fileRemoteTableModel.getFileName(row);

            File f = new File(chooseFile(), filename);
            long[] offsets = new long[4];
            long length = client.get(filename, offsets, f);
//            this.filesStatus.addFileStatus(filename, length);

        } catch (IOException ex) {
            Logger.getLogger(MainFrameClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_menuItemDownloadActionPerformed

    private File chooseFile() {
        JFileChooser chooser = new JFileChooser("./local/");
        chooser.setDialogTitle("Choose folder destination to download file.");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }
        return null;
    }
    
    public void showRemoteFile(IClient client) {
        this.client = client;
        String response = null;
        try {
            this.txtFilePathRemote.setText(client.pwd());
            response = this.client.list();
        } catch (IOException ex) {
            Logger.getLogger(MainFrameClient.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        String[] parsed = response.split("\n");
        this.fileRemoteTableModel.setFiles(parsed);
    }

    public void removeShowRemoteFile() {
        this.txtFilePathRemote.setText("");
        this.fileRemoteTableModel.setFiles(new String[0]);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JMenuItem menuItemDownload;
    private javax.swing.JPopupMenu popupMenuOptionFile;
    private javax.swing.JTable tblShowFileRemote;
    private javax.swing.JTextField txtFilePathRemote;
    // End of variables declaration//GEN-END:variables
}
